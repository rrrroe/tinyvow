param(
    [string]$DistDir = "dist"
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
$resolvedDistDir = Join-Path $root $DistDir
New-Item -ItemType Directory -Force -Path $resolvedDistDir | Out-Null

$chinaArchive = Join-Path $resolvedDistDir "tinyvow-cn-$versionName-vc$versionCode-release.apk"
$googlePlayArchive = Join-Path $resolvedDistDir "tinyvow-googleplay-$versionName-vc$versionCode-release.aab"

& (Join-Path $root "tools\package-china-release.ps1") -OutputApk $chinaArchive
if ($LASTEXITCODE -ne 0) {
    throw "China release packaging failed."
}

& .\gradlew.bat :app:bundleGooglePlayRelease
if ($LASTEXITCODE -ne 0) {
    throw "bundleGooglePlayRelease failed."
}

$bundleOutput = Join-Path $root "app\build\outputs\bundle\googlePlayRelease\app-googlePlay-release.aab"
if (!(Test-Path $bundleOutput)) {
    throw "Google Play release bundle not found: $bundleOutput"
}

Copy-Item -Force $bundleOutput $googlePlayArchive

Write-Host "Release artifacts ready:"
Write-Host "  China APK: $chinaArchive"
Write-Host "  Google Play AAB: $googlePlayArchive"
