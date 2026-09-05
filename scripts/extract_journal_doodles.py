"""Extract individual transparent doodles from the supplied travel contact sheet.

Run with an optional path to vectorstock_64372227.svg. Requires rsvg-convert
and ImageMagick. Source groups 13–15 are lettering/texture and are excluded.
The existing journal_doodles suitcase is reused by the app.
"""
from copy import deepcopy
from pathlib import Path
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
import re

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path.home() / 'Downloads/vectorstock_64372227/vectorstock_64372227/vectorstock_64372227.svg'
)
DEST = ROOT / 'feature/kits/src/main/res/drawable-nodpi'
VECTORS = ROOT / 'assets/journal-doodles'
VECTORS.mkdir(parents=True, exist_ok=True)
NAMES = ['glasses', 'plane', 'globe', 'passport', 'camera', 'pin', 'map',
         'ticket', 'photos', 'signpost', 'compass', 'lifebuoy', 'suitcase']
source = ET.parse(SOURCE).getroot()
with tempfile.TemporaryDirectory() as temp:
    for index, name in enumerate(NAMES):
        svg = ET.Element(source.tag, {'viewBox': source.attrib['viewBox']})
        svg.append(deepcopy(source[0]))  # Original shared style definitions.
        svg.append(deepcopy(source[2][index]))
        raw = Path(temp) / f'{name}.svg'
        ET.ElementTree(svg).write(raw)
        png = raw.with_suffix('.png')
        subprocess.run(['rsvg-convert', '-w', '3900', '-o', str(png), str(raw)], check=True)
        bounds = subprocess.check_output(
            ['magick', str(png), '-format', '%@', 'info:'], text=True,
        ).strip()
        width, height, left, top = re.fullmatch(r'(\d+)x(\d+)\+(\d+)\+(\d+)', bounds).groups()
        svg.set('viewBox', f'{left} {top} {width} {height}')
        ET.ElementTree(svg).write(VECTORS / f'{name}.svg', encoding='utf-8', xml_declaration=True)
        if name == 'suitcase':
            continue  # Preserve the existing Android suitcase treatment.
        dest = DEST / f'journal_doodle_{name}.webp'
        subprocess.run(['magick', str(png), '-trim', '+repage', '-resize', '420x420',
                        '-define', 'webp:lossless=true', str(dest)], check=True)
        print(name, dest.stat().st_size)
