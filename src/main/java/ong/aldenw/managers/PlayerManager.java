package ong.aldenw.managers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
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

        playerData.notifications.forEach((message) -> {
            player.sendMessageToClient(Text.literal(message).formatted(Formatting.GOLD), false);
        });

        playerData.notifications.clear();
    }

    public PlayerData getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public static class PlayerData {
        private String username;
        private final UUID uuid;
        private final ArrayList<String> notifications;

        public String getUsername() {
            return username;
        }

        public UUID getUuid() {
            return uuid;
        }

        public PlayerData(String username, UUID uuid) {
            this.username = username;
            this.uuid = uuid;
            notifications = new ArrayList<>();
        }

        public PlayerData(NbtCompound compound) {
            this.username = compound.getString("username");
            this.uuid = UUID.fromString(compound.getString("uuid"));

            this.notifications = new ArrayList<>();
            NbtList notificationNbt = compound.getList("notifications", NbtElement.STRING_TYPE);
            notificationNbt.forEach(notification -> notifications.add(notification.asString()));
        }

        public NbtCompound writeNbt() {
            NbtCompound pdNbt = new NbtCompound();

            pdNbt.putString("username", username);
            pdNbt.putString("uuid", uuid.toString());

            NbtList notificationNbt = new NbtList();
            notifications.forEach(notification -> notificationNbt.add(NbtString.of(notification)));
            pdNbt.put("notifications", notificationNbt);

            return pdNbt;
        }

        public void notifyPlayer(String message, MinecraftServer server) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (Objects.isNull(player)) {
                notifications.add(message);
                return;
            }

            player.sendMessageToClient(Text.literal(message).formatted(Formatting.GOLD), false);
        }
    }
}
