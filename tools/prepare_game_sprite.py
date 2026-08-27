"""Prepare a generated cutout as a square, transparent game sprite."""

from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
from PIL import Image

from normalize_character_sheet import remove_connected_light_background


def prepare(source_path: Path, destination_path: Path, size: int, margin: int) -> None:
    source = Image.open(source_path)
    if source.mode != "RGBA":
        source = remove_connected_light_background(source)
    else:
        source = source.convert("RGBA")

    alpha = np.asarray(source.getchannel("A"))
    content_mask = Image.fromarray((alpha >= 5).astype(np.uint8) * 255, "L")
    bounds = content_mask.getbbox()
    if bounds is None:
        raise ValueError(f"{source_path.name} contains no visible sprite")

    sprite = source.crop(bounds)
    maximum = size - margin * 2
    scale = min(maximum / sprite.width, maximum / sprite.height)
    sprite = sprite.resize(
        (max(1, round(sprite.width * scale)), max(1, round(sprite.height * scale))),
        Image.Resampling.LANCZOS,
    )

    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    x = (size - sprite.width) // 2
    y = size - margin - sprite.height
    canvas.alpha_composite(sprite, (x, y))
    destination_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(destination_path, optimize=True)
    print(
        f"{destination_path.name}: source={source.size} crop={bounds} "
        f"packed=({x},{y},{sprite.width},{sprite.height}) alpha={canvas.getchannel('A').getextrema()}"
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("destination", type=Path)
    parser.add_argument("--size", type=int, default=1024)
    parser.add_argument("--margin", type=int, default=48)
    args = parser.parse_args()
    prepare(args.source, args.destination, args.size, args.margin)


if __name__ == "__main__":
    main()
