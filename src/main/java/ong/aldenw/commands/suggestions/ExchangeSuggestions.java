package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.PluginState;
import ong.aldenw.handlers.ExchangeHandler;

import java.util.concurrent.CompletableFuture;

public class ExchangeSuggestions implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ExchangeHandler exchangeHandler = PluginState.get(context.getSource().getServer()).exchangeHandler;
        exchangeHandler.
        return builder.buildFuture();
    }
}