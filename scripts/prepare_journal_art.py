"""Prepare supplied VectorStock SVGs as compact, transparent Android artwork.

Requires rsvg-convert and ImageMagick. Run with the directory containing the
vectorstock_<id>/vectorstock_<id>/<id>.svg downloads as the first argument.
Originals stay untouched; only selected views and colors are adapted.
"""
from pathlib import Path
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path.home() / "Downloads"
DEST = ROOT / "feature/kits/src/main/res/drawable-nodpi"
DEST.mkdir(parents=True, exist_ok=True)
NS = "{http://www.w3.org/2000/svg}"

# Viewboxes isolate artwork from stock captions, white backing, and contact sheets.
ART = {
    "journal_mountains": ("64195475", "250 1080 2500 850", 1000),
    "journal_sunrise": ("100858", None, 1200),
    "journal_travelers": ("22384057", None, 650),
    "journal_hikers": ("37134008", "250 140 850 920", 650),
    "journal_vehicle": ("53476033", None, 650),
    "journal_rest": ("63707063", "1250 100 1180 700", 650),
    "journal_footsteps": ("50228935", "700 220 620 560", 180),
    "journal_doodles": ("64372227", "1200 750 1200 800", 500),
}

with tempfile.TemporaryDirectory() as temp:
    for name, (stock_id, viewbox, width) in ART.items():
        original = SOURCE / f"vectorstock_{stock_id}" / f"vectorstock_{stock_id}" / f"vectorstock_{stock_id}.svg"
        tree = ET.parse(original)
        svg = tree.getroot()
        if name == "journal_doodles":
            # This source groups each rough ink illustration separately.
            suitcase = list(list(svg)[2])[12]
            for element in list(svg):
                if element.tag != NS + "defs":
                    svg.remove(element)
            svg.append(suitcase)
        # Backgrounds in the supplied silhouette files are direct root rectangles.
        if name not in ("journal_sunrise", "journal_doodles"):
            for element in list(svg):
                if element.tag == NS + "rect":
                    svg.remove(element)
            svg.set("fill", "#293f35")
        if viewbox:
            svg.set("viewBox", viewbox)
        raw = ET.tostring(svg, encoding="unicode")
        raw = raw.replace("#f7931e", "#eba15b")
        if name != "journal_doodles":
            raw = raw.replace("#000000", "#293f35").replace("#000;", "#293f35;")
        source = Path(temp) / f"{name}.svg"
        source.write_text(raw)
        png = Path(temp) / f"{name}.png"
        subprocess.run(["rsvg-convert", "-w", str(width), "-o", str(png), str(source)], check=True)
        treatments = {
            "journal_doodles": ["-fuzz", "8%", "-transparent", "#1a1a1a"],
            "journal_footsteps": ["-transparent", "white"],
        }.get(name, [])
        subprocess.run([
            "magick", str(png), *treatments, "-trim", "+repage",
            "-define", "webp:lossless=true", str(DEST / f"{name}.webp"),
        ], check=True)
        if name in ("journal_mountains", "journal_hikers"):
            night_svg = Path(temp) / f"{name}_night.svg"
            night_svg.write_text(raw.replace("#293f35", "#d0d8ba"))
            night_png = Path(temp) / f"{name}_night.png"
            subprocess.run(["rsvg-convert", "-w", str(width), "-o", str(night_png), str(night_svg)], check=True)
            subprocess.run([
                "magick", str(night_png), "-trim", "+repage", "-define", "webp:lossless=true",
                str(DEST / f"{name}_night.webp"),
            ], check=True)
        print(name, (DEST / f"{name}.webp").stat().st_size)

# Reuse the mountain paths for the Arrival Mode widget mark.
stock = SOURCE / "vectorstock_64195475/vectorstock_64195475/vectorstock_64195475.svg"
mountain = ET.parse(stock).getroot().find(NS + "g")
paths = "\n".join(
    f'        <path android:fillColor="#293F35" android:pathData="{element.attrib["d"]}" />'
    for element in mountain.findall(NS + "path")
)
sun = mountain.find(NS + "circle")
cx, cy, radius = (float(sun.attrib[key]) for key in ("cx", "cy", "r"))
sun_path = f"M{cx-radius},{cy}a{radius},{radius} 0,1 0,{radius*2},0a{radius},{radius} 0,1 0,{-radius*2},0"
for filename, viewport, scale, tx, ty in (
    (ROOT / "feature/kits/src/main/res/drawable/ic_arrival_entry.xml", 48, .014, 3, 1),
):
    backing = (
        '    <path android:fillColor="#FFF9ED" android:pathData="M24,1a23,23 0,1 0,0,46a23,23 0,1 0,0,-46" />\n'
        if viewport == 48 else ""
    )

    filename.write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{viewport}dp" android:height="{viewport}dp"\n'
        f'    android:viewportWidth="{viewport}" android:viewportHeight="{viewport}">\n'
        f'{backing}    <group android:scaleX="{scale}" android:scaleY="{scale}" '
        f'android:translateX="{tx}" android:translateY="{ty}">\n'
        f'        <path android:fillColor="#EBA15B" android:pathData="{sun_path}" />\n'
        f'{paths}\n    </group>\n</vector>\n'
    )

# Keep subsequent artwork regeneration aligned with the ticket launcher choice.
subprocess.run([sys.executable, str(ROOT / "scripts/prepare_launcher_icon.py")], check=True)
