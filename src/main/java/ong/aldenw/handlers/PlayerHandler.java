package ong.aldenw.handlers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.Moolah;

import java.util.*;

public class PlayerHandler {
    private final HashMap<UUID, PlayerData> players = new HashMap<>();

    public PlayerHandler() {

    }

    public PlayerHandler(NbtCompound compound) {
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

    public UUID getPlayerUuid(String username) {
        for (PlayerData player : players.values()) {
            if (player.username.equals(username))
                return player.uuid;
        }
        return null;
    }

    public ArrayList<String> getPlayerList() {
        ArrayList<String> playerList = new ArrayList<>();
        for (PlayerData player : players.values()) {
            playerList.add(player.username);
        }
        return playerList;
    }

    public Text transferMoney(UUID fromUuid, UUID toUuid, double amount, MinecraftServer server) {
        if (fromUuid.equals(toUuid)) {
            return Text.literal("You cannot transfer funds to yourself").formatted(Formatting.DARK_RED);
        }
        if (amount <= 0.0) {
            return Text.literal("Amount must be more than 0").formatted(Formatting.DARK_RED);
        }

        PlayerData fromData = players.get(fromUuid);
        PlayerData toData = players.get(toUuid);
        if (Objects.isNull(fromData) || Objects.isNull(toData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }
        if (fromData.money < amount) {
            return Text.literal("You do not have enough funds to do this transaction").formatted(Formatting.DARK_RED);
        }
        fromData.money -= amount;
        toData.money += amount;
        toData.notifyPlayer("$" + amount + " has been transferred to you by " + fromData.username, server);
        return Text.literal("$" + amount + " transferred to " + toData.username).formatted(Formatting.GREEN);
    }

    public Text changeMoney(UUID playerUuid, double amount, MinecraftServer server) {
        if (amount == 0.0) {
            return Text.literal("Amount cannot be 0").formatted(Formatting.DARK_RED);
        }

        PlayerData playerData = players.get(playerUuid);
        if (Objects.isNull(playerData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }
        playerData.money += amount;
        if (amount > 0) {
            playerData.notifyPlayer("$" + amount + " has been added to your account by an admin", server);
            return Text.literal("Gave $" + Math.abs(amount) + " to " + playerData.username).formatted(Formatting.GREEN);
        }
        else {
            playerData.notifyPlayer("$" + Math.abs(amount) + " has been removed from your account by an admin", server);
            return Text.literal("Removed $" + Math.abs(amount) + " from " + playerData.username).formatted(Formatting.RED);
        }
    }

    public Text getAmount(UUID playerUuid) {
        PlayerData playerData = players.get(playerUuid);

        if (Objects.isNull(playerData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }

        return Text.empty().append(Text.literal("You have ").formatted(Formatting.GOLD)).append(Text.literal("$" + playerData.money).formatted(Formatting.GREEN));
    }

    public static class PlayerData {
        private String username;
        private final UUID uuid;
        private final ArrayList<String> notifications;
        private double money;

        public String getUsername() {
            return username;
        }

        public UUID getUuid() {
            return uuid;
        }

        public PlayerData(String username, UUID uuid) {
            this.username = username;
            this.uuid = uuid;
            this.money = 1000;
            notifications = new ArrayList<>();
        }

        public PlayerData(NbtCompound compound) {
            this.username = compound.getString("username");
            this.uuid = UUID.fromString(compound.getString("uuid"));
            this.money = compound.getDouble("money");

            this.notifications = new ArrayList<>();
            NbtList notificationNbt = compound.getList("notifications", NbtElement.STRING_TYPE);
            notificationNbt.forEach(notification -> notifications.add(notification.asString()));
        }

        public NbtCompound writeNbt() {
            NbtCompound pdNbt = new NbtCompound();

            pdNbt.putString("username", username);
            pdNbt.putString("uuid", uuid.toString());
            pdNbt.putDouble("money", money);

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
