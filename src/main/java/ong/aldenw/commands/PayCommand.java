package ong.aldenw.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.PluginState;
import ong.aldenw.commands.suggestions.PlayerSuggestions;
import ong.aldenw.handlers.BankHandler;

import java.util.UUID;

public class PayCommand {
    public final static String commandName = "pay";
    public final static int permissionLevel = 0;

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
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        BankHandler bankHandler = PluginState.get(context.getSource().getServer()).bankHandler;
        UUID fromUuid = context.getSource().getPlayer().getUuid();
        UUID toUuid = bankHandler.getPlayerUuid(StringArgumentType.getString(context, "player"));
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Text result = bankHandler.transferCmd(fromUuid, toUuid, amount, context.getSource().getServer());

        context.getSource().sendFeedback(() -> result, false);

        return 1;
    }
}
