"""Generate the WebP files used by the portal; preserve original photos in frontend/img.

Run from any directory with Python and Pillow installed.
"""
from pathlib import Path
from PIL import Image, ImageOps


image_dir = Path(__file__).resolve().parent.parent / "frontend" / "img"
originals = ["DSC_2630.jpg", "DSC_2744.jpg", "DSC_0830.JPG", "DSC_4070.jpg"]

if __name__ == "__main__":
    for name in originals:
        source = image_dir / name
        with Image.open(source) as original:
            image = ImageOps.exif_transpose(original).convert("RGB")
            image.thumbnail((1920, 1440), Image.LANCZOS)
            output = source.with_suffix(".webp")
            image.save(output, "WEBP", quality=84, method=6)
        print("{}: {} -> {} bytes".format(name, source.stat().st_size, output.stat().st_size))
