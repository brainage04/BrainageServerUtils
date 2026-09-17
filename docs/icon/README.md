# BrainageServerUtils icon

## What this is

The mod's icon: `icon.png` — 32x32 RGBA PNG, 597 bytes,
sha256 `31ebdfd8574226763e3880a72f9e38f9bccf8c1ead71480ccd74a0011d7b5680`.

Two layers: behind, a 16x16 historical **vanilla procedural gear frame** enlarged 2x; in
front, the author's own **player head face** enlarged 2x and centred.

## How it was made

**Method: generated pixel art from vanilla assets.** This is *not* an in-game screenshot and
*not* a Blender render. It is a deterministic offline composition of official Minecraft
textures with Pillow — no hand-drawn approximation, and the only pixel operations used are
integer nearest-neighbour enlargement (2x), alpha compositing, and (elsewhere in the same
script) a 90° transpose.

| | |
|---|---|
| Tool | Pillow 12.3.0 on Python 3.13 (`provenance/render.py`, function block `gear`/`skinface`) |
| Gear source | **Mojang Alpha a1.0.4 `client.jar`**, retained historical `misc/gear.png` (32x32) and `misc/gearmiddle.png` (16x16) — pinned by sha256 in `provenance.json` (`a1cf16ff…`, `b7188fd2…`) |
| Gear algorithm | the historical frame-0 coordinate sampling: for each of the 16x16 output pixels, sample `gearmiddle`; where its alpha ≤ 128, sample `gear` at `((x/15 - 0.5) * 31 + 16, (y/15 - 0.5) * 31 + 16)` and use it; output alpha is forced to fully opaque or fully transparent at the 128 threshold. Frame 0 is the un-rotated frame, so no rotation is applied |
| Gear references | `MC-TextureGen` `GearRotationFramesGenerator` (github.com/NeRdTheNed/MC-TextureGen) and minecraft.wiki *Procedural animated texture generation/Gears*, recorded in `derivations.json` |
| Face source | the author's **own supplied skin**, `sources/supplied-skin.png` (sha256 `e9ebbeec…`), provided locally by the author; it is never fetched by username or UUID |
| Face geometry | flat face UV `(8,8)-(16,16)` composited with the hat layer's front UV `(40,8)-(48,16)` (both flat layers, as `derivations.json` records), then doubled from 8x8 to 16x16 |
| Composition | 32x32 canvas; gear frame doubled to 32x32 at `(0,0)`; face doubled and centred at `(8,8)` on top |

Everything is integer, deterministic and replayable: running the script again produces the
same bytes. Verified while creating this provenance: `python3 render.py` in a clean directory
holding the 17 shipped source textures reproduces `icon.png` byte for byte.

## Provenance files

| Path | What it is |
|---|---|
| `render.py` | **The script that produced this icon.** Deterministic Pillow composition of all round-3 pixel icons (this icon's block is the one that builds the gear frame and composites the skin face) |
| `provenance.json` | The pinned source manifest: for every texture, the exact Mojang URL + archive member + sha256, plus the supplied skin's path and sha256 |
| `derivations.json` | The derivation record: Pillow version, the exact runtime command, the gear algorithm and its references, and the per-texture crop/UV notes |
| `sources/` | The 17 source textures `render.py` reads (official Java 1.21.4 client textures, the two Alpha gear textures, and the supplied skin), each hash-verified against `provenance.json` |
| `manifest.json` | The round-3 delivery record for all pixel icons, including this one's label and method |
| `blockers.json` | The pixel session's blocker list (empty for this icon) |
| `acquire_sources.py` | Re-fetch/verify tool for `provenance.json` (see Notes for its known defect; **not needed**, every source is shipped) |

Excluded on purpose: other icons' output PNGs from the same script, the NMSR head-render helper
(`render_service.py`, `service-provenance.json`) which belongs to a different mod's icon, and
`__pycache__`.

## How to regenerate

```sh
cd provenance
PYTHONPATH=/nix/store/4v9j9wbzyhrlx9980ygbr812313mazy0-python3.13-pillow-12.3.0/lib/python3.13/site-packages \
  python3 render.py
```

This rewrites `brainage-server-utils.png` (and the other pixel icons the script contains) and
`manifest.json`. It needs no network: every texture `render.py` reads is in `sources/`, and each
one was verified against its pinned sha256 when this provenance was assembled.

To re-verify the pins instead:

```sh
python3 acquire_sources.py     # see the Note about the duplicate potion.png entry
```

## Notes

* `acquire_sources.py` has a defect inherited from the round-3 session: `provenance.json` lists
  `potion.png` twice (once as `item/potion.png`, once as `gui/sprites/container/slot/potion.png`),
  so on the second entry the already-written file fails the checksum comparison and the script
  raises after writing 13 of its 17 files. That copy of `potion.png` is not used by this icon;
  it is shipped here (verified against the `item/potion.png` pin) so `render.py` can run.
* The supplied skin is the author's own and is included as a plain texture; no third-party head
  renderer and no username lookup was used for this icon. (The NMSR head render belongs to a
  different mod's icon and is deliberately not part of this provenance.)
* The gear frame is the *historical* 16x16 frame-zero mapping, not a newly drawn approximation:
  the two source textures are the original Mojang files and the sampling rule is the historical
  one, so the result is the same image the old client generated.
* All colours and pixels come from those sources; nothing here is upscaled with interpolation,
  so the icon is crisp at 32x32 and at integer multiples of it.

## Working-tree note

The round-3 working tree that produced this icon was cleaned up after integration. Every file needed to regenerate the icon was copied into `provenance/`; the copies live under `provenance/from-round3/` when they came from the working tree. Any remaining `round3/...` mention records where something came from, not a path that still exists.
