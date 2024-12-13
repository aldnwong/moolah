package ong.aldenw;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import ong.aldenw.handlers.PlayerHandler;

public class PluginState extends PersistentState {
    public PlayerHandler playerHandler = new PlayerHandler();

    private static final Type<PluginState> type = new Type<>(
            PluginState::new,
            PluginState::createFromNbt,
            null
    );

    public static PluginState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        PluginState state = new PluginState();
        state.playerHandler = new PlayerHandler(tag.getCompound("playerManager"));
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("plugin_version", Moolah.MOD_VERSION);

        nbt.put("playerManager", playerHandler.writeNbt());

        return nbt;
    }

    public static PluginState get(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        PluginState state = persistentStateManager.getOrCreate(type, Moolah.MOD_ID);
        state.markDirty();
        return state;
    }
}
