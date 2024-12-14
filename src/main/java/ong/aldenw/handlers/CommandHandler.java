package ong.aldenw.handlers;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.commands.*;

public class CommandHandler {
    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, net.minecraft.server.command.CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(PayCommand.register());
        dispatcher.register(BalanceCommand.register());
        dispatcher.register(BalanceCommand.registerAlias());
        dispatcher.register(AdjustCommand.register());
        dispatcher.register(AdjustCommand.registerAlias());
        dispatcher.register(GambleCommand.register());
        dispatcher.register(GambleCommand.registerAlias());
        dispatcher.register(SetCommand.register());
        dispatcher.register(ExchangeCommand.register(registryAccess));
        dispatcher.register(ExchangeCommand.registerAlias(registryAccess));
    }
}
