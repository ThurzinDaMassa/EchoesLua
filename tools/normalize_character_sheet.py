"""Normalize generated 4x3 character sheets for the game asset pipeline."""

from __future__ import annotations

import argparse
from collections import deque
from pathlib import Path

from PIL import Image


def remove_connected_light_background(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size
    background = bytearray(width * height)
    queue: deque[tuple[int, int]] = deque()

    def looks_like_checker(x: int, y: int) -> bool:
        red, green, blue, _ = pixels[x, y]
        return min(red, green, blue) >= 220 and max(red, green, blue) - min(red, green, blue) <= 18

    def enqueue(x: int, y: int) -> None:
        index = y * width + x
        if not background[index] and looks_like_checker(x, y):
            background[index] = 1
            queue.append((x, y))

    for x in range(width):
        enqueue(x, 0)
        enqueue(x, height - 1)
    for y in range(height):
        enqueue(0, y)
        enqueue(width - 1, y)

    while queue:
        x, y = queue.popleft()
        if x > 0:
            enqueue(x - 1, y)
        if x + 1 < width:
            enqueue(x + 1, y)
        if y > 0:
            enqueue(x, y - 1)
        if y + 1 < height:
            enqueue(x, y + 1)

    for y in range(height):
        for x in range(width):
            if background[y * width + x]:
                red, green, blue, _ = pixels[x, y]
                pixels[x, y] = (red, green, blue, 0)
    return rgba


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("path", type=Path)
    parser.add_argument("--remove-light-background", action="store_true")
    parser.add_argument("--width", type=int, default=1536)
    parser.add_argument("--height", type=int, default=1024)
    args = parser.parse_args()

    image = Image.open(args.path)
    if args.remove_light_background:
        image = remove_connected_light_background(image)
    else:
        image = image.convert("RGBA")
    if image.size != (args.width, args.height):
        image = image.resize((args.width, args.height), Image.Resampling.LANCZOS)
    image.save(args.path)


if __name__ == "__main__":
    main()
