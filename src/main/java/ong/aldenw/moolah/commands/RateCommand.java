package ong.aldenw.moolah.commands;

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
import ong.aldenw.moolah.PluginState;
import ong.aldenw.moolah.commands.suggestions.ExchangeSuggestions;

public class RateCommand {
    public final static String commandName = "rate";
    public final static int generalPermissionLevel = 0;
    public final static int modifyPermissionLevel = 4;

    public static LiteralArgumentBuilder<ServerCommandSource> register(CommandRegistryAccess registryAccess) {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(generalPermissionLevel))
                .then(CommandManager.literal("get")
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .suggests(new ExchangeSuggestions())
                                .executes(RateCommand::get)))
                .then(CommandManager.literal("list")
                        .executes(RateCommand::list))
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(modifyPermissionLevel))
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .then(CommandManager.argument("cost", DoubleArgumentType.doubleArg())
                                        .executes(RateCommand::set)))
                )
                .then(CommandManager.literal("remove")
                        .requires(source -> source.hasPermissionLevel(modifyPermissionLevel))
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .suggests(new ExchangeSuggestions())
                                .executes(RateCommand::remove))
                );
    }

    public static int set(CommandContext<ServerCommandSource> context) {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        double cost = DoubleArgumentType.getDouble(context, "cost");
        Text result = PluginState.get(context.getSource().getServer()).exchangeHandler.addRate(item, cost);
        context.getSource().sendFeedback(() -> result, false);
        return 1;
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        Text result = PluginState.get(context.getSource().getServer()).exchangeHandler.getRates();
        context.getSource().sendFeedback(() -> result, false);
        return 1;
    }

    public static int remove(CommandContext<ServerCommandSource> context) {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        Text result = PluginState.get(context.getSource().getServer()).exchangeHandler.removeRate(item);
        context.getSource().sendFeedback(() -> result, false);
        return 1;
    }

    public static int get(CommandContext<ServerCommandSource> context) {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        double cost = PluginState.get(context.getSource().getServer()).exchangeHandler.getCostForItem(item);
        Text result = Text.empty().append(Text.literal("One ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal(" is equivalent to ").formatted(Formatting.GOLD)).append(Text.literal("$"+cost).formatted(Formatting.GREEN));
        if (cost < 0) {
            context.getSource().sendFeedback(() -> Text.literal("Item is not exchangeable").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        context.getSource().sendFeedback(() -> result, false);
        return 1;
    }
}
