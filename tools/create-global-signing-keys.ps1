param(
    [string]$SigningDirectory = "release-signing"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function New-RandomSecret {
    $bytes = New-Object byte[] 32
    [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    return [Convert]::ToBase64String($bytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")
}

function Write-Utf8File {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Content
    )

    $encoding = New-Object Text.UTF8Encoding($false)
    [IO.File]::WriteAllText($Path, $Content, $encoding)
}

$keytoolCommand = Get-Command keytool -ErrorAction SilentlyContinue
if ($null -eq $keytoolCommand) {
    throw "keytool is unavailable. Install or select a JDK before generating signing keys."
}

$signingRoot = [IO.Path]::GetFullPath((Join-Path $root $SigningDirectory))
$rootFullPath = [IO.Path]::GetFullPath($root)
if (!$signingRoot.StartsWith($rootFullPath, [StringComparison]::OrdinalIgnoreCase)) {
    throw "SigningDirectory must stay inside the Tiny Vow repository."
}
New-Item -ItemType Directory -Force -Path $signingRoot | Out-Null

$definitions = @(
    @{
        Name = "Global app signing"
        BaseName = "tinyvow-global-app-signing"
        Alias = "tinyvow-global-app-signing"
        DistinguishedName = "CN=Tiny Vow Global App Signing, OU=Release Signing, O=Rorolo"
    },
    @{
        Name = "Google Play upload"
        BaseName = "tinyvow-play-upload"
        Alias = "tinyvow-play-upload"
        DistinguishedName = "CN=Tiny Vow Google Play Upload, OU=Release Signing, O=Rorolo"
    }
)

$targets = foreach ($definition in $definitions) {
    $basePath = Join-Path $signingRoot $definition.BaseName
    [pscustomobject]@{
        Definition = $definition
        Keystore = "$basePath.jks"
        Properties = "$basePath.properties"
        CertificateDer = "$basePath-cert.der"
        CertificatePem = "$basePath-cert.pem"
    }
}

$existingTargets = $targets |
    ForEach-Object { @($_.Keystore, $_.Properties, $_.CertificateDer, $_.CertificatePem) } |
    Where-Object { Test-Path $_ }
if ($existingTargets) {
    throw "Refusing to overwrite existing signing material: $($existingTargets -join ', ')"
}

foreach ($target in $targets) {
    $storePassword = New-RandomSecret
    $keyPassword = New-RandomSecret
    $definition = $target.Definition

    & $keytoolCommand.Source `
        -genkeypair `
        -keystore $target.Keystore `
        -storetype JKS `
        -storepass $storePassword `
        -alias $definition.Alias `
        -keypass $keyPassword `
        -keyalg RSA `
        -keysize 4096 `
        -sigalg SHA256withRSA `
        -validity 10000 `
        -dname $definition.DistinguishedName
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to generate $($definition.Name) key."
    }

    & $keytoolCommand.Source `
        -exportcert `
        -keystore $target.Keystore `
        -storepass $storePassword `
        -alias $definition.Alias `
        -file $target.CertificateDer
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to export $($definition.Name) DER certificate."
    }

    & $keytoolCommand.Source `
        -exportcert `
        -rfc `
        -keystore $target.Keystore `
        -storepass $storePassword `
        -alias $definition.Alias `
        -file $target.CertificatePem
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to export $($definition.Name) PEM certificate."
    }

    $properties = @"
storeFile=$($target.Keystore)
storePassword=$storePassword
keyAlias=$($definition.Alias)
keyPassword=$keyPassword
"@
    Write-Utf8File -Path $target.Properties -Content $properties

    $certificateSha256 = (Get-FileHash -Algorithm SHA256 $target.CertificateDer).Hash.ToUpperInvariant()
    Write-Host "$($definition.Name) key created."
    Write-Host "  Certificate SHA-256: $certificateSha256"
    Write-Host "  Certificate for Play Console: $($target.CertificatePem)"
}

Write-Host "Signing passwords were written only to ignored *.properties files under $signingRoot."
Write-Host "Back up the two JKS and properties files before using either key in Play Console."
