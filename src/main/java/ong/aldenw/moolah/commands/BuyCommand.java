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

public class BuyCommand {
    public final static String commandName = "buy";
    public final static int permissionLevel = 0;

    public static LiteralArgumentBuilder<ServerCommandSource> register(CommandRegistryAccess registryAccess) {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                        .suggests(new ExchangeSuggestions())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes(BuyCommand::execute)));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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
}
