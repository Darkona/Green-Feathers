#!/usr/bin/env python3
"""Regenerates the brewing recipes of Feathers of Fatigue's potions, Minecraft 26.3 format (minecraft:brewing).

Output (rewritten from scratch, stale files are deleted):
  src/main/resources/data/feathers_of_fatigue/recipe/brewing/

Run:  python3 scripts/brewing/generate.py          (rewrite files)
      python3 scripts/brewing/generate.py --check  (fail if the files on disk differ)

Minecraft 26.3 has no brewing registration in code: each mix is a recipe for one container, and each container
change (gunpowder, dragon's breath) is a recipe for one potion, like vanilla's data/minecraft/recipe/brewing. Names
follow vanilla: <input container>_<input potion>_<reagent>.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/data/feathers_of_fatigue/recipe/brewing"

CONTAINERS = ["potion", "splash_potion", "lingering_potion"]
# input potion, reagent, output potion
MIXES = [
    ("minecraft:awkward", "minecraft:snowball", "feathers_of_fatigue:cold_potion"),
    ("feathers_of_fatigue:cold_potion", "minecraft:glowstone_dust", "feathers_of_fatigue:strong_cold_potion"),
    ("minecraft:awkward", "minecraft:magma_block", "feathers_of_fatigue:hot_potion"),
    ("feathers_of_fatigue:hot_potion", "minecraft:glowstone_dust", "feathers_of_fatigue:strong_hot_potion"),
    # Cooling protects from Heat and Fatigue.
    ("minecraft:awkward", "minecraft:packed_ice", "feathers_of_fatigue:cooling_potion"),
    ("feathers_of_fatigue:cooling_potion", "minecraft:redstone", "feathers_of_fatigue:long_cooling_potion"),
    ("minecraft:awkward", "minecraft:feather", "feathers_of_fatigue:endurance_potion"),
    ("feathers_of_fatigue:endurance_potion", "minecraft:redstone", "feathers_of_fatigue:long_endurance_potion"),
    ("feathers_of_fatigue:endurance_potion", "minecraft:glowstone_dust", "feathers_of_fatigue:strong_endurance_potion"),
    ("minecraft:awkward", "minecraft:basalt", "feathers_of_fatigue:momentum_potion"),
    ("feathers_of_fatigue:momentum_potion", "minecraft:redstone", "feathers_of_fatigue:long_momentum_potion"),
    ("feathers_of_fatigue:momentum_potion", "minecraft:glowstone_dust", "feathers_of_fatigue:strong_momentum_potion"),
    ("minecraft:awkward", "minecraft:raw_copper", "feathers_of_fatigue:energized_potion"),
    ("feathers_of_fatigue:energized_potion", "minecraft:redstone", "feathers_of_fatigue:long_energized_potion"),
    ("feathers_of_fatigue:energized_potion", "minecraft:glowstone_dust", "feathers_of_fatigue:strong_energized_potion"),
]
# Every potion the mod registers (ModPotions): each changes container like vanilla's.
POTIONS = sorted({out for _, _, out in MIXES})
# input container, reagent, output container
CONVERSIONS = [
    ("potion", "minecraft:gunpowder", "splash_potion"),
    ("splash_potion", "minecraft:dragon_breath", "lingering_potion"),
]


def path(id_):
    return id_.split(":", 1)[1]


def recipe(container, potion, reagent, out_container, out_potion):
    return {
        "type": "minecraft:brewing",
        "input": {"item": "minecraft:" + container, "potion_contents": {"potions": potion}},
        "reagent": {"item": reagent},
        "output": {"id": "minecraft:" + out_container, "components": {"minecraft:potion_contents": {"potion": out_potion}}},
    }


def build():
    files = {}
    for container in CONTAINERS:
        for potion, reagent, out in MIXES:
            files["%s_%s_%s.json" % (container, path(potion), path(reagent))] = recipe(container, potion, reagent, container, out)
    for potion in POTIONS:
        for container, reagent, out_container in CONVERSIONS:
            files["%s_%s_%s.json" % (container, path(potion), path(reagent))] = recipe(container, potion, reagent, out_container, potion)
    return {name: json.dumps(data, indent=2) + "\n" for name, data in files.items()}


def main():
    check = "--check" in sys.argv[1:]
    files = build()
    existing = {p.name for p in OUT.glob("*.json")} if OUT.is_dir() else set()
    diffs = [n for n in sorted(existing - files.keys())]
    diffs += [n for n, text in sorted(files.items()) if not (OUT / n).is_file() or (OUT / n).read_text(encoding="utf-8") != text]
    if check:
        for n in diffs:
            print("differs: " + n)
        return 1 if diffs else 0
    OUT.mkdir(parents=True, exist_ok=True)
    for n in existing - files.keys():
        (OUT / n).unlink()
    for n, text in files.items():
        (OUT / n).write_text(text, encoding="utf-8")
    print("%d brewing recipes (%d changed)" % (len(files), len(diffs)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
