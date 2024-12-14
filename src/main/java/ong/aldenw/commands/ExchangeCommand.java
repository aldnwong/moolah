package ong.aldenw.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.PluginState;
import ong.aldenw.commands.suggestions.ExchangeSuggestions;
import ong.aldenw.handlers.ExchangeHandler;

public class ExchangeCommand {
    public final static String commandName = "exchange";
    public final static String commandAlias = "exc";
    public final static int generalPermissionLevel = 0;
    public final static int modifyPermissionLevel = 4;

    public static LiteralArgumentBuilder<ServerCommandSource> register(CommandRegistryAccess registryAccess) {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(generalPermissionLevel))
                .then(CommandManager.literal("forItem")
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .suggests(new ExchangeSuggestions())
                                .executes(ExchangeCommand::forItem))
                )
                .then(CommandManager.literal("forFunds")
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .suggests(new ExchangeSuggestions())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                        .executes(ExchangeCommand::forFunds)))
                )
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(modifyPermissionLevel))
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .then(CommandManager.argument("cost", DoubleArgumentType.doubleArg())
                                    .executes(ExchangeCommand::set)))
                )
                .then(CommandManager.literal("remove")
                        .requires(source -> source.hasPermissionLevel(modifyPermissionLevel))
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .suggests(new ExchangeSuggestions())
                                    .executes(ExchangeCommand::remove))
                );
    }

    /*public static LiteralArgumentBuilder<ServerCommandSource> registerAlias() {
        return CommandManager.literal(commandAlias)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.argument("item", StringArgumentType.string())
                        .suggests(new ExchangeSuggestions())
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(ExchangeCommand::execute))
                );
    }*/

    public static int forItem(CommandContext<ServerCommandSource> context) {
        /*BankHandler bankHandler = PluginState.get(context.getSource().getServer()).bankHandler;
        UUID playerUuid = bankHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = bankHandler.setCmd(playerUuid, amount, context.getSource().getServer());

        context.getSource().sendFeedback(() -> result, false);*/
        ExchangeHandler exchangeHandler = PluginState.get(context.getSource().getServer()).exchangeHandler;
        //exchangeHandler.add();
        return 1;
    }

    public static int forFunds(CommandContext<ServerCommandSource> context) {
        /*BankHandler bankHandler = PluginState.get(context.getSource().getServer()).bankHandler;
        UUID playerUuid = bankHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = bankHandler.setCmd(playerUuid, amount, context.getSource().getServer());

        context.getSource().sendFeedback(() -> result, false);*/
        ExchangeHandler exchangeHandler = PluginState.get(context.getSource().getServer()).exchangeHandler;
        //exchangeHandler.add();
        return 1;
    }

    public static int set(CommandContext<ServerCommandSource> context) {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        double cost = DoubleArgumentType.getDouble(context, "cost");
        PluginState.get(context.getSource().getServer()).exchangeHandler.addRate(item, cost);
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Set ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal("'s cost to " + cost).formatted(Formatting.GOLD)), false);
        return 1;
    }

    public static int remove(CommandContext<ServerCommandSource> context) {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        PluginState.get(context.getSource().getServer()).exchangeHandler.removeRate(item);
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Removed ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal("'s exchange rate").formatted(Formatting.GOLD)), false);
        return 1;
    }
}
