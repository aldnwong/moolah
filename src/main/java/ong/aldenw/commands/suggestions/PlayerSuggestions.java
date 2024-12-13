package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.PluginState;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class PlayerSuggestions implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ArrayList<String> playerNames = PluginState.get(context.getSource().getServer()).playerHandler.getPlayerList();
        for (String playerName : playerNames) {
            builder.suggest(playerName);
        }
        return builder.buildFuture();
    }
}