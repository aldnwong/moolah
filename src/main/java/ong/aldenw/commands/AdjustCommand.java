package ong.aldenw.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import ong.aldenw.PluginState;
import ong.aldenw.commands.suggestions.PlayerSuggestions;
import ong.aldenw.handlers.PlayerHandler;

import java.util.UUID;

public class AdjustCommand {
    public final static String commandName = "adjust";
    public final static String commandAlias = "adj";
    public final static int permissionLevel = 4;

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .suggests(new PlayerSuggestions())
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(AdjustCommand::execute))
                );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> registerAlias() {
        return CommandManager.literal(commandAlias)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .suggests(new PlayerSuggestions())
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(AdjustCommand::execute))
                );
    }


    public static int execute(CommandContext<ServerCommandSource> context) {
        PlayerHandler playerHandler = PluginState.get(context.getSource().getServer()).playerHandler;
        UUID playerUuid = playerHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = playerHandler.changeMoney(playerUuid, amount, context.getSource().getServer());

        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }
}
