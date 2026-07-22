"""Render a scan-safe, icon-forward TinyVow website QR image."""

from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).parent
CORE = ROOT / "website-qr-core.png"
APP_ICON = ROOT.parent.parent / "app" / "src" / "main" / "res" / "drawable" / "tinyvow_share_app_icon.png"
OUT = ROOT / "tinyvow-website-qr-icon-v2.png"

CANVAS = 1400
QR_SIZE = 1120
QR_OFFSET = (CANVAS - QR_SIZE) // 2
DATA_GREEN = (97, 148, 128)
FINDER_GREEN = (72, 119, 100)


def rounded_rectangle(draw, box, radius, fill, outline=None, width=1):
    """Compatibility helper for the workspace's older Pillow build."""
    left, top, right, bottom = box
    draw.rectangle((left + radius, top, right - radius, bottom), fill=fill)
    draw.rectangle((left, top + radius, right, bottom - radius), fill=fill)
    draw.ellipse((left, top, left + radius * 2, top + radius * 2), fill=fill)
    draw.ellipse((right - radius * 2, top, right, top + radius * 2), fill=fill)
    draw.ellipse((left, bottom - radius * 2, left + radius * 2, bottom), fill=fill)
    draw.ellipse((right - radius * 2, bottom - radius * 2, right, bottom), fill=fill)
    if outline:
        draw.line((left + radius, top, right - radius, top), fill=outline, width=width)
        draw.line((left + radius, bottom, right - radius, bottom), fill=outline, width=width)
        draw.line((left, top + radius, left, bottom - radius), fill=outline, width=width)
        draw.line((right, top + radius, right, bottom - radius), fill=outline, width=width)


def colorize_qr(source):
    mono = source.convert("L")
    result = Image.new("RGBA", mono.size, (0, 0, 0, 0))
    source_pixels = mono.load()
    target_pixels = result.load()
    side = mono.size[0]
    finder_size = int(side * 0.30)
    for y in range(side):
        for x in range(side):
            if source_pixels[x, y] >= 128:
                continue
            finder = ((x < finder_size and y < finder_size) or
                      (x > side - finder_size and y < finder_size) or
                      (x < finder_size and y > side - finder_size))
            target_pixels[x, y] = (*(FINDER_GREEN if finder else DATA_GREEN), 255)
    return result


def main():
    canvas = Image.new("RGBA", (CANVAS, CANVAS), "#FFFDF9")
    qr = Image.open(CORE).convert("L").resize((QR_SIZE, QR_SIZE), Image.NEAREST)
    canvas.alpha_composite(colorize_qr(qr), (QR_OFFSET, QR_OFFSET))

    # At about one fifth of the QR width, the icon reads at a glance while the
    # high-error-correction core still has enough data around it to scan.
    tile_size = 248
    tile = Image.new("RGBA", (tile_size, tile_size), "#FFFDF9")
    tile_draw = ImageDraw.Draw(tile)
    rounded_rectangle(tile_draw, (0, 0, tile_size - 1, tile_size - 1), 70, "#FFFDF9", "#D9E9E0", 4)
    icon = Image.open(APP_ICON).convert("RGBA")
    icon.thumbnail((190, 190), Image.LANCZOS)
    tile.alpha_composite(icon, ((tile_size - icon.width) // 2, (tile_size - icon.height) // 2))
    canvas.alpha_composite(tile, ((CANVAS - tile_size) // 2, (CANVAS - tile_size) // 2))

    canvas.convert("RGB").save(OUT, quality=96, optimize=True)
    print(OUT)


if __name__ == "__main__":
    main()
