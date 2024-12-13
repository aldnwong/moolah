package ong.aldenw.handlers;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.commands.PayCommand;

public class CommandHandler {
    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, net.minecraft.server.command.CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(PayCommand.register());
    }
}
