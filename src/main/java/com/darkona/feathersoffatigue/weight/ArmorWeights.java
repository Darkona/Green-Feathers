package com.darkona.feathersoffatigue.weight;

import com.darkona.feathersoffatigue.Feathers;
import com.darkona.feathersoffatigue.api.WeightSource;
import com.darkona.feathersoffatigue.api.event.ArmorWeightEvent;
import com.darkona.feathersoffatigue.api.registry.FeathersAttributes;
import com.darkona.feathersoffatigue.api.registry.FeathersDataMaps;
import com.darkona.feathersoffatigue.api.registry.FeathersEnchantments;
import com.darkona.feathersoffatigue.config.FeathersServerConfig;
import com.darkona.feathersoffatigue.core.Extensions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Armor weight: how many feathers worn armor makes unusable.
 * <p>
 * A piece's base weight comes from the most specific source that has an answer: a config rule for the item, a
 * config rule for one of its tags, the {@link FeathersDataMaps#ARMOR_WEIGHT} data map, a config rule for its armor
 * material and piece, a config rule for its armor material, and for other armor its defense points times
 * {@code unlisted_armor_weight_per_defense}. Lightweight removes a share per level; Curse of Heaviness doubles it.
 * The worn total plus every {@link WeightSource} goes through {@link ArmorWeightEvent} and
 * is then scaled by the armor weight multiplier attribute, and rounded once.
 */
public final class ArmorWeights {

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final int UNRESOLVED = -1;

    private record TagRule(TagKey<Item> tag, int weight) {}

    /* Parsed config rules, and the resolved base weight per item. Guarded by the class lock: tooltips read them on
       the client thread while the integrated server fills them. */
    private static final Reference2IntOpenHashMap<Item> itemRules = new Reference2IntOpenHashMap<>();
    private static final List<TagRule> tagRules = new ArrayList<>();
    private static final Object2IntOpenHashMap<String> materialPieceRules = new Object2IntOpenHashMap<>();
    private static final Object2IntOpenHashMap<Identifier> materialRules = new Object2IntOpenHashMap<>();
    private static final Reference2IntOpenHashMap<Item> baseWeightCache = new Reference2IntOpenHashMap<>();
    private static boolean rulesLoaded;

    static {
        itemRules.defaultReturnValue(UNRESOLVED);
        materialPieceRules.defaultReturnValue(UNRESOLVED);
        materialRules.defaultReturnValue(UNRESOLVED);
        baseWeightCache.defaultReturnValue(UNRESOLVED);
    }

    private ArmorWeights() {}

    /**
     * Forgets parsed rules and cached weights: the config or the datapacks changed.
     */
    public static synchronized void invalidate() {
        rulesLoaded = false;
        baseWeightCache.clear();
    }

    private static void loadRules() {
        if (rulesLoaded) return;
        itemRules.clear();
        tagRules.clear();
        materialPieceRules.clear();
        materialRules.clear();

        for (String rule : FeathersServerConfig.ARMOR_WEIGHTS.get()) {
            int eq = rule.lastIndexOf('=');
            if (eq <= 0) {
                Feathers.LOGGER.warn("Armor weight rule '{}' has no '=weight' part, ignored.", rule);
                continue;
            }
            String target = rule.substring(0, eq).trim();
            if (target.isEmpty()) {
                Feathers.LOGGER.warn("Armor weight rule '{}' has nothing before '=', ignored.", rule);
                continue;
            }
            int weight;
            try {
                weight = Math.max(0, Integer.parseInt(rule.substring(eq + 1).trim()));
            } catch (NumberFormatException e) {
                Feathers.LOGGER.warn("Armor weight rule '{}' has a weight that isn't a whole number, ignored.", rule);
                continue;
            }

            switch (target.charAt(0)) {
                case '#' -> {
                    Identifier id = Identifier.tryParse(target.substring(1));
                    if (id != null) tagRules.add(new TagRule(TagKey.create(Registries.ITEM, id), weight));
                    else Feathers.LOGGER.warn("Armor weight rule '{}' has an invalid tag, ignored.", rule);
                }
                case '@' -> {
                    String material = target.substring(1);
                    int slash = material.indexOf('/');
                    Identifier id = Identifier.tryParse(slash < 0 ? material : material.substring(0, slash));
                    if (id == null) Feathers.LOGGER.warn("Armor weight rule '{}' has an invalid material, ignored.", rule);
                    else if (slash < 0) materialRules.put(id, weight);
                    else materialPieceRules.put(id + "/" + material.substring(slash + 1), weight);
                }
                default -> {
                    Identifier id = Identifier.tryParse(target);
                    if (id != null && BuiltInRegistries.ITEM.containsKey(id)) itemRules.put(BuiltInRegistries.ITEM.getValue(id), weight);
                    else Feathers.LOGGER.warn("Armor weight rule '{}' names an unknown item, ignored.", rule);
                }
            }
        }
        rulesLoaded = true;
    }

    /**
     * The weight of an item before enchantments.
     */
    public static synchronized int baseWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        int cached = baseWeightCache.getInt(item);
        if (cached != UNRESOLVED) return cached;

        int weight = resolveBaseWeight(stack);
        baseWeightCache.put(item, weight);
        return weight;
    }

    private static int resolveBaseWeight(ItemStack stack) {
        loadRules();
        Item item = stack.getItem();

        int weight = itemRules.getInt(item);
        if (weight != UNRESOLVED) return weight;

        for (TagRule rule : tagRules) {
            if (stack.is(rule.tag())) return rule.weight();
        }

        Integer mapped = stack.getItemHolder().getData(FeathersDataMaps.ARMOR_WEIGHT);
        if (mapped != null) return mapped;

        Equippable armor = armor(stack);
        if (armor != null) {
            Identifier material = armor.assetId().orElseThrow().identifier();
            weight = materialPieceRules.getInt(material + "/" + pieceName(armor.slot()));
            if (weight != UNRESOLVED) return weight;
            weight = materialRules.getInt(material);
            if (weight != UNRESOLVED) return weight;
            return (int) Math.round(defense(stack, armor.slot()) * FeathersServerConfig.UNLISTED_ARMOR_WEIGHT_PER_DEFENSE.get());
        }
        return 0;
    }

    /**
     * The equippable component of an armor piece, or null: worn in an armor slot or on a mount's body, with an
     * equipment model, whose id is the armor material ({@code minecraft:iron}). Carved pumpkins and heads have none.
     */
    public static @Nullable Equippable armor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.assetId().isPresent() && equippable.slot().isArmor() ? equippable : null;
    }

    /** The piece name in material rules: helmet, chestplate, leggings, boots, or body for a mount's armor. */
    private static String pieceName(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> "body";
        };
    }

    /** The armor points the piece adds in its slot. */
    private static double defense(ItemStack stack, EquipmentSlot slot) {
        double defense = 0;
        for (ItemAttributeModifiers.Entry entry : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.attribute().is(Attributes.ARMOR) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE
                    && entry.slot().test(slot)) {
                defense += entry.modifier().amount();
            }
        }
        return defense;
    }

    /**
     * One piece's weight with its enchantments. Fractional: totals are rounded once.
     */
    public static double pieceWeight(ItemStack stack) {
        int base = baseWeight(stack);
        if (base == 0) return 0;
        EnchantmentLevels levels = enchantmentLevels(stack.getTagEnchantments());
        double lightness = Math.max(0.0, 1.0 - levels.lightweight() * FeathersServerConfig.LIGHTWEIGHT_REDUCTION_PER_LEVEL.get());
        return base * lightness * (1 + levels.heavy());
    }

    /** A stack's Lightweight and Heavy levels, read from its enchantments component. */
    private record EnchantmentLevels(ItemEnchantments enchantments, int lightweight, int heavy) {}

    private static final EnchantmentLevels NO_LEVELS = new EnchantmentLevels(ItemEnchantments.EMPTY, 0, 0);
    /** The last component read: a tooltip asks for the same stack every frame, and the component is immutable. */
    private static volatile EnchantmentLevels lastLevels = NO_LEVELS;

    /**
     * Reads the levels straight from the component, so no registry access is needed.
     */
    private static EnchantmentLevels enchantmentLevels(ItemEnchantments enchantments) {
        if (enchantments.isEmpty()) return NO_LEVELS;
        EnchantmentLevels levels = lastLevels;
        if (levels.enchantments() == enchantments) return levels;
        int lightweight = 0;
        int heavy = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchantments.entrySet()) {
            if (e.getKey().is(FeathersEnchantments.LIGHTWEIGHT)) lightweight = e.getIntValue();
            else if (e.getKey().is(FeathersEnchantments.HEAVY)) heavy = e.getIntValue();
        }
        levels = new EnchantmentLevels(enchantments, lightweight, heavy);
        lastLevels = levels;
        return levels;
    }

    /** The head armor share in a {@link WeightSplit}. */
    public static final int HEAD = 0;
    /** The chest armor share in a {@link WeightSplit}. */
    public static final int CHEST = 1;
    /** The leg armor share in a {@link WeightSplit}. */
    public static final int LEGS = 2;
    /** The foot armor share in a {@link WeightSplit}. */
    public static final int FEET = 3;
    /** The share without a specific armor piece or colored source. */
    public static final int OTHER = 4;
    /** The number of built-in weight shares. */
    public static final int PARTS = 5;

    /**
     * The entity's weight: armor plus weight sources, after the event and the multiplier. 0 when disabled.
     */
    @SuppressWarnings("unused")
    public static int totalWeight(LivingEntity entity) {
        return totalWeight(entity, null);
    }

    /**
     * Same, and divides the total into {@code split} (when not null): each armor piece, each weight source with a
     * color of its own, and the rest, scaled like the total and rounded so they add up to it.
     */
    public static int totalWeight(LivingEntity entity, @Nullable WeightSplit split) {
        if (split != null) split.clear();
        if (!FeathersServerConfig.ENABLE_ARMOR_WEIGHTS.get()) return 0;

        double head = pieceWeight(entity.getItemBySlot(EquipmentSlot.HEAD));
        double chest = pieceWeight(entity.getItemBySlot(EquipmentSlot.CHEST));
        // A mount's armor (horse armor) covers its body: it counts as its chest piece.
        if (!(entity instanceof Player)) chest += pieceWeight(entity.getItemBySlot(EquipmentSlot.BODY));
        double legs = pieceWeight(entity.getItemBySlot(EquipmentSlot.LEGS));
        double feet = pieceWeight(entity.getItemBySlot(EquipmentSlot.FEET));
        double raw = head + chest + legs + feet;
        double other = 0;
        if (split != null) {
            split.shares.add(head);
            split.shares.add(chest);
            split.shares.add(legs);
            split.shares.add(feet);
            split.shares.add(0);
        }
        for (Extensions.WeightEntry entry : Extensions.weightSources()) {
            double weight = entry.source().weight(entity);
            if (weight <= 0) continue;
            raw += weight;
            int tint = split != null ? tintOf(entry.source(), entity) : NO_TINT;
            if (tint == NO_TINT) {
                other += weight;
            } else {
                split.shares.add(weight);
                split.sources.add(tint);
                split.sources.add(0);
            }
        }
        if (split != null) split.shares.set(OTHER, other);

        double weight = NeoForge.EVENT_BUS.post(new ArmorWeightEvent(entity, raw)).getWeight();

        AttributeInstance multiplier = entity.getAttribute(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER);
        if (multiplier != null) weight *= multiplier.getValue();

        int total = Math.max(0, (int) Math.round(weight));
        if (split != null && total > 0) {
            if (raw <= 0) {
                split.parts[OTHER] = total;
            } else {
                round(split, total, weight / raw);
            }
        }
        return total;
    }

    private static final int NO_TINT = Integer.MIN_VALUE;

    /** A source's own color, its item's (as {@code -(id + 1)}, see {@link WeightSplit#tint}), or none. */
    private static int tintOf(WeightSource source, LivingEntity entity) {
        int color = source.color(entity);
        if (color != WeightSource.NO_COLOR) return color & 0xFFFFFF;
        Item item = source.displayItem(entity);
        if (item == null || item == Items.AIR) return NO_TINT;
        return -(BuiltInRegistries.ITEM.getId(item) + 1);
    }

    /**
     * Rounds the shares, times {@code scale}, to whole feathers adding up to {@code total}: floors first, then the
     * largest remainders.
     */
    private static void round(WeightSplit split, int total, double scale) {
        int count = split.shares.size();
        int assigned = 0;
        for (int i = 0; i < count; i++) {
            int floor = (int) Math.floor(split.shares.getDouble(i) * scale);
            setFeathers(split, i, floor);
            assigned += floor;
        }
        while (assigned < total) {
            int best = 0;
            double bestRemainder = -1;
            for (int i = 0; i < count; i++) {
                double remainder = split.shares.getDouble(i) * scale - feathers(split, i);
                if (remainder > bestRemainder) {
                    bestRemainder = remainder;
                    best = i;
                }
            }
            setFeathers(split, best, feathers(split, best) + 1);
            assigned++;
        }
        for (int i = count - 1; i >= 0 && assigned > total; i--) {
            int feathers = feathers(split, i);
            int taken = Math.min(feathers, assigned - total);
            setFeathers(split, i, feathers - taken);
            assigned -= taken;
        }
    }

    /** Share {@code i}: a part below {@link #PARTS}, a colored source after. */
    private static int feathers(WeightSplit split, int i) {
        return i < PARTS ? split.parts[i] : split.sources.getInt(2 * (i - PARTS) + 1);
    }

    private static void setFeathers(WeightSplit split, int i, int feathers) {
        if (i < PARTS) split.parts[i] = feathers;
        else split.sources.set(2 * (i - PARTS) + 1, feathers);
    }
}
