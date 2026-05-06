[CmdletBinding()]
param(
    [string]$SourceDir = "D:\Users\rrrrz\Desktop\achive_icon",
    [string]$OutputDir = "E:\Project\tinyvow\app\src\main\res\drawable-nodpi"
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$typeDefinition = @"
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;

public static class AchievementIconProcessor
{
    public static void Process(string inputPath, string outputPath, bool preserveBrightEdges)
    {
        using (var original = new Bitmap(inputPath))
        {
            if (preserveBrightEdges)
            {
                using (var diamond = ProcessDiamond(original, ringSize: 10))
                using (var resized = Resize(diamond, 512, 512))
                {
                    Directory.CreateDirectory(Path.GetDirectoryName(outputPath));
                    resized.Save(outputPath, ImageFormat.Png);
                }
            }
            else
            {
                using (var working = new Bitmap(original.Width, original.Height, PixelFormat.Format32bppArgb))
                {
                    using (var graphics = Graphics.FromImage(working))
                    {
                        graphics.DrawImage(original, 0, 0, original.Width, original.Height);
                    }

                    RemoveEdgeConnectedBackground(working, preserveBrightEdges);
                    CleanupHalo(working, preserveBrightEdges);
                    using (var resized = Resize(working, 512, 512))
                    {
                        Directory.CreateDirectory(Path.GetDirectoryName(outputPath));
                        resized.Save(outputPath, ImageFormat.Png);
                    }
                }
            }
        }
    }

    private static Bitmap ProcessDiamond(Bitmap original, int ringSize)
    {
        using (var working = new Bitmap(original.Width, original.Height, PixelFormat.Format32bppArgb))
        {
            using (var graphics = Graphics.FromImage(working))
            {
                graphics.DrawImage(original, 0, 0, original.Width, original.Height);
            }

            RemoveEdgeConnectedBackground(working, preserveBrightEdges: true);
            bool[] mask = ExtractOpaqueMask(working);
            mask = MorphClose(mask, working.Width, working.Height, radius: 2);
            FillInnerHoles(mask, working.Width, working.Height);
            RepairMaskInsideDiamondSilhouette(mask, working.Width, working.Height);

            return ApplyMask(original, mask);
        }
    }

    private static void RemoveEdgeConnectedBackground(Bitmap bitmap, bool preserveBrightEdges)
    {
        var rect = new Rectangle(0, 0, bitmap.Width, bitmap.Height);
        var data = bitmap.LockBits(rect, ImageLockMode.ReadWrite, PixelFormat.Format32bppArgb);
        try
        {
            int stride = data.Stride;
            int bytes = Math.Abs(stride) * bitmap.Height;
            byte[] buffer = new byte[bytes];
            Marshal.Copy(data.Scan0, buffer, 0, bytes);

            bool[] visited = new bool[bitmap.Width * bitmap.Height];
            var queue = new Queue<Point>();

            for (int x = 0; x < bitmap.Width; x++)
            {
                EnqueueIfBackground(bitmap.Width, bitmap.Height, buffer, stride, visited, queue, x, 0, preserveBrightEdges);
                EnqueueIfBackground(bitmap.Width, bitmap.Height, buffer, stride, visited, queue, x, bitmap.Height - 1, preserveBrightEdges);
            }
            for (int y = 0; y < bitmap.Height; y++)
            {
                EnqueueIfBackground(bitmap.Width, bitmap.Height, buffer, stride, visited, queue, 0, y, preserveBrightEdges);
                EnqueueIfBackground(bitmap.Width, bitmap.Height, buffer, stride, visited, queue, bitmap.Width - 1, y, preserveBrightEdges);
            }

            int[] dx = preserveBrightEdges
                ? new[] { 1, 1, 1, 0, 0, -1, -1, -1 }
                : new[] { 1, -1, 0, 0 };
            int[] dy = preserveBrightEdges
                ? new[] { 1, 0, -1, 1, -1, 1, 0, -1 }
                : new[] { 0, 0, 1, -1 };
            int neighborCount = preserveBrightEdges ? 8 : 4;

            while (queue.Count > 0)
            {
                var point = queue.Dequeue();
                SetTransparent(buffer, stride, point.X, point.Y);

                for (int i = 0; i < neighborCount; i++)
                {
                    int nx = point.X + dx[i];
                    int ny = point.Y + dy[i];
                    if (nx < 0 || ny < 0 || nx >= bitmap.Width || ny >= bitmap.Height) continue;

                    int index = ny * bitmap.Width + nx;
                    if (visited[index]) continue;
                    visited[index] = true;
                    if (IsBackground(buffer, stride, nx, ny, preserveBrightEdges))
                    {
                        queue.Enqueue(new Point(nx, ny));
                    }
                }
            }

            Marshal.Copy(buffer, 0, data.Scan0, bytes);
        }
        finally
        {
            bitmap.UnlockBits(data);
        }
    }

    private static bool IsBackground(byte[] buffer, int stride, int x, int y, bool preserveBrightEdges)
    {
        int offset = y * stride + x * 4;
        byte b = buffer[offset];
        byte g = buffer[offset + 1];
        byte r = buffer[offset + 2];
        byte a = buffer[offset + 3];
        if (a == 0) return true;

        int max = Math.Max(r, Math.Max(g, b));
        int min = Math.Min(r, Math.Min(g, b));
        int brightness = (r + g + b) / 3;
        if (preserveBrightEdges)
        {
            return brightness >= 242 && (max - min) <= 10;
        }
        return brightness >= 232 && (max - min) <= 14;
    }

    private static bool[] ExtractOpaqueMask(Bitmap bitmap)
    {
        int width = bitmap.Width;
        int height = bitmap.Height;
        var rect = new Rectangle(0, 0, width, height);
        var data = bitmap.LockBits(rect, ImageLockMode.ReadOnly, PixelFormat.Format32bppArgb);
        try
        {
            int stride = data.Stride;
            int bytes = Math.Abs(stride) * height;
            byte[] buffer = new byte[bytes];
            Marshal.Copy(data.Scan0, buffer, 0, bytes);

            bool[] opaque = new bool[width * height];
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    opaque[y * width + x] = buffer[y * stride + x * 4 + 3] > 0;
                }
            }

            return opaque;
        }
        finally
        {
            bitmap.UnlockBits(data);
        }
    }

    private static bool[] MorphClose(bool[] mask, int width, int height, int radius)
    {
        return ErodeMask(DilateMask(mask, width, height, radius), width, height, radius);
    }

    private static bool[] DilateMask(bool[] mask, int width, int height, int radius)
    {
        bool[] result = new bool[mask.Length];
        int radiusSquared = radius * radius;

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                bool on = false;
                for (int ny = Math.Max(0, y - radius); ny <= Math.Min(height - 1, y + radius) && !on; ny++)
                {
                    for (int nx = Math.Max(0, x - radius); nx <= Math.Min(width - 1, x + radius); nx++)
                    {
                        int dx = nx - x;
                        int dy = ny - y;
                        if (dx * dx + dy * dy > radiusSquared) continue;
                        if (!mask[ny * width + nx]) continue;
                        on = true;
                        break;
                    }
                }
                result[y * width + x] = on;
            }
        }

        return result;
    }

    private static bool[] ErodeMask(bool[] mask, int width, int height, int radius)
    {
        bool[] result = new bool[mask.Length];
        int radiusSquared = radius * radius;

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                bool on = true;
                for (int ny = Math.Max(0, y - radius); ny <= Math.Min(height - 1, y + radius) && on; ny++)
                {
                    for (int nx = Math.Max(0, x - radius); nx <= Math.Min(width - 1, x + radius); nx++)
                    {
                        int dx = nx - x;
                        int dy = ny - y;
                        if (dx * dx + dy * dy > radiusSquared) continue;
                        if (mask[ny * width + nx]) continue;
                        on = false;
                        break;
                    }
                }
                result[y * width + x] = on;
            }
        }

        return result;
    }

    private static void FillInnerHoles(bool[] mask, int width, int height)
    {
        bool[] visited = new bool[mask.Length];
        var queue = new Queue<int>();
        int[] dx = new[] { 1, 1, 1, 0, 0, -1, -1, -1 };
        int[] dy = new[] { 1, 0, -1, 1, -1, 1, 0, -1 };

        for (int x = 0; x < width; x++)
        {
            SeedHole(mask, visited, queue, width, height, x, 0);
            SeedHole(mask, visited, queue, width, height, x, height - 1);
        }
        for (int y = 0; y < height; y++)
        {
            SeedHole(mask, visited, queue, width, height, 0, y);
            SeedHole(mask, visited, queue, width, height, width - 1, y);
        }

        while (queue.Count > 0)
        {
            int current = queue.Dequeue();
            int x = current % width;
            int y = current / width;

            for (int i = 0; i < 8; i++)
            {
                int nx = x + dx[i];
                int ny = y + dy[i];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;

                int next = ny * width + nx;
                if (visited[next] || mask[next]) continue;
                visited[next] = true;
                queue.Enqueue(next);
            }
        }

        for (int i = 0; i < mask.Length; i++)
        {
            if (!mask[i] && !visited[i])
            {
                mask[i] = true;
            }
        }
    }

    private static void SeedHole(bool[] mask, bool[] visited, Queue<int> queue, int width, int height, int x, int y)
    {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        int index = y * width + x;
        if (visited[index] || mask[index]) return;
        visited[index] = true;
        queue.Enqueue(index);
    }

    private static void RepairMaskInsideDiamondSilhouette(bool[] mask, int width, int height)
    {
        using (var path = new GraphicsPath())
        {
            path.AddPolygon(new[]
            {
                new PointF(625f, 34f),
                new PointF(969f, 155f),
                new PointF(1178f, 441f),
                new PointF(1150f, 812f),
                new PointF(969f, 1102f),
                new PointF(625f, 1214f),
                new PointF(284f, 1102f),
                new PointF(77f, 812f),
                new PointF(77f, 441f),
                new PointF(284f, 155f),
            });

            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    int index = y * width + x;
                    if (mask[index]) continue;
                    if (!path.IsVisible(x, y)) continue;
                    mask[index] = true;
                }
            }
        }
    }

    private static Bitmap ApplyMask(Bitmap original, bool[] mask)
    {
        var result = new Bitmap(original.Width, original.Height, PixelFormat.Format32bppArgb);
        for (int y = 0; y < original.Height; y++)
        {
            for (int x = 0; x < original.Width; x++)
            {
                result.SetPixel(x, y, mask[y * original.Width + x] ? original.GetPixel(x, y) : Color.Transparent);
            }
        }
        return result;
    }

    private static void SetTransparent(byte[] buffer, int stride, int x, int y)
    {
        int offset = y * stride + x * 4;
        buffer[offset] = 0;
        buffer[offset + 1] = 0;
        buffer[offset + 2] = 0;
        buffer[offset + 3] = 0;
    }

    private static void EnqueueIfBackground(
        int width,
        int height,
        byte[] buffer,
        int stride,
        bool[] visited,
        Queue<Point> queue,
        int x,
        int y,
        bool preserveBrightEdges)
    {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        int idx = y * width + x;
        if (visited[idx]) return;
        visited[idx] = true;
        if (!IsBackground(buffer, stride, x, y, preserveBrightEdges)) return;
        queue.Enqueue(new Point(x, y));
    }

    private static void CleanupHalo(Bitmap bitmap, bool preserveBrightEdges)
    {
        var rect = new Rectangle(0, 0, bitmap.Width, bitmap.Height);
        var data = bitmap.LockBits(rect, ImageLockMode.ReadWrite, PixelFormat.Format32bppArgb);
        try
        {
            int stride = data.Stride;
            int bytes = Math.Abs(stride) * bitmap.Height;
            byte[] buffer = new byte[bytes];
            Marshal.Copy(data.Scan0, buffer, 0, bytes);
            byte[] result = new byte[bytes];
            Buffer.BlockCopy(buffer, 0, result, 0, bytes);

            for (int y = 1; y < bitmap.Height - 1; y++)
            {
                for (int x = 1; x < bitmap.Width - 1; x++)
                {
                    int offset = y * stride + x * 4;
                    byte a = buffer[offset + 3];
                    if (a == 0) continue;

                    byte b = buffer[offset];
                    byte g = buffer[offset + 1];
                    byte r = buffer[offset + 2];
                    int max = Math.Max(r, Math.Max(g, b));
                    int min = Math.Min(r, Math.Min(g, b));
                    int brightness = (r + g + b) / 3;

                    int brightnessThreshold = preserveBrightEdges ? 246 : 220;
                    int varianceThreshold = preserveBrightEdges ? 8 : 24;
                    if (brightness < brightnessThreshold || (max - min) > varianceThreshold) continue;

                    bool touchesTransparency = false;
                    for (int ny = -1; ny <= 1 && !touchesTransparency; ny++)
                    {
                        for (int nx = -1; nx <= 1; nx++)
                        {
                            if (nx == 0 && ny == 0) continue;
                            int neighborOffset = (y + ny) * stride + (x + nx) * 4;
                            if (buffer[neighborOffset + 3] == 0)
                            {
                                touchesTransparency = true;
                                break;
                            }
                        }
                    }

                    if (!touchesTransparency) continue;

                    int alphaFloor = preserveBrightEdges ? 18 : 36;
                    int alpha = preserveBrightEdges
                        ? Math.Max(0, Math.Min(255, (brightness - 246) * 16))
                        : Math.Max(0, Math.Min(255, (brightness - 220) * 7));
                    if (alpha < alphaFloor)
                    {
                        result[offset] = 0;
                        result[offset + 1] = 0;
                        result[offset + 2] = 0;
                        result[offset + 3] = 0;
                    }
                    else
                    {
                        result[offset + 3] = (byte)Math.Min(a, alpha);
                    }
                }
            }

            Marshal.Copy(result, 0, data.Scan0, bytes);
        }
        finally
        {
            bitmap.UnlockBits(data);
        }
    }

    private static Rectangle FindOpaqueBounds(Bitmap bitmap)
    {
        int minX = bitmap.Width;
        int minY = bitmap.Height;
        int maxX = -1;
        int maxY = -1;

        var rect = new Rectangle(0, 0, bitmap.Width, bitmap.Height);
        var data = bitmap.LockBits(rect, ImageLockMode.ReadOnly, PixelFormat.Format32bppArgb);
        try
        {
            int stride = data.Stride;
            int bytes = Math.Abs(stride) * bitmap.Height;
            byte[] buffer = new byte[bytes];
            Marshal.Copy(data.Scan0, buffer, 0, bytes);

            for (int y = 0; y < bitmap.Height; y++)
            {
                for (int x = 0; x < bitmap.Width; x++)
                {
                    int offset = y * stride + x * 4;
                    byte a = buffer[offset + 3];
                    if (a == 0) continue;

                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        finally
        {
            bitmap.UnlockBits(data);
        }

        if (maxX < minX || maxY < minY)
        {
            return new Rectangle(0, 0, bitmap.Width, bitmap.Height);
        }

        return Rectangle.FromLTRB(minX, minY, maxX + 1, maxY + 1);
    }

    private static Bitmap Resize(Bitmap bitmap, int width, int height)
    {
        var result = new Bitmap(width, height, PixelFormat.Format32bppArgb);
        using (var graphics = Graphics.FromImage(result))
        {
            graphics.Clear(Color.Transparent);
            graphics.CompositingQuality = CompositingQuality.HighQuality;
            graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
            graphics.SmoothingMode = SmoothingMode.HighQuality;
            graphics.PixelOffsetMode = PixelOffsetMode.HighQuality;
            graphics.DrawImage(bitmap, new Rectangle(0, 0, width, height));
        }
        return result;
    }
}
"@

Add-Type -TypeDefinition $typeDefinition -ReferencedAssemblies System.Drawing

$tierNames = @{
    "1" = "bronze"
    "2" = "silver"
    "3" = "gold"
    "4" = "diamond"
    "5" = "legendary"
}

$typeNames = @{
    "points" = "points"
    "redeem_points" = "redeem_points"
    "control_days" = "control_days"
    "control_streak" = "control_streak"
    "encourage_days" = "encourage_days"
    "encourage_streak" = "encourage_streak"
}

function Get-OutputName {
    param([string]$FileNameWithoutExtension)

    if ($FileNameWithoutExtension -match '^([1-5])$') {
        return "achievement_tier_$($tierNames[$matches[1]]).png"
    }

    if ($FileNameWithoutExtension -match '^([1-5])-(points|redeem_points|control_days|control_streak|encourage_days|encourage_streak)$') {
        return "achievement_$($tierNames[$matches[1]])_$($typeNames[$matches[2]]).png"
    }

    throw "Unsupported icon file name: $FileNameWithoutExtension"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

Get-ChildItem -LiteralPath $OutputDir -Filter 'achievement_*.png' -File -ErrorAction SilentlyContinue |
    Remove-Item -Force

$files = Get-ChildItem -LiteralPath $SourceDir -Filter '*.png' -File | Sort-Object Name
if ($files.Count -ne 35) {
    throw "Expected 35 png files, found $($files.Count)."
}

foreach ($file in $files) {
    $outputName = Get-OutputName -FileNameWithoutExtension $file.BaseName
    $outputPath = Join-Path $OutputDir $outputName
    $preserveBrightEdges = $file.BaseName.StartsWith("4")
    [AchievementIconProcessor]::Process($file.FullName, $outputPath, $preserveBrightEdges)
    Write-Host "Generated $outputName"
}
