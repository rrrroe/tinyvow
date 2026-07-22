"""Render the Tiny Vow website QR distribution card.

The QR matrix is downloaded separately as `website-qr-core.png` with high
error correction. Keep its four-module quiet zone intact; this script adds
only the presentation card and the small centered app mark.
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).parent
OUT = ROOT / "tinyvow-website-qr-rorolo-v1.png"
CORE = ROOT / "website-qr-core.png"
APP_ICON = ROOT.parent.parent / "app" / "src" / "main" / "res" / "drawable" / "tinyvow_share_app_icon.png"

WIDTH, HEIGHT = 1600, 2100
CREAM = "#F7F3EE"
SURFACE = "#FFFDFC"
INK = "#203B33"
MUTED = "#667A72"
GREEN = "#5EAA8D"
PEACH = "#F3C8B7"
BLUE = "#AFC9E7"
# The QR uses a single quiet sage family rather than a rainbow pattern.
# Both values retain enough contrast against white for scanning.
QR_DATA = (91, 142, 122)
QR_FINDER = (70, 116, 97)

FONT_REGULAR = "C:/Windows/Fonts/Noto Sans SC (TrueType).otf"
FONT_MEDIUM = "C:/Windows/Fonts/Noto Sans SC Medium (TrueType).otf"
FONT_BOLD = "C:/Windows/Fonts/Noto Sans SC Bold (TrueType).otf"
TITLE_SIZE = 76
TEXT_SIZE = 38


def font(path: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(path, size)


def center_text(draw: ImageDraw.ImageDraw, text: str, y: int, font_: ImageFont.FreeTypeFont, fill: str) -> None:
    width, _ = draw.textsize(text, font=font_)
    draw.text(((WIDTH - width) / 2, y), text, font=font_, fill=fill)


def rounded_mask(size: int, radius: int) -> Image.Image:
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size, size), radius=radius, fill=255)
    return mask


def rounded_rectangle(draw: ImageDraw.ImageDraw, box: tuple, radius: int, fill: str, outline: str = None, width: int = 1) -> None:
    """Pillow 5-compatible rounded rectangle."""
    left, top, right, bottom = box
    draw.rectangle((left + radius, top, right - radius, bottom), fill=fill)
    draw.rectangle((left, top + radius, right, bottom - radius), fill=fill)
    draw.ellipse((left, top, left + radius * 2, top + radius * 2), fill=fill)
    draw.ellipse((right - radius * 2, top, right, top + radius * 2), fill=fill)
    draw.ellipse((left, bottom - radius * 2, left + radius * 2, bottom), fill=fill)
    draw.ellipse((right - radius * 2, bottom - radius * 2, right, bottom), fill=fill)
    if outline:
        for inset in range(width):
            draw.arc((left + inset, top + inset, left + radius * 2 - inset, top + radius * 2 - inset), 180, 270, fill=outline)
            draw.arc((right - radius * 2 + inset, top + inset, right - inset, top + radius * 2 - inset), 270, 360, fill=outline)
            draw.arc((left + inset, bottom - radius * 2 + inset, left + radius * 2 - inset, bottom - inset), 90, 180, fill=outline)
            draw.arc((right - radius * 2 + inset, bottom - radius * 2 + inset, right - inset, bottom - inset), 0, 90, fill=outline)
        draw.line((left + radius, top, right - radius, top), fill=outline, width=width)
        draw.line((left + radius, bottom, right - radius, bottom), fill=outline, width=width)
        draw.line((left, top + radius, left, bottom - radius), fill=outline, width=width)
        draw.line((right, top + radius, right, bottom - radius), fill=outline, width=width)


def draw_title_with_icon(canvas: Image.Image, title: str) -> None:
    """Keep the product mark compact instead of introducing a separate logo row."""
    draw = ImageDraw.Draw(canvas)
    title_font = font(FONT_BOLD, TITLE_SIZE)
    title_width, _ = draw.textsize(title, font=title_font)
    icon_size = 76
    gap = 22
    left = int((WIDTH - icon_size - gap - title_width) / 2)
    icon = Image.open(APP_ICON).convert("RGBA")
    icon.thumbnail((icon_size, icon_size), Image.LANCZOS)
    tile = Image.new("RGBA", (icon_size, icon_size), "#FFFDFC")
    tile_draw = ImageDraw.Draw(tile)
    rounded_rectangle(tile_draw, (0, 0, icon_size - 1, icon_size - 1), 23, "#FFFDFC", "#DCE8E0", 2)
    tile.alpha_composite(icon, ((icon_size - icon.width) // 2, (icon_size - icon.height) // 2))
    canvas.alpha_composite(tile, (left, 302))
    draw.text((left + icon_size + gap, 292), title, font=title_font, fill=INK)


def colorize_qr(qr: Image.Image) -> Image.Image:
    """Use only dark colours on data modules; finder zones stay deep green."""
    mono = qr.convert("L")
    result = Image.new("RGBA", mono.size, (0, 0, 0, 0))
    source = mono.load()
    target = result.load()
    side = mono.size[0]
    finder_size = int(side * 0.30)
    for y in range(side):
        for x in range(side):
            if source[x, y] >= 128:
                continue
            # Keep the three scan anchors visually conventional and robust.
            in_top_left = x < finder_size and y < finder_size
            in_top_right = x > side - finder_size and y < finder_size
            in_bottom_left = x < finder_size and y > side - finder_size
            if in_top_left or in_top_right or in_bottom_left:
                color = QR_FINDER
            else:
                color = QR_DATA
            target[x, y] = (*color, 255)
    return result


def main() -> None:
    # A calm, warm background with restrained versions of the app icon colours.
    canvas = Image.new("RGBA", (WIDTH, HEIGHT), CREAM)
    background = ImageDraw.Draw(canvas)
    background.ellipse((-230, 1280, 660, 2170), fill=PEACH)
    background.ellipse((1040, -280, 1830, 510), fill=BLUE)
    background.ellipse((-270, -250, 510, 530), fill="#B6D8C4")

    # Soft surface card; shadows are deliberately subtle so printed copies stay clean.
    shadow = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    rounded_rectangle(ImageDraw.Draw(shadow), (130, 190, 1470, 1900), radius=82, fill=(32, 59, 51, 28))
    shadow = shadow.filter(ImageFilter.GaussianBlur(28))
    canvas.alpha_composite(shadow)
    draw = ImageDraw.Draw(canvas)
    rounded_rectangle(draw, (130, 170, 1470, 1880), radius=82, fill=SURFACE, outline="#E6DED5", width=3)

    # Header uses one combined product name with the app mark at its left.
    draw_title_with_icon(canvas, "TinyVow小约定")
    center_text(draw, "给自己一个温和、可坚持的约定", 420, font(FONT_REGULAR, TEXT_SIZE), MUTED)

    # QR stays high-contrast and preserves its own white quiet zone.
    qr = Image.open(CORE).convert("L").resize((940, 940), Image.NEAREST)
    qr_coloured = colorize_qr(qr)
    qr_backing = Image.new("RGBA", (1012, 1012), "#FFFFFF")
    qr_backing_draw = ImageDraw.Draw(qr_backing)
    rounded_rectangle(qr_backing_draw, (0, 0, 1011, 1011), radius=36, fill="#FFFFFF", outline="#E5ECE7", width=3)
    canvas.alpha_composite(qr_backing, (294, 632))
    canvas.alpha_composite(qr_coloured, (330, 668))

    # A small, error-correction-safe central mark (about 15% of QR width).
    logo_tile = Image.new("RGBA", (146, 146), "#FFFDFC")
    tile_draw = ImageDraw.Draw(logo_tile)
    rounded_rectangle(tile_draw, (1, 1, 144, 144), radius=38, fill="#FFFDFC", outline="#DCE8E0", width=3)
    icon = Image.open(APP_ICON).convert("RGBA")
    icon.thumbnail((108, 108), Image.LANCZOS)
    logo_tile.alpha_composite(icon, ((146 - icon.width) // 2, (146 - icon.height) // 2))
    canvas.alpha_composite(logo_tile, (727, 1075))

    # Footer has one clear action and a quiet brand byline.
    center_text(draw, "扫描二维码，前往官网获取小约定", 1714, font(FONT_MEDIUM, TEXT_SIZE), INK)
    center_text(draw, "tinyvow.rorolo.com  ·  by Rorolo", 1781, font(FONT_MEDIUM, TEXT_SIZE), "#60766B")

    canvas.convert("RGB").save(OUT, quality=96, optimize=True)
    print(OUT)


if __name__ == "__main__":
    main()
