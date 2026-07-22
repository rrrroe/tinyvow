$ErrorActionPreference = 'Stop'

$taskDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$chrome = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
$sheetPath = Join-Path $taskDir 'output\sheet.png'
$htmlUri = [System.Uri]::new((Join-Path $taskDir 'index.html')).AbsoluteUri

& $chrome --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 --window-size=1208,9100 --screenshot=$sheetPath $htmlUri
if (-not (Test-Path $sheetPath)) { throw "Chrome did not produce $sheetPath" }

Add-Type -AssemblyName System.Drawing
$sheet = [System.Drawing.Bitmap]::new($sheetPath)
try {
    $expectedWidth = 1208
    $expectedHeight = 9008
    if ($sheet.Width -ne $expectedWidth -or $sheet.Height -lt $expectedHeight) {
        throw "Unexpected sheet size: $($sheet.Width)x$($sheet.Height), expected at least ${expectedWidth}x${expectedHeight}."
    }
    $top = 64
    1..6 | ForEach-Object {
        $index = $_
        $sourceY = $top + (($index - 1) * 1488)
        $crop = [System.Drawing.Rectangle]::new(64, $sourceY, 1080, 1440)
        $poster = $sheet.Clone($crop, $sheet.PixelFormat)
        try {
            $poster.Save((Join-Path $taskDir ("output\\xhs-{0:d2}-{1}.png" -f $index, @('cover','method','interrupt','focus','review','privacy')[$index - 1])), [System.Drawing.Imaging.ImageFormat]::Png)
        }
        finally { $poster.Dispose() }
    }
}
finally { $sheet.Dispose() }
