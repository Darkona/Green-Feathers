package com.darkona.feathers.api;

import com.darkona.feathers.capability.PlayerFeathers;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface IModifier {


    /**
     * Usage Modifier Ordinals reference.
     * 0 = Hot effect / Momentum effect;
     * they will be applied first. If you are going to spend more/less feathers, these need to be applied first.
     * <p>
     * 10 = Endurance/Strain effect;
     * If you're going to overspend feathers, or spend endurance feathers this will be applied after knowing how many will be spent.
     * <p>
     * 1 to 9 are available.
     **/
    int[] ordinals = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    /**
     * This modifier is used to make the regeneration effect non-linear.
     * Regeneration is faster at the start and slower at the end.
     * Available for modders as an example, but not used in this mod.
     */
    IModifier NON_LINEAR_REGENERATION = new IModifier() {
        @Override
        public void onAdd(IFeathers iFeathers) {

        }

        @Override
        public void onRemove(IFeathers iFeathers) {

        }

        @Override
        public void applyToDelta(Player player, IFeathers f, AtomicInteger staminaDelta) {
            if (f.getCooldown() > 0) return;
            var staminaPerSecond = FeathersAPI.getPlayerStaminaRegenerationPerTick(player);
            var maxStamina = f.getMaxStamina();
            var value = Math.max((int) (1 / Math.log(f.getFeathers() / 40d + 1.4) - 3.5) * staminaPerSecond, 1);
            staminaDelta.set(value);

        }

        @Override
        public void applyToUsage(Player player, IFeathers f, AtomicInteger staminaDelta, AtomicBoolean result) {

        }

        @Override
        public int getUsageOrdinal() {
            return 0;
        }

        public int getDeltaOrdinal() {return 0;}

        @Override
        public String getName() {
            return "non_linear_regeneration";
        }
    };
    /**
     * This modifier is used to inverse the regeneration effect.
     * Available for modders as an example, but not used in this mod.
     */
    IModifier INVERSE_REGENERATION = new IModifier() {


        @Override
        public void onAdd(IFeathers iFeathers) {

        }

        @Override
        public void onRemove(IFeathers iFeathers) {

        }

        @Override
        public void applyToDelta(Player player, IFeathers f, AtomicInteger staminaDelta) {
            if (f.getCooldown() > 0) return;
            staminaDelta.set(staminaDelta.get() - FeathersAPI.getPlayerStaminaRegenerationPerTick(player));
        }

        @Override
        public void applyToUsage(Player player, IFeathers f, AtomicInteger staminaDelta, AtomicBoolean result) {

        }

        @Override
        public int getUsageOrdinal() {
            return 0;
        }

        @Override
        public int getDeltaOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "inverse_regeneration";
        }
    };
    /**
     * Basic modifier that applies the regeneration effect.
     * This modifier is used to regenerate the player's stamina.
     * The regeneration value is defined in the config.
     * This modifier is applied once per tick.
     */
    IModifier REGENERATION = new IModifier() {

        @Override
        public void onAdd(IFeathers iFeathers) {

        }

        @Override
        public void onRemove(IFeathers iFeathers) {

        }

        @Override
        public void applyToDelta(Player player, IFeathers f, AtomicInteger staminaDelta) {
            if (f.getCooldown() > 0) return;
            staminaDelta.set(staminaDelta.get() + FeathersAPI.getPlayerStaminaRegenerationPerTick(player));
        }

        @Override
        public void applyToUsage(Player player, IFeathers iFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {

        }

        @Override
        public int getUsageOrdinal() {
            return 0;
        }

        @Override
        public int getDeltaOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "regeneration";
        }

    };
    IModifier DEFAULT_USAGE = new IModifier() {


        @Override
        public void onAdd(IFeathers iFeathers) {

        }

        @Override
        public void onRemove(IFeathers iFeathers) {

        }

        @Override
        public void applyToDelta(Player player, IFeathers f, AtomicInteger usingFeathers) {
            //Do nothing
        }

        @Override
        public void applyToUsage(Player player, IFeathers f, AtomicInteger staminaToUse, AtomicBoolean approve) {
            if (approve.get()) return;
            approve.set(f.getAvailableStamina() - staminaToUse.get() >= 0);
        }

        @Override
        public int getUsageOrdinal() {
            return ordinals[20];
        }

        @Override
        public int getDeltaOrdinal() {
            return ordinals[0];
        }

        @Override
        public String getName() {
            return "default";
        }
    };

    void onAdd(IFeathers iFeathers);

    void onRemove(IFeathers iFeathers);

    void applyToDelta(Player player, IFeathers f, AtomicInteger staminaDelta);

    void applyToUsage(Player player, IFeathers f, AtomicInteger staminaToUse, AtomicBoolean result);

    int getUsageOrdinal();

    int getDeltaOrdinal();

    String getName();
}
