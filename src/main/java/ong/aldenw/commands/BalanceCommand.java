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

public class BalanceCommand {
    public final static String commandName = "balance";
    public final static int permissionLevel = 0;
    public final static int subpermissionLevel = 4;

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .executes(BalanceCommand::execute)
                .then(CommandManager.literal("of")
                        .requires(source -> source.hasPermissionLevel(subpermissionLevel))
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(new PlayerSuggestions())
                                .executes(BalanceCommand::subexecute)
                        ));
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        PlayerHandler playerHandler = PluginState.get(context.getSource().getServer()).playerHandler;
        UUID playerUuid = context.getSource().getPlayer().getUuid();
        Text result = playerHandler.getAmount(playerUuid);

        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }

    public static int subexecute(CommandContext<ServerCommandSource> context) {

        PlayerHandler playerHandler = PluginState.get(context.getSource().getServer()).playerHandler;
        UUID playerUuid = playerHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        Text result = playerHandler.getAmount(playerUuid);

        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }
}
