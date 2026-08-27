"""Repack character atlases into clean, uniformly aligned 4x3 cells.

The generated source artwork sometimes crosses a cell boundary or leaves a
detached fragment in a neighbouring frame.  This tool keeps the largest
connected opaque silhouette in each cell, scales every pose consistently and
places it on a shared centre/baseline with a transparent safety gutter.
"""

from __future__ import annotations

import argparse
from collections import deque
from pathlib import Path

import numpy as np
from PIL import Image


COLS = 4
ROWS = 3
FRAME_WIDTH = 384
FRAME_HEIGHT = 341
ATLAS_SIZE = (1536, 1024)
TARGET_IDLE_HEIGHT = 286
MAX_CONTENT_WIDTH = 344
MAX_CONTENT_HEIGHT = 313
CENTER_X = FRAME_WIDTH // 2
BASELINE_Y = 326
ALPHA_THRESHOLD = 5


def largest_component_mask(alpha: np.ndarray) -> tuple[np.ndarray, tuple[int, int, int, int], int]:
    opaque = alpha >= ALPHA_THRESHOLD
    height, width = opaque.shape
    visited = np.zeros_like(opaque, dtype=bool)
    best_pixels: list[tuple[int, int]] = []

    for start_y, start_x in zip(*np.nonzero(opaque & ~visited)):
        if visited[start_y, start_x]:
            continue
        queue = deque([(int(start_x), int(start_y))])
        visited[start_y, start_x] = True
        pixels: list[tuple[int, int]] = []
        while queue:
            x, y = queue.popleft()
            pixels.append((x, y))
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1),
                           (x - 1, y - 1), (x + 1, y - 1),
                           (x - 1, y + 1), (x + 1, y + 1)):
                if 0 <= nx < width and 0 <= ny < height and opaque[ny, nx] and not visited[ny, nx]:
                    visited[ny, nx] = True
                    queue.append((nx, ny))
        if len(pixels) > len(best_pixels):
            best_pixels = pixels

    if not best_pixels:
        raise ValueError("animation cell has no opaque sprite pixels")

    mask = np.zeros_like(alpha, dtype=np.uint8)
    xs = [point[0] for point in best_pixels]
    ys = [point[1] for point in best_pixels]
    for x, y in best_pixels:
        mask[y, x] = 255
    return mask, (min(xs), min(ys), max(xs) + 1, max(ys) + 1), len(best_pixels)


def extract_frames(image: Image.Image) -> list[tuple[Image.Image, tuple[int, int, int, int], int]]:
    frames = []
    rgba = image.convert("RGBA")
    for row in range(ROWS):
        for column in range(COLS):
            left = column * FRAME_WIDTH
            top = row * FRAME_HEIGHT
            cell = rgba.crop((left, top, left + FRAME_WIDTH, top + FRAME_HEIGHT))
            array = np.asarray(cell).copy()
            mask, bbox, area = largest_component_mask(array[:, :, 3])
            array[:, :, 3] = np.minimum(array[:, :, 3], mask)
            frames.append((Image.fromarray(array, "RGBA").crop(bbox), bbox, area))
    return frames


def repack(path: Path, flip_horizontal: bool, write: bool) -> None:
    source = Image.open(path)
    if source.size != ATLAS_SIZE:
        raise ValueError(f"{path.name}: expected {ATLAS_SIZE}, found {source.size}")
    frames = extract_frames(source)
    sizes = [frame.size for frame, _, _ in frames]
    idle_height = sum(height for _, height in sizes[:4]) / 4.0
    desired_scale = TARGET_IDLE_HEIGHT / idle_height
    fit_scale = min(
        MAX_CONTENT_WIDTH / max(width for width, _ in sizes),
        MAX_CONTENT_HEIGHT / max(height for _, height in sizes),
    )
    scale = min(desired_scale, fit_scale)

    atlas = Image.new("RGBA", ATLAS_SIZE, (0, 0, 0, 0))
    print(f"{path.name}: idle={idle_height:.1f}px scale={scale:.4f} flip={flip_horizontal}")
    for index, (frame, old_bbox, area) in enumerate(frames):
        width = max(1, round(frame.width * scale))
        height = max(1, round(frame.height * scale))
        frame = frame.resize((width, height), Image.Resampling.LANCZOS)
        if flip_horizontal:
            frame = frame.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        column = index % COLS
        row = index // COLS
        x = column * FRAME_WIDTH + CENTER_X - width // 2
        y = row * FRAME_HEIGHT + BASELINE_Y - height
        atlas.alpha_composite(frame, (x, y))
        print(
            f"  frame {index:02d}: source={old_bbox} area={area} "
            f"packed=({x - column * FRAME_WIDTH},{y - row * FRAME_HEIGHT},{width},{height})"
        )

    if write:
        atlas.save(path, optimize=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("paths", type=Path, nargs="+")
    parser.add_argument("--write", action="store_true")
    parser.add_argument("--flip", type=str, default="", help="comma-separated file stems to flip")
    args = parser.parse_args()
    flip_stems = {item.strip() for item in args.flip.split(",") if item.strip()}
    for path in args.paths:
        repack(path, path.stem in flip_stems, args.write)


if __name__ == "__main__":
    main()
