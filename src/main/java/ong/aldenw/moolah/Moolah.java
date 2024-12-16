package ong.aldenw.moolah;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import ong.aldenw.moolah.handlers.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Moolah implements ModInitializer {
	public static final String MOD_ID = "moolah";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PluginState.get(server).bankHandler.onServerJoin(handler.getPlayer());
		});

		CommandRegistrationCallback.EVENT.register(CommandHandler::initialize);
	}
}