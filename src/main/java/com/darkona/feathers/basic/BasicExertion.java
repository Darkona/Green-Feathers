package com.darkona.feathers.basic;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.config.FeathersServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Sprinting and jumping cost feathers, so Green Feathers does something on its own. Built only on the public API,
 * as another mod would. Off when Actions of Stamina is installed: it owns player actions.
 */
@Mod.EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class BasicExertion {

    public static final ResourceLocation SPRINT = id("sprint");
    public static final ResourceLocation JUMP = id("jump");

    private static final boolean ACTIONS_OF_STAMINA = ModList.get().isLoaded("actionsofstamina");

    private BasicExertion() {}

    public static boolean isActive() {
        return !ACTIONS_OF_STAMINA && FeathersServerConfig.ENABLE_BASIC_EXERTION.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !isActive() || !player.isSprinting() || player.isPassenger()) return;

        double perSecond = FeathersServerConfig.SPRINT_FEATHERS_PER_SECOND.get();
        if (perSecond <= 0) return;

        SpendResult result = FeathersAPI.startDrain(player, SPRINT, Stamina.perTick(perSecond));
        if (!result.allowed()) player.setSprinting(false);
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level.isClientSide() || !isActive() || !(event.getEntity() instanceof Player player)) return;
        double feathers = FeathersServerConfig.JUMP_FEATHERS.get();
        if (feathers > 0) FeathersAPI.spend(player, JUMP, Stamina.ofFeathers(feathers));
    }
}
