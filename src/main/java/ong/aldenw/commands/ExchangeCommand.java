package ong.aldenw.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import ong.aldenw.PluginState;
import ong.aldenw.commands.suggestions.ExchangeSuggestions;
import ong.aldenw.commands.suggestions.ItemSuggestions;
import ong.aldenw.commands.suggestions.PlayerSuggestions;
import ong.aldenw.handlers.BankHandler;
import ong.aldenw.handlers.ExchangeHandler;

import java.util.UUID;

public class ExchangeCommand {
    public final static String commandName = "exchange";
    public final static String commandAlias = "exc";
    public final static int permissionLevel = 0;

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.argument("give", StringArgumentType.string())
                        .suggests(new ExchangeSuggestions())
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .then(CommandManager.argument("receive", StringArgumentType.string())
                                        .executes(ExchangeCommand::execute)))
                )
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("give", StringArgumentType.string())
                                .suggests(new ItemSuggestions()))
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

    public static int execute(CommandContext<ServerCommandSource> context) {
        /*BankHandler bankHandler = PluginState.get(context.getSource().getServer()).bankHandler;
        UUID playerUuid = bankHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = bankHandler.setCmd(playerUuid, amount, context.getSource().getServer());

        context.getSource().sendFeedback(() -> result, false);*/
        ExchangeHandler exchangeHandler = PluginState.get(context.getSource().getServer()).exchangeHandler;
        exchangeHandler.add();
        return 1;
    }
}
