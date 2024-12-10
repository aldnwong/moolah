package ong.aldenw.managers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
    private final HashMap<UUID, PlayerData> players = new HashMap<>();

    public PlayerManager() {

    }

    public PlayerManager(NbtCompound compound) {
        NbtCompound playerCompound = compound.getCompound("players");
        playerCompound.getKeys().forEach((key) -> {
            players.put(UUID.fromString(key), new PlayerData(playerCompound.getCompound(key)));
        });
    }

    public NbtCompound writeNbt() {
        NbtCompound pmNbt = new NbtCompound();

        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> playersNbt.put(uuid.toString(), playerData.writeNbt()));
        pmNbt.put("players", playersNbt);

        return pmNbt;
    }

    public void onServerJoin(ServerPlayerEntity player) {
        PlayerData playerData = players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData(player.getName().getString(), player.getUuid()));

        if (!player.getName().getString().equals(playerData.username))
            playerData.username = player.getName().getString();


    }

    public static class PlayerData {
        private String username;
        private final UUID uuid;

        public PlayerData(String username, UUID uuid) {
            this.username = username;
            this.uuid = uuid;
        }

        public PlayerData(NbtCompound compound) {
            this.username = compound.getString("username");
            this.uuid = UUID.fromString(compound.getString("uuid"));
        }

        public NbtCompound writeNbt() {
            NbtCompound pdNbt = new NbtCompound();
            pdNbt.putString("username", username);
            pdNbt.putString("uuid", uuid.toString());
            return pdNbt;
        }
    }
}
