package ong.aldenw.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.PluginState;
import ong.aldenw.commands.suggestions.PlayerSuggestions;
import ong.aldenw.handlers.PlayerHandler;

import java.util.UUID;

public class GrantCommand {
    /*public final static String commandName = "grant";
    public final static int permissionLevel = 4;

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .suggests(new PlayerSuggestions())
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(PayCommand::execute))
                );
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        PlayerHandler playerHandler = PluginState.get(context.getSource().getServer()).playerHandler;
        UUID toUuid = playerHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = playerHandler.transferMoney(fromUuid, toUuid, amount, context.getSource().getServer());

        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }*/
}
