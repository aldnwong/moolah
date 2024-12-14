package ong.aldenw.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
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
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                        .executes(ExchangeCommand::forItem)))
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

    public static int forItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available for players").formatted(Formatting.DARK_RED), false);
            return -1;
        }

        ItemStackArgument item = ItemStackArgumentType.getItemStackArgument(context, "item");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Text result = PluginState.get(context.getSource().getServer()).exchangeHandler.forItem(item, amount, context.getSource().getPlayer(), context.getSource().getServer());
        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }

    public static int forFunds(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available for players").formatted(Formatting.DARK_RED), false);
            return -1;
        }

        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Text result = PluginState.get(context.getSource().getServer()).exchangeHandler.forFunds(item, amount, context.getSource().getPlayer(), context.getSource().getServer());
        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }

    public static int set(CommandContext<ServerCommandSource> context) {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        double cost = DoubleArgumentType.getDouble(context, "cost");
        Text result = PluginState.get(context.getSource().getServer()).exchangeHandler.addRate(item, cost);
        context.getSource().sendFeedback(() -> result, false);
        return 1;
    }

    public static int remove(CommandContext<ServerCommandSource> context) {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        Text result = PluginState.get(context.getSource().getServer()).exchangeHandler.removeRate(item);
        context.getSource().sendFeedback(() -> result, false);
        return 1;
    }
}
