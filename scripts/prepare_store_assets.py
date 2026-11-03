"""Render Thikana's store icon and feature graphic.

Requires Pillow and rsvg-convert. All artwork and fonts come from this repository.
Outputs opaque PNG files in output/store-listing/.
"""

from pathlib import Path
import subprocess
import tempfile
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "output/store-listing"
ART = ROOT / "feature/kits/src/main/res/drawable-nodpi"
FONTS = ROOT / "core/designsystem/src/main/res/font"
PAPER = "#F5EFDF"
FOREST = "#293F35"
INK = "#263329"
AMBER = "#EBA15B"
SAGE = "#D0D8BA"
SCALE = 3
SVG_NS = "{http://www.w3.org/2000/svg}"


def ticket_svg(color: str) -> str:
    original = ET.parse(ROOT / "assets/journal-doodles/ticket.svg").getroot()
    paths = []
    for element in original[-1].iter():
        if element.tag in (SVG_NS + "path", SVG_NS + "polygon"):
            tag = element.tag.removeprefix(SVG_NS)
            geometry = "d" if tag == "path" else "points"
            paths.append(f'<{tag} fill="{color}" {geometry}="{element.attrib[geometry]}"/>')
    return (
        '<svg xmlns="http://www.w3.org/2000/svg" '
        f'viewBox="{original.attrib["viewBox"]}">{"".join(paths)}</svg>'
    )


def render_ticket(color: str, width: int) -> Image.Image:
    with tempfile.TemporaryDirectory() as temp:
        source = Path(temp) / "ticket.svg"
        target = Path(temp) / "ticket.png"
        source.write_text(ticket_svg(color))
        subprocess.run(
            ["rsvg-convert", "-w", str(width), "-o", str(target), str(source)],
            check=True,
        )
        with Image.open(target) as image:
            return image.convert("RGBA").copy()


def font(name: str, size: int, weight: int) -> ImageFont.FreeTypeFont:
    result = ImageFont.truetype(str(FONTS / f"{name}_variable.ttf"), size * SCALE)
    axes = result.get_variation_axes()
    values = [axis["default"] for axis in axes]
    for index, axis in enumerate(axes):
        if axis["name"] == b"Weight":
            values[index] = weight
        elif axis["name"] == b"Optical Size":
            values[index] = size
    result.set_variation_by_axes(values)
    return result


def paste_scaled(canvas: Image.Image, image: Image.Image, xy: tuple, width: int, alpha=1.0):
    ratio = width * SCALE / image.width
    image = image.convert("RGBA").resize(
        (width * SCALE, round(image.height * ratio)), Image.Resampling.LANCZOS,
    )
    if alpha != 1:
        image.putalpha(image.getchannel("A").point(lambda value: round(value * alpha)))
    canvas.alpha_composite(image, (round(xy[0] * SCALE), round(xy[1] * SCALE)))


def draw_doodle(canvas: Image.Image, name: str, xy: tuple, width: int, color: str, alpha: float):
    with Image.open(ART / f"journal_doodle_{name}.webp") as source:
        tinted = Image.new("RGBA", source.size, color)
        tinted.putalpha(source.getchannel("A"))
        paste_scaled(canvas, tinted, xy, width, alpha)


def save_png(image: Image.Image, filename: str, size: tuple, maximum_bytes: int):
    target = OUTPUT / filename
    image.convert("RGB").resize(size, Image.Resampling.LANCZOS).save(target, optimize=True)
    with Image.open(target) as exported:
        assert exported.size == size
        assert exported.mode == "RGB"
        assert exported.format == "PNG"
    assert target.stat().st_size <= maximum_bytes
    print(f"{target.name}: {size[0]} × {size[1]}, {target.stat().st_size:,} bytes, opaque PNG")


def make_icon():
    canvas = Image.new("RGBA", (512 * SCALE, 512 * SCALE), PAPER)
    ticket = render_ticket(FOREST, 350 * SCALE)
    canvas.alpha_composite(
        ticket,
        ((canvas.width - ticket.width) // 2, (canvas.height - ticket.height) // 2),
    )
    save_png(canvas, "travel-with-thikana-app-icon.png", (512, 512), 1_000_000)


def make_feature():
    canvas = Image.new("RGBA", (1024 * SCALE, 500 * SCALE), PAPER)
    draw = ImageDraw.Draw(canvas)

    # A full-height journal cover; illustration stays separate from readable copy.
    cover = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    cover_draw = ImageDraw.Draw(cover)
    bounds = tuple(value * SCALE for value in (648, 32, 1048, 468))
    cover_draw.rounded_rectangle(bounds, radius=48 * SCALE, fill=FOREST)
    cover_mask = cover.getchannel("A").copy()
    cover_draw.ellipse(
        tuple(value * SCALE for value in (758, 154, 928, 324)),
        fill=AMBER,
    )
    with Image.open(ART / "journal_mountains_night.webp") as mountains:
        mountains = mountains.convert("RGBA")
        # Reposition the source's sun behind the skyline for this wider composition.
        pixels = mountains.load()
        for y in range(mountains.height):
            for x in range(mountains.width):
                red, green, blue, alpha = pixels[x, y]
                if red > 150 and 80 < green < 200 and blue < 150 and red > green * 1.2:
                    pixels[x, y] = (red, green, blue, 0)
        paste_scaled(cover, mountains, (665, 226), 395)
    draw_doodle(cover, "plane", (920, 85), 82, PAPER, .55)
    draw_doodle(cover, "compass", (960, 388), 45, SAGE, .22)

    # Quiet dotted flight path, without adding another text or interface layer.
    cover_draw = ImageDraw.Draw(cover)
    for step in range(0, 101, 5):
        t = step / 100
        x = (1 - t) ** 2 * 694 + 2 * (1 - t) * t * 830 + t ** 2 * 953
        y = (1 - t) ** 2 * 171 + 2 * (1 - t) * t * 95 + t ** 2 * 107
        radius = 1.5 * SCALE
        cover_draw.ellipse(
            (x * SCALE - radius, y * SCALE - radius, x * SCALE + radius, y * SCALE + radius),
            fill="#75857A",
        )
    ticket = render_ticket(PAPER, 158 * SCALE)
    cover.alpha_composite(ticket, (739 * SCALE, 358 * SCALE))
    cover.putalpha(cover_mask)
    canvas.alpha_composite(cover)

    # Use the same type families as the app, with enough space for store thumbnails.
    small_ticket = render_ticket(FOREST, 47 * SCALE)
    canvas.alpha_composite(small_ticket, (54 * SCALE, 47 * SCALE))
    draw = ImageDraw.Draw(canvas)
    draw.text(
        (119 * SCALE, 49 * SCALE),
        "YOUR OFFLINE TRAVEL COMPANION",
        font=font("manrope", 13, 600), fill=FOREST, anchor="lt",
    )
    heading = font("fraunces", 72, 800)
    draw.text((52 * SCALE, 129 * SCALE), "Travel with", font=heading, fill=INK, anchor="lt")
    draw.text((52 * SCALE, 216 * SCALE), "Thikana", font=heading, fill=INK, anchor="lt")
    draw.text(
        (56 * SCALE, 348 * SCALE), "Arrive ready. Travel lighter.",
        font=font("manrope", 23, 500), fill=FOREST, anchor="lt",
    )
    draw.text(
        (56 * SCALE, 391 * SCALE), "Your arrival essentials, even offline.",
        font=font("manrope", 16, 400), fill="#6B7467", anchor="lt",
    )
    draw_doodle(canvas, "pin", (553, 386), 39, FOREST, .10)
    save_png(canvas, "travel-with-thikana-feature-graphic.png", (1024, 500), 15_000_000)


if __name__ == "__main__":
    OUTPUT.mkdir(parents=True, exist_ok=True)
    make_icon()
    make_feature()
