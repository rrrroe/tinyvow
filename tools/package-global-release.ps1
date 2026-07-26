param(
    [string]$SigningProperties = "release-signing\tinyvow-global-app-signing.properties",
    [string]$OutputApk = "",
    [string]$WebsiteDirectory = "",
    [switch]$PrepareWebsite
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
    if (!(Test-Path $gradleProperties)) {
        throw "gradle.properties not found: $gradleProperties"
    }

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
            throw "Unable to export the signing certificate from the configured keystore."
        }
        return (Get-FileHash -Algorithm SHA256 $certificateFile).Hash.ToUpperInvariant()
    } finally {
        Remove-Item -Force -ErrorAction SilentlyContinue $certificateFile
    }
}

$versionName = Get-GradleProperty -Name "TINYVOW_VERSION_NAME"
$versionCode = Get-GradleProperty -Name "TINYVOW_GLOBAL_VERSION_CODE"
$expectedCertificateSha256 = (Get-GradleProperty -Name "TINYVOW_GLOBAL_APP_SIGNING_CERT_SHA256").ToUpperInvariant()

if ([string]::IsNullOrWhiteSpace($OutputApk)) {
    $OutputApk = "dist\tinyvow-global-$versionName-vc$versionCode-release.apk"
}

$sdkDir = $env:ANDROID_HOME
if ([string]::IsNullOrWhiteSpace($sdkDir)) {
    $localProperties = Join-Path $root "local.properties"
    if (Test-Path $localProperties) {
        $sdkLine = Get-Content -Encoding UTF8 $localProperties |
            Where-Object { $_ -match "^sdk\.dir=" } |
            Select-Object -First 1
        if ($sdkLine) {
            $sdkDir = ($sdkLine -replace "^sdk\.dir=", "").Replace("\:", ":")
        }
    }
}
if ([string]::IsNullOrWhiteSpace($sdkDir) -or !(Test-Path $sdkDir)) {
    throw "Android SDK not found. Set ANDROID_HOME or sdk.dir in local.properties."
}

$buildToolsRoot = Join-Path $sdkDir "build-tools"
$buildToolsDir = Get-ChildItem -Directory $buildToolsRoot |
    Sort-Object Name -Descending |
    Select-Object -First 1
if ($null -eq $buildToolsDir) {
    throw "No Android build-tools directory found under $buildToolsRoot."
}

$zipalign = Join-Path $buildToolsDir.FullName "zipalign.exe"
$apksigner = Join-Path $buildToolsDir.FullName "apksigner.bat"
$aapt = Join-Path $buildToolsDir.FullName "aapt.exe"
$keytoolCommand = Get-Command keytool -ErrorAction SilentlyContinue
foreach ($tool in @($zipalign, $apksigner, $aapt)) {
    if (!(Test-Path $tool)) {
        throw "Required Android build tool not found: $tool"
    }
}
if ($null -eq $keytoolCommand) {
    throw "keytool is unavailable."
}

if (!(Test-Path $SigningProperties)) {
    throw "Signing properties not found: $SigningProperties"
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
    throw "Keystore not found: $($props["storeFile"])"
}
$keystoreCertificateSha256 = Get-KeystoreCertificateSha256 `
    -Keytool $keytoolCommand.Source `
    -Properties $props
if ($keystoreCertificateSha256 -ne $expectedCertificateSha256) {
    throw "Global signing keystore certificate does not match TINYVOW_GLOBAL_APP_SIGNING_CERT_SHA256."
}

& .\gradlew.bat assembleGlobalRelease
if ($LASTEXITCODE -ne 0) {
    throw "assembleGlobalRelease failed."
}

$unsigned = "app\build\outputs\apk\global\release\app-global-release-unsigned.apk"
$signed = "app\build\outputs\apk\global\release\app-global-release.apk"

$distDir = Split-Path -Parent $OutputApk
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
$aligned = Join-Path $distDir "tinyvow-global-release-aligned.apk"

Remove-Item -Force -ErrorAction SilentlyContinue $OutputApk, $aligned, "$OutputApk.idsig"

if (Test-Path $signed) {
    Copy-Item -Force $signed $OutputApk
} elseif (Test-Path $unsigned) {
    & $zipalign -f -p 4 $unsigned $aligned
    if ($LASTEXITCODE -ne 0) {
        throw "zipalign failed."
    }

    & $apksigner sign `
        --ks $props["storeFile"] `
        --ks-key-alias $props["keyAlias"] `
        --ks-pass "pass:$($props["storePassword"])" `
        --key-pass "pass:$($props["keyPassword"])" `
        --out $OutputApk `
        $aligned
    if ($LASTEXITCODE -ne 0) {
        throw "apksigner sign failed."
    }
} else {
    throw "Neither signed nor unsigned Global release APK was found."
}

$signatureOutput = & $apksigner verify --verbose --print-certs $OutputApk
if ($LASTEXITCODE -ne 0) {
    throw "apksigner verify failed."
}
$signatureText = $signatureOutput -join "`n"
$certificateMatch = [regex]::Match(
    $signatureText,
    "certificate SHA-256 digest:\s*([0-9a-fA-F]+)"
)
if (!$certificateMatch.Success) {
    throw "Unable to read the Global APK signing certificate SHA-256."
}
$apkCertificateSha256 = $certificateMatch.Groups[1].Value.ToUpperInvariant()
if ($apkCertificateSha256 -ne $expectedCertificateSha256) {
    throw "Global APK was not signed by the expected App Signing certificate."
}

$badging = & $aapt dump badging $OutputApk
$permissions = & $aapt dump permissions $OutputApk
$badgingText = $badging -join "`n"
$permissionsText = $permissions -join "`n"

$badging | Select-Object -First 8
$permissions
$signatureOutput

foreach ($forbidden in @(
        "com.android.vending.BILLING",
        "android.permission.USE_BIOMETRIC",
        "android.permission.USE_FINGERPRINT"
    )) {
    if ($permissionsText -match [regex]::Escape($forbidden)) {
        throw "Forbidden permission still present in Global release APK: $forbidden"
    }
}

if ($badgingText -notmatch "name='com\.rorolo\.tinyvow'") {
    throw "Unexpected package name in Global APK."
}
if ($badgingText -match "name='com\.rrrrz\.tinyvow\.cn'") {
    throw "China package name leaked into Global APK."
}
if ($badgingText -notmatch "versionCode='$([regex]::Escape($versionCode))'") {
    throw "Unexpected versionCode in Global APK."
}
if ($badgingText -notmatch "versionName='$([regex]::Escape($versionName))'") {
    throw "Unexpected versionName in Global APK."
}

$apkHash = (Get-FileHash -Algorithm SHA256 $OutputApk).Hash.ToUpperInvariant()
$apkLength = (Get-Item $OutputApk).Length

Write-Host "Global release APK ready: $OutputApk"
Write-Host "  SHA-256: $apkHash"
Write-Host "  Size: $apkLength bytes"
Write-Host "  App signing certificate SHA-256: $apkCertificateSha256"

if ($PrepareWebsite) {
    if ([string]::IsNullOrWhiteSpace($WebsiteDirectory)) {
        $WebsiteDirectory = Join-Path (Split-Path -Parent $root) "tinyvow-site"
    }

    $websitePublishScript = Join-Path $WebsiteDirectory "scripts\publish-release.ps1"
    if (!(Test-Path $websitePublishScript)) {
        throw "Website publish script not found: $websitePublishScript"
    }

    & $websitePublishScript `
        -ApkPath (Resolve-Path $OutputApk) `
        -Version $versionName `
        -VersionCode ([int]$versionCode) `
        -Channel global `
        -DryRun
    if ($LASTEXITCODE -ne 0) {
        throw "Website preparation failed; the verified Global APK remains at $OutputApk."
    }
}
