# Resource Packs

Every feather on the HUD is drawn from grayscale sprites, tinted with two colors: one for the body and one for the outline. Each state picks a feather shape (a variant). For cold, it also picks an overlay, drawn over the whole row. With one JSON file, a resource pack can change the colors, the shape and the overlay of every state. It can also give a state sprites of its own.

## Recoloring

Add `assets/feathers_of_fatigue/feather_styles.json` to your pack:

```json
{
  "styles": {
    "feathers_of_fatigue:cold": { "border": "#1C4652", "overlay": "none" },
    "feathers_of_fatigue:hot": { "variant": "feathers_of_fatigue:glint", "overlay_color": "#FFB000" },
    "feathers_of_fatigue:empty": { "body": "#80282828" }
  }
}
```

Each entry changes only the fields that it names. The other fields keep the values of Feathers of Fatigue. If several packs have the file, the higher pack wins, field by field. F3+T reads the file again.

| Field | What it is |
|---|---|
| `body` | The color of the feather. Colors are `#RRGGBB`, or `#AARRGGBB` for see-through colors. |
| `border` | The color of the outline. Alpha `00` draws no outline. |
| `variant` | The feather shape, see below. |
| `overlay` | Drawn over the whole row, see below. `none` removes it. |
| `overlay_color`, `overlay_accent` | The two colors of the overlay. |
| `sprites` | A texture of your own, see below. |

| Style | What it colors | Body | Border | Variant | Overlay |
|---|---|---|---|---|---|
| `feathers_of_fatigue:green` | Your feathers, `feather_color = GREEN` | `#00B53A` | `#000000` | feather | |
| `feathers_of_fatigue:blue` | Your feathers, `feather_color = BLUE` | `#22A5F0` | `#000000` | feather | |
| `feathers_of_fatigue:white` | Your feathers, `feather_color = WHITE` | `#F2F2F2` | `#3C3C3C` | feather | |
| `feathers_of_fatigue:cold` | Your feathers while Cold | `#7DEFFF` | `#000000` | crystal | frost (`#8DC8FE`, `#FFFFFF`) |
| `feathers_of_fatigue:hot` | Your feathers while Hot | `#FF870C` | `#000000` | feather | none |
| `feathers_of_fatigue:energized` | Your feathers while Energized | `#FBEE2B` | `#000000` | glint | |
| `feathers_of_fatigue:momentum` | Your feathers with Momentum | `#00B192` | `#000000` | crystal | |
| `feathers_of_fatigue:strain` | strain feathers | `#FF0E0C` | `#940000` | strained | |
| `feathers_of_fatigue:endurance` | Bonus (Endurance) rows | `#D4AF37` | `#000000` | feather | |
| `feathers_of_fatigue:armor` | Weight with no color of its own | `#B8B9C4` | none | plain | |
| `feathers_of_fatigue:empty` | The empty slots | `#282828` | `#000000` | feather | |
| `feathers_of_fatigue:exhausted` | The empty slots while exhausted | `#281616` | `#000000` | feather | |

Rows after the first use deeper shades of the body color. The empty slots use the outline of their variant and the fill in row 0. Armor pieces and mounts use the plain feather, in the colors of their own textures, with an outline in the complementary color. They have no style to change. You can change styles from other mods the same way, by their ids.

## Variants and overlays

| Variant | Looks like | Used by |
|---|---|---|
| `feathers_of_fatigue:feather` | The striped feather | Your color, heat, endurance, mounts, armor pieces |
| `feathers_of_fatigue:crystal` | Ice, lighter at the top | Cold, momentum |
| `feathers_of_fatigue:glint` | A longer highlight and a fading trail | Energized |
| `feathers_of_fatigue:strained` | One deep stripe and a lighter edge | strain |
| `feathers_of_fatigue:plain` | No stripes, shaded along the edge | Weight with no color |

| Overlay | Looks like | Used by |
|---|---|---|
| `feathers_of_fatigue:frost` | Frost with icicles. Accent: the white rim | Cold |
| `feathers_of_fatigue:heat` | Flames. Accent: the red tongues | Not used by default. Available to styles and packs |

Other mods can add variants and overlays of their own. Name them by their ids.

## The sprite sheet

`feathers_of_fatigue:textures/gui/icons.png` is 56x72. It has 6 columns and 8 rows of 9x9 cells from the top-left corner. Two transparent columns on the right make both sizes multiples of 8.

- Bodies and overlays are grayscale, with the shade kept as value.
- Outlines and the empty fill are white.
- Shines are white with their own alpha. They are drawn as they are, over the body.

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

To change every feather at once, replace `icons.png`, and keep it 56x72. To change only one state, point its `sprites` at a texture of your own with the same layout and size (56x72). The variant and overlay rows of the style then come from that texture.

```json
{
  "styles": {
    "feathers_of_fatigue:cold": { "body": "#FFFFFF", "border": "#FFFFFF", "sprites": "mypack:textures/gui/frosty_feathers.png" }
  }
}
```

The colors multiply the sprites. Draw in grayscale to keep the tint. To show your colors as they are, draw in full color and set a white body and border.
