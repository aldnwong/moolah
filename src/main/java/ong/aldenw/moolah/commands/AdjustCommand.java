package ong.aldenw.moolah.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import ong.aldenw.moolah.PluginState;
import ong.aldenw.moolah.commands.suggestions.PlayerSuggestions;
import ong.aldenw.moolah.handlers.BankHandler;

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
        BankHandler bankHandler = PluginState.get(context.getSource().getServer()).bankHandler;
        UUID playerUuid = bankHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = bankHandler.adjustCmd(playerUuid, amount, context.getSource().getServer());

        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }
}
