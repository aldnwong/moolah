package ong.aldenw.moolah.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.moolah.PluginState;
import ong.aldenw.moolah.handlers.BankHandler;

import java.util.UUID;

public class GambleCommand {
    public final static String commandName = "gamble";
    public final static String commandAlias = "gam";
    public final static int permissionLevel = 0;

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(GambleCommand::execute)
                );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> registerAlias() {
        return CommandManager.literal(commandAlias)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(GambleCommand::execute)
                );
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        BankHandler bankHandler = PluginState.get(context.getSource().getServer()).bankHandler;
        UUID playerUuid = context.getSource().getPlayer().getUuid();
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = bankHandler.gambleCmd(playerUuid, amount);

        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }
}
