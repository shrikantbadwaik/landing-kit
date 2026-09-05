"""Use the extracted ticket doodle as the adaptive Android launcher foreground."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "assets/journal-doodles/ticket.svg"
DEST = ROOT / "app/src/main/res/drawable/ic_launcher_foreground.xml"
NS = "{http://www.w3.org/2000/svg}"

svg = ET.parse(SOURCE).getroot()
left, top, width, height = map(float, svg.attrib["viewBox"].split())
# Keep the entire rough outline inside the adaptive icon's central safe area.
scale = 58 / width
tx = (108 - width * scale) / 2 - left * scale
ty = (108 - height * scale) / 2 - top * scale
paths = []
for element in svg[-1].iter():
    if element.tag == NS + "path":
        data = element.attrib["d"]
    elif element.tag == NS + "polygon":
        coordinates = re.findall(r"-?\d+(?:\.\d+)?", element.attrib["points"])
        data = "M" + " ".join(coordinates) + "Z"
    elif element.tag == NS + "g":
        continue
    else:
        raise ValueError(f"Unsupported ticket shape: {element.tag}")
    paths.append(f'        <path android:fillColor="#293F35" android:pathData="{data}" />')

DEST.write_text(
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '    android:width="108dp" android:height="108dp"\n'
    '    android:viewportWidth="108" android:viewportHeight="108">\n'
    f'    <group android:scaleX="{scale:.9f}" android:scaleY="{scale:.9f}" '
    f'android:translateX="{tx:.9f}" android:translateY="{ty:.9f}">\n'
    + "\n".join(paths) + "\n    </group>\n</vector>\n"
)
print(f"Ticket launcher: {len(paths)} original doodle paths")
