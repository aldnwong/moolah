package ong.aldenw;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import ong.aldenw.handlers.BankHandler;
import ong.aldenw.handlers.ExchangeHandler;

public class PluginState extends PersistentState {
    public BankHandler bankHandler = new BankHandler();
    public ExchangeHandler exchangeHandler = new ExchangeHandler();

    private static final Type<PluginState> type = new Type<>(
            PluginState::new,
            PluginState::createFromNbt,
            null
    );

    public static PluginState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        PluginState state = new PluginState();
        state.bankHandler = new BankHandler(tag.getCompound("playerManager"));
        state.exchangeHandler = new ExchangeHandler(tag.getCompound("exchangeManager"));
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.put("playerManager", bankHandler.writeNbt());
        nbt.put("exchangeManager", exchangeHandler.writeNbt());
        return nbt;
    }

    public static PluginState get(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        PluginState state = persistentStateManager.getOrCreate(type, Moolah.MOD_ID);
        state.markDirty();
        return state;
    }
}
