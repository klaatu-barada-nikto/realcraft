"""Generate a house.json voxel model template for the Realcraft mod.

Usage:
    python generate_house.py

The output JSON can be served over HTTP and loaded in-game via:
    /buildmodel http://<host>/house.json
"""
import json
import os

WIDTH = 7
DEPTH = 7
WALL_HEIGHT = 4

blocks = []


def add(x, y, z, block_id):
    blocks.append([block_id, x, y, z])


# Floor (oak planks)
for x in range(WIDTH):
    for z in range(DEPTH):
        add(x, 0, z, "minecraft:oak_planks")

# Walls (stone bricks) with door and windows
for y in range(1, WALL_HEIGHT + 1):
    for x in range(WIDTH):
        for z in range(DEPTH):
            if not (x == 0 or x == WIDTH - 1 or z == 0 or z == DEPTH - 1):
                continue
            if z == 0 and x == WIDTH // 2 and y in (1, 2):
                add(x, y, z, "minecraft:oak_door")
                continue
            if (z == 0 or z == DEPTH - 1) and x in (1, WIDTH - 2) and y in (2, 3):
                add(x, y, z, "minecraft:glass")
                continue
            add(x, y, z, "minecraft:stone_bricks")

# Roof with overhang (spruce planks)
for x in range(-1, WIDTH + 1):
    for z in range(-1, DEPTH + 1):
        add(x, WALL_HEIGHT + 1, z, "minecraft:spruce_planks")

# Lantern hanging from the roof
add(WIDTH // 2, WALL_HEIGHT, DEPTH // 2, "minecraft:lantern")

out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "house.json")
with open(out, "w", encoding="utf-8") as f:
    json.dump(blocks, f, ensure_ascii=False, indent=2)

print(f"generated {len(blocks)} blocks -> {out}")