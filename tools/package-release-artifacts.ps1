param(
    [string]$DistDir = "dist",
    [string]$WebsiteDirectory = "",
    [switch]$PublishWebsite
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
$chinaVersionCode = Get-GradleProperty -Name "TINYVOW_VERSION_CODE"
$playVersionCode = Get-GradleProperty -Name "TINYVOW_PLAY_VERSION_CODE"
$resolvedDistDir = Join-Path $root $DistDir
New-Item -ItemType Directory -Force -Path $resolvedDistDir | Out-Null

$chinaArchive = Join-Path $resolvedDistDir "tinyvow-cn-$versionName-vc$chinaVersionCode-release.apk"
$googlePlayArchive = Join-Path $resolvedDistDir "tinyvow-googleplay-$versionName-vc$playVersionCode-release.aab"

& (Join-Path $root "tools\package-china-release.ps1") -OutputApk $chinaArchive
if ($LASTEXITCODE -ne 0) {
    throw "China release packaging failed."
}

& (Join-Path $root "tools\package-play-release.ps1") -OutputAab $googlePlayArchive
if ($LASTEXITCODE -ne 0) {
    throw "Google Play release packaging failed."
}

if ($PublishWebsite) {
    if ([string]::IsNullOrWhiteSpace($WebsiteDirectory)) {
        $WebsiteDirectory = Join-Path (Split-Path -Parent $root) "tinyvow-site"
    }

    $websitePublishScript = Join-Path $WebsiteDirectory "scripts\publish-release.ps1"
    if (!(Test-Path $websitePublishScript)) {
        throw "Website publish script not found: $websitePublishScript"
    }

    & $websitePublishScript `
        -ApkPath (Resolve-Path $chinaArchive) `
        -Version $versionName `
        -VersionCode ([int]$chinaVersionCode)
    if ($LASTEXITCODE -ne 0) {
        throw "Website publish failed; both verified release artifacts remain in $resolvedDistDir."
    }
}

Write-Host "Release artifacts ready:"
Write-Host "  China APK: $chinaArchive"
Write-Host "  Google Play AAB: $googlePlayArchive"
