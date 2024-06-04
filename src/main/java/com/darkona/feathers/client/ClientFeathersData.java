package com.darkona.feathers.client;

import com.darkona.feathers.api.Constants;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.IFeathers;
import com.darkona.feathers.capability.Capabilities;
import com.darkona.feathers.config.ClientConfig;
import com.darkona.feathers.effect.effects.EnduranceEffect;
import com.darkona.feathers.effect.effects.StrainEffect;
import com.darkona.feathers.networking.packet.FeatherSTCSyncPacket;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Setter
@Getter
public class ClientFeathersData {

    //Use singleton for sanity
    private static ClientFeathersData instance;
    private int stamina = 2000;
    private int feathers = 0;
    private int maxStamina = 2000;
    private int maxFeathers = 0;
    private int staminaDelta = 0;
    private int previousFeathers = 0;
    private int enduranceFeathers = 0;
    private int weight = 0;
    private int animationCooldown = 0;
    private int fadeCooldown = 0;
    private int strainFeathers = 0;
    private boolean hot = false;
    private boolean cold = false;
    private boolean energized = false;
    private boolean momentum = false;
    private boolean fatigued = false;
    private boolean endurance = false;
    private boolean strained = false;

    private ClientFeathersData() {}

    public static ClientFeathersData getInstance() {
        if (instance == null) {
            instance = new ClientFeathersData();
        }
        return instance;
    }

    public void update(Player player, IFeathers f){
        stamina = f.getStamina();
        maxStamina = f.getMaxStamina();
        feathers = f.getFeathers();
        maxFeathers = f.getMaxFeathers();
        staminaDelta = f.getStaminaDelta();
        weight = f.getWeight();

        synchronizeEffects(player);

        strainFeathers = (int)Math.ceil(f.getCounter(StrainEffect.STRAIN_COUNTER));
        enduranceFeathers = (int)Math.ceil(f.getCounter(EnduranceEffect.ENDURANCE_COUNTER));
    }

    public void update(FeatherSTCSyncPacket message, Supplier<NetworkEvent.Context> supplier) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                update(player, f);
                supplier.get().setPacketHandled(true);
            });
        }
    }

    private void synchronizeEffects(Player player) {
        hot = FeathersAPI.isHot(player);
        endurance = FeathersAPI.isEnduring(player);
        cold = FeathersAPI.isCold(player);
        energized = FeathersAPI.isEnergized(player);
        momentum = FeathersAPI.isMomentum(player);
        fatigued = FeathersAPI.isFatigued(player);
        strained = FeathersAPI.isStrained(player);
    }

    public boolean hasFullStamina() {
        return stamina >= maxStamina;
    }

    public boolean hasFeathers() {
        return feathers > 0;
    }

    public boolean hasFullFeathers() {
        return feathers >= maxFeathers;
    }

    public boolean hasWeight() {
        return weight > 0;
    }

    public boolean isOverflowing() {
        return feathers > maxFeathers;
    }

    public void tick() {
        var player = Minecraft.getInstance().player;

        if(player != null)
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> update(player, f));

        if (animationCooldown > 0) animationCooldown--;

        if (feathers != previousFeathers) {
            if (feathers > previousFeathers && ClientConfig.REGEN_EFFECT.get() && animationCooldown <= 0) {
                animationCooldown = 18;
            }
            previousFeathers = feathers;
        }

        if (ClientConfig.FADE_WHEN_FULL.get()) {
            int cooldown = fadeCooldown;
            if (feathers == getMaxFeathers() || enduranceFeathers > 0) {
                fadeCooldown = cooldown < ClientConfig.FADE_COOLDOWN.get() ? fadeCooldown + 1 : 0;
            }
        }
    }

    public int getStrainFeathers() {
        return strainFeathers > 0 ? strainFeathers / Constants.STAMINA_PER_FEATHER : 0;
    }

    public int getAvailableFeathers() {
        return feathers - weight;
    }
}
