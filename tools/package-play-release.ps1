param(
    [string]$SigningProperties = "release-signing\tinyvow-play-upload.properties",
    [string]$OutputAab = ""
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Get-GradleProperty {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $gradleProperties = Join-Path $root "gradle.properties"
    $line = Get-Content -Encoding UTF8 $gradleProperties |
        Where-Object { $_ -match "^\s*$([regex]::Escape($Name))=(.*)$" } |
        Select-Object -First 1
    if ($null -eq $line) {
        throw "Missing $Name in gradle.properties"
    }
    return ($line -replace "^\s*$([regex]::Escape($Name))=", "").Trim()
}

function Get-KeystoreCertificateSha256 {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Keytool,
        [Parameter(Mandatory = $true)]
        [hashtable]$Properties
    )

    $certificateFile = [IO.Path]::GetTempFileName()
    Remove-Item -Force $certificateFile
    try {
        & $Keytool `
            -exportcert `
            -keystore $Properties["storeFile"] `
            -storepass $Properties["storePassword"] `
            -alias $Properties["keyAlias"] `
            -file $certificateFile | Out-Null
        if ($LASTEXITCODE -ne 0 -or !(Test-Path $certificateFile)) {
            throw "Unable to export the Play upload certificate."
        }
        return (Get-FileHash -Algorithm SHA256 $certificateFile).Hash.ToUpperInvariant()
    } finally {
        Remove-Item -Force -ErrorAction SilentlyContinue $certificateFile
    }
}

$versionName = Get-GradleProperty -Name "TINYVOW_VERSION_NAME"
$versionCode = Get-GradleProperty -Name "TINYVOW_PLAY_VERSION_CODE"
$expectedUploadCertificateSha256 = (Get-GradleProperty -Name "TINYVOW_PLAY_UPLOAD_CERT_SHA256").ToUpperInvariant()

if ([string]::IsNullOrWhiteSpace($OutputAab)) {
    $OutputAab = "dist\tinyvow-googleplay-$versionName-vc$versionCode-release.aab"
}

if (!(Test-Path $SigningProperties)) {
    throw "Play upload signing properties not found: $SigningProperties"
}
$props = @{}
Get-Content -Encoding UTF8 $SigningProperties | ForEach-Object {
    if ($_ -match "^(.*?)=(.*)$") {
        $props[$Matches[1]] = $Matches[2]
    }
}
foreach ($key in @("storeFile", "storePassword", "keyAlias", "keyPassword")) {
    if ([string]::IsNullOrWhiteSpace($props[$key])) {
        throw "Missing $key in $SigningProperties"
    }
}
if (!(Test-Path $props["storeFile"])) {
    throw "Play upload keystore not found: $($props["storeFile"])"
}

$keytoolCommand = Get-Command keytool -ErrorAction SilentlyContinue
$jarsignerCommand = Get-Command jarsigner -ErrorAction SilentlyContinue
if ($null -eq $keytoolCommand -or $null -eq $jarsignerCommand) {
    throw "keytool and jarsigner are required."
}

$uploadCertificateSha256 = Get-KeystoreCertificateSha256 `
    -Keytool $keytoolCommand.Source `
    -Properties $props
if ($uploadCertificateSha256 -ne $expectedUploadCertificateSha256) {
    throw "Play upload keystore certificate does not match TINYVOW_PLAY_UPLOAD_CERT_SHA256."
}

& .\gradlew.bat :app:bundleGooglePlayRelease
if ($LASTEXITCODE -ne 0) {
    throw "bundleGooglePlayRelease failed."
}

$bundleOutput = "app\build\outputs\bundle\googlePlayRelease\app-googlePlay-release.aab"
if (!(Test-Path $bundleOutput)) {
    throw "Google Play release bundle not found: $bundleOutput"
}

$outputDirectory = Split-Path -Parent $OutputAab
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
Copy-Item -Force $bundleOutput $OutputAab

& $jarsignerCommand.Source -verify $OutputAab | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "Google Play AAB signature verification failed."
}

$aabCertificateOutput = & $keytoolCommand.Source -printcert -jarfile $OutputAab
if ($LASTEXITCODE -ne 0) {
    throw "Unable to read the Google Play AAB signing certificate."
}
$aabCertificateText = $aabCertificateOutput -join "`n"
$aabCertificateMatch = [regex]::Match(
    $aabCertificateText,
    "SHA-?256:\s*([0-9a-fA-F:]+)"
)
if (!$aabCertificateMatch.Success) {
    throw "Unable to read the Google Play AAB signing certificate SHA-256."
}
$aabUploadCertificateSha256 = $aabCertificateMatch.Groups[1].Value.Replace(":", "").ToUpperInvariant()
if ($aabUploadCertificateSha256 -ne $expectedUploadCertificateSha256) {
    throw "Google Play AAB was not signed by the expected upload certificate."
}

$manifestPath = Join-Path $root "app\build\intermediates\packaged_manifests\googlePlayRelease\processGooglePlayReleaseManifestForPackage\AndroidManifest.xml"
if (!(Test-Path $manifestPath)) {
    throw "Final Google Play packaged manifest was not found: $manifestPath"
}
$manifestXml = [xml](Get-Content -Raw -Encoding UTF8 $manifestPath)
$androidNamespace = "http://schemas.android.com/apk/res/android"
$applicationId = $manifestXml.manifest.package
$actualVersionCode = $manifestXml.manifest.GetAttribute("versionCode", $androidNamespace)
$actualVersionName = $manifestXml.manifest.GetAttribute("versionName", $androidNamespace)
if ($applicationId -ne "com.rorolo.tinyvow") {
    throw "Unexpected Google Play application ID: $applicationId"
}
if ($actualVersionCode -ne $versionCode) {
    throw "Unexpected Google Play versionCode: $actualVersionCode"
}
if ($actualVersionName -ne $versionName) {
    throw "Unexpected Google Play versionName: $actualVersionName"
}

$aabArchive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $OutputAab))
try {
    if ($null -eq $aabArchive.GetEntry("base/manifest/AndroidManifest.xml")) {
        throw "Google Play AAB does not contain base/manifest/AndroidManifest.xml."
    }
} finally {
    $aabArchive.Dispose()
}

$aabHash = (Get-FileHash -Algorithm SHA256 $OutputAab).Hash.ToUpperInvariant()
Write-Host "Google Play release AAB ready: $OutputAab"
Write-Host "  Package: $applicationId"
Write-Host "  Version: $actualVersionName ($actualVersionCode)"
Write-Host "  SHA-256: $aabHash"
Write-Host "  Upload certificate SHA-256: $aabUploadCertificateSha256"
