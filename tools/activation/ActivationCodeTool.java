import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ActivationCodeTool {
    private static final String PREFIX = "TVA1";
    private static final String PRODUCT_ID = "tinyvow_pro";
    private static final String CHANNEL = "china";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return;
        }
        switch (args[0]) {
            case "generate-keypair":
                generateKeyPair(options(args));
                break;
            case "issue-code":
                issueCode(options(args));
                break;
            case "verify-code":
                verifyCode(options(args));
                break;
            default:
                usage();
        }
    }

    private static void generateKeyPair(Map<String, String> options) throws Exception {
        Path privatePath = Path.of(options.getOrDefault("--private", "tools/activation/private_key.pkcs8"));
        Path publicPath = Path.of(options.getOrDefault("--public", "tools/activation/public_key.x509"));
        Files.createDirectories(privatePath.getParent());
        Files.createDirectories(publicPath.getParent());

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        Files.writeString(privatePath, Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        Files.writeString(publicPath, Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        System.out.println("Private key: " + privatePath);
        System.out.println("Public key:  " + publicPath);
        System.out.println("Public key base64:");
        System.out.println(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
    }

    private static void issueCode(Map<String, String> options) throws Exception {
        String userId = required(options, "--user-id");
        int days = Integer.parseInt(required(options, "--days"));
        int validDays = Integer.parseInt(options.getOrDefault("--valid-days", "7"));
        String codeId = options.getOrDefault("--code-id", UUID.randomUUID().toString());
        long now = Instant.now().toEpochMilli();
        long validUntil = now + validDays * 86_400_000L;
        PrivateKey privateKey = readPrivateKey(Path.of(options.getOrDefault("--private", "tools/activation/private_key.pkcs8")));

        String payload = json(
            Map.of(
                "version", "1",
                "codeId", codeId,
                "userId", userId,
                "productId", PRODUCT_ID,
                "channel", CHANNEL,
                "durationDays", Integer.toString(days),
                "issuedAtMillis", Long.toString(now),
                "validUntilMillis", Long.toString(validUntil)
            )
        );
        String payloadPart = encodeUrl(payload.getBytes(StandardCharsets.UTF_8));
        String signaturePart = encodeUrl(sign(privateKey, payloadPart));
        System.out.println(PREFIX + "." + payloadPart + "." + signaturePart);
    }

    private static void verifyCode(Map<String, String> options) throws Exception {
        String code = required(options, "--code");
        PublicKey publicKey = readPublicKey(Path.of(options.getOrDefault("--public", "tools/activation/public_key.x509")));
        String[] parts = code.trim().split("\\.");
        if (parts.length != 3 || !PREFIX.equals(parts[0])) {
            throw new IllegalArgumentException("Invalid code format.");
        }
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(parts[1].getBytes(StandardCharsets.UTF_8));
        if (!verifier.verify(Base64.getUrlDecoder().decode(parts[2]))) {
            throw new IllegalArgumentException("Invalid signature.");
        }
        System.out.println(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
    }

    private static PrivateKey readPrivateKey(Path path) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(Files.readString(path).trim());
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private static PublicKey readPublicKey(Path path) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(Files.readString(path).trim());
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    }

    private static byte[] sign(PrivateKey privateKey, String payloadPart) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(payloadPart.getBytes(StandardCharsets.UTF_8));
        return signer.sign();
    }

    private static String encodeUrl(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static Map<String, String> options(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i += 2) {
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for " + args[i]);
            }
            options.put(args[i], args[i + 1]);
        }
        return options;
    }

    private static String required(Map<String, String> options, String key) {
        String value = options.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required option " + key);
        }
        return value;
    }

    private static String json(Map<String, String> values) {
        return "{" +
            "\"version\":" + values.get("version") + "," +
            "\"codeId\":\"" + escape(values.get("codeId")) + "\"," +
            "\"userId\":\"" + escape(values.get("userId")) + "\"," +
            "\"productId\":\"" + PRODUCT_ID + "\"," +
            "\"channel\":\"" + CHANNEL + "\"," +
            "\"durationDays\":" + values.get("durationDays") + "," +
            "\"issuedAtMillis\":" + values.get("issuedAtMillis") + "," +
            "\"validUntilMillis\":" + values.get("validUntilMillis") +
            "}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  java ActivationCodeTool generate-keypair [--private path] [--public path]");
        System.out.println("  java ActivationCodeTool issue-code --user-id ID --days N [--valid-days N] [--private path]");
        System.out.println("  java ActivationCodeTool verify-code --code CODE [--public path]");
    }
}
