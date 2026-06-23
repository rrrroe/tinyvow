param(
    [string]$SigningProperties = "release-signing\tinyvow-cn-release.properties",
    [string]$OutputApk = ""
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

$versionName = Get-GradleProperty -Name "TINYVOW_VERSION_NAME"
$versionCode = Get-GradleProperty -Name "TINYVOW_VERSION_CODE"
$chinaVersionName = "$versionName-cn"

if ([string]::IsNullOrWhiteSpace($OutputApk)) {
    $OutputApk = "dist\tinyvow-cn-$versionName-vc$versionCode-release.apk"
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
foreach ($tool in @($zipalign, $apksigner, $aapt)) {
    if (!(Test-Path $tool)) {
        throw "Required Android build tool not found: $tool"
    }
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

& .\gradlew.bat assembleChinaRelease
if ($LASTEXITCODE -ne 0) {
    throw "assembleChinaRelease failed."
}

$unsigned = "app\build\outputs\apk\china\release\app-china-release-unsigned.apk"
$signed = "app\build\outputs\apk\china\release\app-china-release.apk"

$distDir = Split-Path -Parent $OutputApk
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
$aligned = Join-Path $distDir "tinyvow-cn-release-aligned.apk"

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
    throw "Neither signed nor unsigned China release APK was found."
}

& $apksigner verify --verbose $OutputApk
if ($LASTEXITCODE -ne 0) {
    throw "apksigner verify failed."
}

$badging = & $aapt dump badging $OutputApk
$permissions = & $aapt dump permissions $OutputApk
$badgingText = $badging -join "`n"
$permissionsText = $permissions -join "`n"

$badging | Select-Object -First 8
$permissions

foreach ($forbidden in @(
        "com.android.vending.BILLING",
        "android.permission.USE_BIOMETRIC",
        "android.permission.USE_FINGERPRINT"
    )) {
    if ($permissionsText -match [regex]::Escape($forbidden)) {
        throw "Forbidden permission still present in China release APK: $forbidden"
    }
}

if ($badgingText -notmatch "name='com\.rrrrz\.tinyvow\.cn'") {
    throw "Unexpected package name in APK."
}
if ($badgingText -notmatch "versionCode='$([regex]::Escape($versionCode))'") {
    throw "Unexpected versionCode in APK."
}
if ($badgingText -notmatch "versionName='$([regex]::Escape($chinaVersionName))'") {
    throw "Unexpected versionName in APK."
}

Write-Host "China release APK ready: $OutputApk"
