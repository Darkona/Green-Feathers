package com.darkona.feathers.command;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersIds;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * {@code /feathers}: inspect and set players' feathers. Permission level 2.
 */
@EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class FeathersCommands {

    private static final String TARGETS = "targets";
    private static final String AMOUNT = "amount";

    private FeathersCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("feathers")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("info")
                        .then(Commands.argument(TARGETS, EntityArgument.players()).executes(FeathersCommands::info)))
                .then(Commands.literal("set")
                        .then(Commands.argument(TARGETS, EntityArgument.players())
                                .then(Commands.argument(AMOUNT, DoubleArgumentType.doubleArg(0)).executes(FeathersCommands::set))))
                .then(Commands.literal("reset")
                        .then(Commands.argument(TARGETS, EntityArgument.players()).executes(FeathersCommands::reset)))
                .then(Commands.literal("max")
                        .then(Commands.argument(TARGETS, EntityArgument.players())
                                .then(Commands.argument(AMOUNT, IntegerArgumentType.integer(0, 1000)).executes(FeathersCommands::setMax))))
                .then(Commands.literal("regen")
                        .then(Commands.argument(TARGETS, EntityArgument.players())
                                .then(Commands.argument(AMOUNT, DoubleArgumentType.doubleArg(-40, 40)).executes(FeathersCommands::setRegen))))
                .then(Commands.literal("spend")
                        .then(Commands.argument(TARGETS, EntityArgument.players())
                                .then(Commands.argument(AMOUNT, DoubleArgumentType.doubleArg(0)).executes(FeathersCommands::spend)))));
    }

    private static Collection<ServerPlayer> targets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return EntityArgument.getPlayers(ctx, TARGETS);
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = targets(ctx);
        for (ServerPlayer player : players) {
            FeathersView f = FeathersAPI.get(player);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "%s: %.2f/%d feathers (%.2f usable), strain %.2f/%.2f, bonus %.2f, weight %d, regen %.2f f/s%s%s, climate %s, rest %s x%.1f".formatted(
                            player.getName().getString(), feathersOf(f.stamina()), f.maxFeathers(), feathersOf(f.availableStamina()),
                            feathersOf(f.strain()), feathersOf(f.maxStrain()), feathersOf(f.bonusStamina()), f.weight(),
                            FeathersAPI.getRegenPerSecond(player), f.regenDelay() > 0 ? " (paused " + f.regenDelay() + "t)" : "",
                            f.exhausted() ? ", EXHAUSTED" : "", FeathersAPI.getClimate(player), f.restState(), f.restMultiplier())), false);
        }
        return players.size();
    }

    private static double feathersOf(int stamina) {
        return stamina / (double) Stamina.PER_FEATHER;
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = targets(ctx);
        double amount = DoubleArgumentType.getDouble(ctx, AMOUNT);
        for (ServerPlayer player : players) FeathersAPI.setStamina(player, Stamina.ofFeathers(amount));
        ctx.getSource().sendSuccess(() -> Component.literal("Set feathers to %.2f for %d player(s)".formatted(amount, players.size())), true);
        return players.size();
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = targets(ctx);
        players.forEach(FeathersAPI::reset);
        ctx.getSource().sendSuccess(() -> Component.literal("Reset feathers for %d player(s)".formatted(players.size())), true);
        return players.size();
    }

    private static int setMax(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = targets(ctx);
        int amount = IntegerArgumentType.getInteger(ctx, AMOUNT);
        for (ServerPlayer player : players) FeathersAPI.setMaxFeathers(player, amount);
        ctx.getSource().sendSuccess(() -> Component.literal("Set base max feathers to %d for %d player(s)".formatted(amount, players.size())), true);
        return players.size();
    }

    private static int setRegen(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = targets(ctx);
        double amount = DoubleArgumentType.getDouble(ctx, AMOUNT);
        for (ServerPlayer player : players) FeathersAPI.setBaseRegenPerSecond(player, amount);
        ctx.getSource().sendSuccess(() -> Component.literal("Set base regeneration to %.2f feathers/s for %d player(s)".formatted(amount, players.size())), true);
        return players.size();
    }

    private static int spend(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = targets(ctx);
        double amount = DoubleArgumentType.getDouble(ctx, AMOUNT);
        for (ServerPlayer player : players) {
            var result = FeathersAPI.spend(player, id("command"), Stamina.ofFeathers(amount));
            ctx.getSource().sendSuccess(() -> Component.literal("%s: %s".formatted(player.getName().getString(), result)), false);
        }
        return players.size();
    }
}
