Add-Type -AssemblyName System.Drawing
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $root 'screenshots/raw/en'
$outputRoot = Join-Path $root 'screenshots/edited/en'
New-Item -ItemType Directory -Force $outputRoot | Out-Null

function New-UiFont([float]$size, [System.Drawing.FontStyle]$style = [System.Drawing.FontStyle]::Regular) {
    $families = @('Segoe UI', 'Arial')
    foreach ($family in $families) {
        try { return [System.Drawing.Font]::new($family, $size, $style, [System.Drawing.GraphicsUnit]::Pixel) } catch {}
    }
    return [System.Drawing.Font]::new([System.Drawing.FontFamily]::GenericSansSerif, $size, $style, [System.Drawing.GraphicsUnit]::Pixel)
}

function Paint-Text {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Text,
        [System.Drawing.RectangleF]$Bounds,
        [System.Drawing.Color]$Background,
        [System.Drawing.Color]$Foreground,
        [float]$FontSize,
        [System.Drawing.FontStyle]$FontStyle = [System.Drawing.FontStyle]::Regular,
        [System.Drawing.StringAlignment]$Horizontal = [System.Drawing.StringAlignment]::Near
    )
    $backgroundBrush = [System.Drawing.SolidBrush]::new($Background)
    $foregroundBrush = [System.Drawing.SolidBrush]::new($Foreground)
    $font = New-UiFont $FontSize $FontStyle
    $format = [System.Drawing.StringFormat]::new()
    $format.Alignment = $Horizontal
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $format.Trimming = [System.Drawing.StringTrimming]::EllipsisCharacter
    $Graphics.FillRectangle($backgroundBrush, $Bounds)
    $Graphics.DrawString($Text, $font, $foregroundBrush, $Bounds, $format)
    $format.Dispose()
    $font.Dispose()
    $foregroundBrush.Dispose()
    $backgroundBrush.Dispose()
}

function Open-EditableBitmap([string]$name) {
    return [System.Drawing.Bitmap]::FromFile((Join-Path $sourceRoot $name))
}

function New-Graphics([System.Drawing.Bitmap]$bitmap) {
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    return $graphics
}

function Save-Png([System.Drawing.Bitmap]$bitmap, [string]$name) {
    $path = Join-Path $outputRoot $name
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
}

$navy = [System.Drawing.Color]::FromArgb(255, 38, 55, 78)
$muted = [System.Drawing.Color]::FromArgb(255, 111, 124, 143)
$white = [System.Drawing.Color]::FromArgb(255, 255, 255, 255)
$page = [System.Drawing.Color]::FromArgb(255, 247, 250, 254)

# Home: only user-created labels are localized. App names and brand artwork remain untouched.
$homeBitmap = Open-EditableBitmap 'S01_home_overview_en.png'
$g = New-Graphics $homeBitmap
Paint-Text $g 'Complete Review +20 PT' ([System.Drawing.RectangleF]::new(690, 1087, 325, 67)) $white $muted 31 ([System.Drawing.FontStyle]::Regular)
foreach ($row in @(
    @('Video', 300, 1572, 188, 66),
    @('Shopping', 278, 1770, 210, 66),
    @('Scroll', 300, 1968, 188, 66),
    @('Review', 810, 1572, 190, 66),
    @('Fitness', 810, 1770, 190, 66),
    @('Reading', 790, 1968, 210, 66)
)) {
    Paint-Text $g $row[0] ([System.Drawing.RectangleF]::new($row[1], $row[2], $row[3], $row[4])) $white $navy 37 ([System.Drawing.FontStyle]::Bold) ([System.Drawing.StringAlignment]::Far)
}
$g.Dispose()
Save-Png $homeBitmap 'S01_home_overview_en_localized.png'
$homeBitmap.Dispose()

# Limit overlay: translate the active vow name and the three user-created focus group names.
$block = Open-EditableBitmap 'S03_block_overlay_encourage_en.jpg'
$g = New-Graphics $block
Paint-Text $g 'You have used Video today, 2 min over your vow.' ([System.Drawing.RectangleF]::new(57, 281, 470, 37)) $page $muted 19 ([System.Drawing.FontStyle]::Regular) ([System.Drawing.StringAlignment]::Center)
Paint-Text $g 'Review' ([System.Drawing.RectangleF]::new(38, 438, 145, 35)) $white $navy 20 ([System.Drawing.FontStyle]::Regular)
Paint-Text $g 'Fitness' ([System.Drawing.RectangleF]::new(307, 438, 155, 35)) $white $navy 20 ([System.Drawing.FontStyle]::Regular)
Paint-Text $g 'Reading' ([System.Drawing.RectangleF]::new(38, 633, 150, 35)) $white $navy 20 ([System.Drawing.FontStyle]::Regular)
$g.Dispose()
Save-Png $block 'S03_block_overlay_encourage_en_localized.png'
$block.Dispose()

# Running focus screen: the category is user data, so localize only its rendered label.
$running = Open-EditableBitmap 'S05_focus_running_en.png'
$g = New-Graphics $running
Paint-Text $g 'Fitness' ([System.Drawing.RectangleF]::new(370, 614, 340, 92)) $page $navy 52 ([System.Drawing.FontStyle]::Bold) ([System.Drawing.StringAlignment]::Center)
$g.Dispose()
Save-Png $running 'S05_focus_running_en_localized.png'
$running.Dispose()

# Daily focus collection: date formatting and user-created category labels.
$daily = Open-EditableBitmap 'S05_focus_daily_collection_en.jpg'
$g = New-Graphics $daily
Paint-Text $g 'Jul 18, 2026' ([System.Drawing.RectangleF]::new(306, 78, 205, 48)) $page ([System.Drawing.Color]::FromArgb(255, 117, 164, 202)) 22 ([System.Drawing.FontStyle]::Bold) ([System.Drawing.StringAlignment]::Center)
foreach ($row in @(
    @('Exam prep', 68, 858, 82, 28, 13),
    @('Fitness', 199, 858, 65, 28, 13),
    @('Walk', 332, 858, 55, 28, 13),
    @('Journal', 68, 902, 66, 28, 13),
    @('Journal', 141, 965, 75, 31, 17),
    @('Walk', 141, 1005, 67, 31, 17),
    @('Exam prep', 141, 1045, 95, 31, 17),
    @('Fitness', 141, 1085, 75, 31, 17)
)) {
    Paint-Text $g $row[0] ([System.Drawing.RectangleF]::new($row[1], $row[2], $row[3], $row[4])) $white $navy $row[5] ([System.Drawing.FontStyle]::Bold)
}
$g.Dispose()
Save-Png $daily 'S05_focus_daily_collection_en_localized.png'
$daily.Dispose()

Write-Output "Localized screenshots written to $outputRoot"
