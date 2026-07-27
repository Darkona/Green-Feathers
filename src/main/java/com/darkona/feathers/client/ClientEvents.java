package com.darkona.feathers.client;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.client.ClientFeathers;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.basic.BasicExertion;
import com.darkona.feathers.client.gui.FeatherColors;
import com.darkona.feathers.client.gui.FeathersHud;
import com.darkona.feathers.config.FeathersClientConfig;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.core.FeathersServiceImpl;
import com.darkona.feathers.style.FeatherStylePack;
import com.darkona.feathers.style.GreenFeatherAnimations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static com.darkona.feathers.api.registry.FeathersIds.id;

@EventBusSubscriber(modid = FeathersIds.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private static final ResourceLocation FEATHER_FONT = id("feather_font");
    /** The weight tooltip's icons, in the feather font: built once, since a tooltip is rebuilt every frame. */
    private static final Style FEATHER_ICONS = Style.EMPTY.withFont(FEATHER_FONT);

    private static boolean wasCold;

    private ClientEvents() {}

    /* Mod bus */

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientFeathers.setService(ClientFeathersData.INSTANCE);
        FeathersServiceImpl.setClientBridge(ClientFeathersData.INSTANCE);
        // Its triggers read the client config: registered here, not with the styles in common setup.
        GreenFeatherAnimations.register();
    }

    /** Feather colors come from textures and resource packs' feather_styles.json: reread them when packs change. */
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            FeatherColors.clear();
            FeatherStylePack.load(manager);
        });
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // Top of the right-hand stack: after air, where thirst mods such as Thirst Was Taken draw their bar.
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, FeathersHud.LAYER_ID, FeathersHud::render);
    }

    /* Game bus */

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ClientFeathersData.INSTANCE.tick();
        FeathersHud.tickAnimations();

        boolean cold = FeathersAPI.isCold(player);
        if (cold && !wasCold && FeathersClientConfig.FROST_SOUND.get()) {
            player.playSound(SoundEvents.PLAYER_HURT_FREEZE, 0.6f, 1.2f);
        }
        wasCold = cold;

        // Sprinting is decided on the client, so the client is where running out has to stop it.
        if (BasicExertion.isActive() && player.isSprinting() && ClientFeathersData.INSTANCE.exhausted() && !player.getAbilities().instabuild) {
            player.setSprinting(false);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientFeathersData.INSTANCE.clear();
        FeathersHud.reset();
    }

    /**
     * Shows how much an armor piece weighs, counting its enchantments.
     */
    /** Icon strings by weight in half icons: a tooltip is rebuilt every frame while hovered. */
    private static final String[] WEIGHT_ICONS = new String[41];

    private static String weightIcons(int halves) {
        if (halves < WEIGHT_ICONS.length && WEIGHT_ICONS[halves] != null) return WEIGHT_ICONS[halves];
        StringBuilder icons = new StringBuilder();
        for (int i = 2; i <= halves + 1; i += 2) {
            icons.append(i - 1 == halves ? "b" : "a ");
        }
        String built = icons.reverse().toString();
        if (halves < WEIGHT_ICONS.length) WEIGHT_ICONS[halves] = built;
        return built;
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ArmorItem) || !FeathersClientConfig.DISPLAY_WEIGHTS.get()
                || !FeathersServerConfig.ENABLE_ARMOR_WEIGHTS.get()) return;

        double weight = FeathersAPI.getPieceWeight(stack);
        if (weight <= 0) return;

        if (FeathersClientConfig.VISUAL_WEIGHTS.get()) {
            // The feather font maps 'a' to a full feather icon and 'b' to a half one; a weight point is half an icon.
            int halves = Math.max(1, (int) Math.round(weight));
            event.getToolTip().add(Component.literal(weightIcons(halves)).withStyle(FEATHER_ICONS));
        } else {
            String shown = weight == Math.rint(weight) ? Integer.toString((int) weight) : "%.1f".formatted(weight);
            event.getToolTip().add(Component.translatable("text.greenfeathers.tooltip", shown).withStyle(ChatFormatting.BLUE));
        }
    }
}
