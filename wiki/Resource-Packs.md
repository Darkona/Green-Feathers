# Resource Packs

Every feather on the HUD is drawn from grayscale sprites tinted with two colors: one for the body and one for the outline. Each state picks a feather shape (a variant) and, for cold, an overlay drawn over the whole row. A resource pack can change the colors, the shape and the overlay of every state with one JSON file, or give a state sprites of its own.

## Recoloring

Add `assets/greenfeathers/feather_styles.json` to your pack:

```json
{
  "styles": {
    "greenfeathers:cold": { "border": "#1C4652", "overlay": "none" },
    "greenfeathers:hot": { "variant": "greenfeathers:glint", "overlay_color": "#FFB000" },
    "greenfeathers:empty": { "body": "#80282828" }
  }
}
```

Each entry changes only the fields it names; the rest keep Green Feathers' values. If several packs have the file, the higher pack wins field by field. The file is read again on F3+T.

| Field | What it is |
|---|---|
| `body` | The feather's color. Colors are `#RRGGBB`, or `#AARRGGBB` for see-through ones. |
| `border` | The outline's color. Alpha `00` draws no outline. |
| `variant` | The feather shape, see below. |
| `overlay` | Drawn over the whole row, see below. `none` removes it. |
| `overlay_color`, `overlay_accent` | The overlay's two colors. |
| `sprites` | A texture of your own, see below. |

| Style | What it colors | Body | Border | Variant | Overlay |
|---|---|---|---|---|---|
| `greenfeathers:green` | Your feathers, `feather_color = GREEN` | `#00B53A` | `#000000` | feather | |
| `greenfeathers:blue` | Your feathers, `feather_color = BLUE` | `#22A5F0` | `#000000` | feather | |
| `greenfeathers:white` | Your feathers, `feather_color = WHITE` | `#F2F2F2` | `#3C3C3C` | feather | |
| `greenfeathers:cold` | Your feathers while Cold | `#7DEFFF` | `#000000` | crystal | frost (`#8DC8FE`, `#FFFFFF`) |
| `greenfeathers:hot` | Your feathers while Hot | `#FF870C` | `#000000` | feather | none |
| `greenfeathers:energized` | Your feathers while Energized | `#FBEE2B` | `#000000` | glint | |
| `greenfeathers:momentum` | Your feathers with Momentum | `#00B192` | `#000000` | crystal | |
| `greenfeathers:strain` | strain feathers | `#FF0E0C` | `#940000` | strained | |
| `greenfeathers:endurance` | Bonus (Endurance) rows | `#D4AF37` | `#000000` | feather | |
| `greenfeathers:armor` | Weight with no color of its own | `#B8B9C4` | none | plain | |
| `greenfeathers:empty` | The empty slots | `#282828` | `#000000` | feather | |
| `greenfeathers:exhausted` | The empty slots while exhausted | `#281616` | `#000000` | feather | |

Rows beyond the first use deeper shades of the body color. The empty slots use their variant's outline and the fill in row 0. Armor pieces and mounts take the colors of their own textures, outlined in the complementary color, in the plain feather; they have no style to change. Styles added by other mods can be changed the same way, by their ids.

## Variants and overlays

| Variant | Looks like | Used by |
|---|---|---|
| `greenfeathers:feather` | The striped feather | Your color, heat, endurance, mounts, armor pieces |
| `greenfeathers:crystal` | Ice, lighter at the top | Cold, momentum |
| `greenfeathers:glint` | A longer highlight and a fading trail | Energized |
| `greenfeathers:strained` | One deep stripe and a lighter edge | strain |
| `greenfeathers:plain` | No stripes, shaded along the edge | Weight with no color |

| Overlay | Looks like | Used by |
|---|---|---|
| `greenfeathers:frost` | Frost with icicles; accent: the white rim | Cold |
| `greenfeathers:heat` | Flames; accent: the red tongues | Not used by default; available to styles and packs |

Other mods can add variants and overlays of their own; name them by their ids.

## The sprite sheet

`greenfeathers:textures/gui/icons.png` is 56x72: 6 columns and 8 rows of 9x9 cells from the top-left corner, and 2 transparent columns on the right so that both sizes are multiples of 8. Bodies and overlays are grayscale, where the shade is kept as value; outlines and the empty fill are white; shines are white with their own alpha and are drawn as they are, over the body.

| Row (y) | Cells, left to right (x = 0, 9, 18, 27, 36, 45) |
|---|---|
| 0 (0) | Empty slot fill, regeneration flash |
| 1 (9) | feather: full body, half body, full outline, half outline, full shine, half shine |
| 2 (18) | crystal: the same six cells |
| 3 (27) | glint: the same six cells |
| 4 (36) | strained: the same six cells |
| 5 (45) | plain: the same six cells |
| 6 (54) | frost: primary, accent |
| 7 (63) | heat: primary, accent |

Replace `icons.png` to change every feather at once; keep it 56x72. To change one state only, point its `sprites` at a texture of your own with the same layout and size (56x72): the style's variant and overlay rows are then read from it.

```json
{
  "styles": {
    "greenfeathers:cold": { "body": "#FFFFFF", "border": "#FFFFFF", "sprites": "mypack:textures/gui/frosty_feathers.png" }
  }
}
```

The colors multiply the sprites, so draw in grayscale to keep the tint, or in full color with a white body and border to have them drawn as they are.
