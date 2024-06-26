package com.darkona.feathers.api;

import com.darkona.feathers.networking.packet.FeatherSTCSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public interface IFeathers {

    boolean isShouldCooldown();

    void setShouldCooldown(boolean shouldCooldown);

    int getStamina();

    void setStamina(int stamina);

    int getFeathers();

    void setFeathers(int feathers);

    void addDeltaModifier(IModifier modifier);

    void removeDeltaModifier(IModifier modifier);

    int getAvailableFeathers();

    void addUsageModifier(IModifier modifier);

    void removeUsageModifier(IModifier modifier);

    int getMaxStamina();

    void setMaxStamina(int maxStamina);

    int getAvailableStamina();

    boolean gainFeathers(int feathers);

    /**
     * Spend feathers from the player.
     *
     * @param player   the player
     * @param feathers the amount of feathers to spend
     * @return true if the player has enough feathers to spend, false otherwise
     */
    boolean useFeathers(Player player, int feathers, int cooldown);

    int getCooldown();

    void setCooldown(int cooldown);

    int getWeight();

    void setWeight(int weight);

    int getMaxStrained();

    void setMaxStrained(int maxStrained);

    int getStaminaDelta();

    void setStaminaDelta(int staminaDelta);

    boolean isDirty();

    Map<String, IModifier> getStaminaDeltaModifiers();

    Map<String, IModifier> getStaminaUsageModifiers();

    void removeCounter(String name);

    Double getCounter(String name);

    void setCounter(String name, double value);

    void markDirty();

    int getMaxFeathers();

    void copyFrom(IFeathers oldStore);

    void tick(Player player);

    CompoundTag saveNBTData();

    void loadNBTData(CompoundTag nbt);

    boolean hasCounter(String name);

    Map<String, Double> getCounters();

    void incrementCounterBy(String name, double amount);

    void multiplyCounterBy(String name, double amount);

    int getPrevFeathers();

    int getPrevStamina();

    void updateInClient(FeatherSTCSyncPacket message, Supplier<NetworkEvent.Context> contextSupplier);

    List<IModifier> getStaminaDeltaModifierList();

    List<IModifier> getFeatherUsageModifiersList();
}
