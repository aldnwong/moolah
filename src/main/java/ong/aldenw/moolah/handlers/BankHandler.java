package ong.aldenw.moolah.handlers;

import net.minecraft.nbt.NbtCompound;
import ong.aldenw.moolah.data.PlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class BankHandler {
    private final HashMap<UUID, PlayerData> players = new HashMap<>();

    public BankHandler() {

    }

    public BankHandler(NbtCompound compound) {
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

        if (!player.getName().getString().equals(playerData.getUsername()))
            playerData.setUsername(player.getName().getString());

        playerData.getNotifications().forEach((message) -> {
            player.sendMessageToClient(Text.literal(message).formatted(Formatting.GOLD), false);
        });

        playerData.clearNotifications();
    }

    public UUID getPlayerUuid(String username) {
        for (PlayerData player : players.values()) {
            if (player.getUsername().equals(username))
                return player.getUuid();
        }
        return null;
    }

    public ArrayList<String> getPlayerList() {
        ArrayList<String> playerList = new ArrayList<>();
        for (PlayerData player : players.values()) {
            playerList.add(player.getUsername());
        }
        return playerList;
    }

    public Text transferCmd(UUID fromUuid, UUID toUuid, double amount, MinecraftServer server) {
        amount = Math.floor(amount * 100.0) / 100.0;
        if (fromUuid.equals(toUuid)) {
            return Text.literal("You cannot transfer money to yourself").formatted(Formatting.DARK_RED);
        }
        if (amount <= 0.0) {
            return Text.literal("Amount must be more than 0").formatted(Formatting.DARK_RED);
        }

        PlayerData fromData = players.get(fromUuid);
        PlayerData toData = players.get(toUuid);
        if (Objects.isNull(fromData) || Objects.isNull(toData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }
        if (fromData.getMoney() < amount) {
            return Text.literal("You do not have enough money").formatted(Formatting.DARK_RED);
        }
        fromData.subtractMoney(amount);
        toData.addMoney(amount);
        toData.notifyPlayer("$" + amount + " has been transferred to you by " + fromData.getUsername(), server);
        return Text.literal("$" + amount + " transferred to " + toData.getUsername()).formatted(Formatting.GREEN);
    }

    public Text adjustCmd(UUID playerUuid, double amount, MinecraftServer server) {
        amount = (amount > 0.0) ? (Math.floor(amount * 100.0) / 100.0) : (Math.ceil(amount * 100.0) / 100.0);
        if (amount == 0.0) {
            return Text.literal("Amount cannot be 0").formatted(Formatting.DARK_RED);
        }

        PlayerData playerData = players.get(playerUuid);
        if (Objects.isNull(playerData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }
        playerData.addMoney(amount);;
        if (amount > 0) {
            playerData.notifyPlayer("$" + amount + " has been added to your account by an admin", server);
            return Text.literal("Gave $" + Math.abs(amount) + " to " + playerData.getUsername()).formatted(Formatting.GREEN);
        }
        else {
            playerData.notifyPlayer("$" + Math.abs(amount) + " has been removed from your account by an admin", server);
            return Text.literal("Removed $" + Math.abs(amount) + " from " + playerData.getUsername()).formatted(Formatting.RED);
        }
    }

    public void adjust(UUID playerUuid, double amount) {
        amount = (amount > 0.0) ? (Math.floor(amount * 100.0) / 100.0) : (Math.ceil(amount * 100.0) / 100.0);
        if (amount == 0.0) {
            return;
        }

        PlayerData playerData = players.get(playerUuid);
        if (Objects.isNull(playerData)) {
            return;
        }
        playerData.addMoney(amount);
    }

    public Text setCmd(UUID playerUuid, double amount, MinecraftServer server) {
        amount = Math.floor(amount * 100.0) / 100.0;

        PlayerData playerData = players.get(playerUuid);
        if (Objects.isNull(playerData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }

        playerData.setMoney(amount);
        playerData.notifyPlayer("Your balance has been set to $" + amount + " by an admin", server);
        return Text.literal("Set " + playerData.getUsername() + "'s balance to $" + amount).formatted(Formatting.GREEN);
    }

    public Text balanceCmd(UUID playerUuid) {
        PlayerData playerData = players.get(playerUuid);

        if (Objects.isNull(playerData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }

        return Text.empty().append(Text.literal("You have ").formatted(Formatting.GOLD)).append(Text.literal("$" + playerData.getMoney()).formatted((playerData.getMoney() > 0) ? Formatting.GREEN : Formatting.RED));
    }

    public Text balanceCmd(UUID playerUuid, boolean isPlayer) {
        PlayerData playerData = players.get(playerUuid);

        if (Objects.isNull(playerData)) {
            return Text.literal("Player not found").formatted(Formatting.DARK_RED);
        }

        return Text.empty().append(Text.literal((isPlayer) ? "You have " : playerData.getUsername() + " has ").formatted(Formatting.GOLD)).append(Text.literal("$" + playerData.getMoney()).formatted((playerData.getMoney() > 0) ? Formatting.GREEN : Formatting.RED));
    }

    public Text gambleCmd(UUID playerUuid, double amount) {
        amount = Math.floor(amount * 100.0) / 100.0;
        PlayerData playerData = players.get(playerUuid);
        if (amount < 0.0) {
            return Text.literal("You must gamble an amount more than 0").formatted(Formatting.DARK_RED);
        }

        if (playerData.getMoney() < amount) {
            return Text.literal("You do not have enough money").formatted(Formatting.DARK_RED);
        }

        double winnings = Math.floor(amount * 80.0) / 100.0;
        double losings = Math.floor(amount * 50.0) / 100.0;
        Random random = new Random();

        // randomInt can be from 0-2
        int randomInt = random.nextInt(3);
        if (randomInt == 2) {
            playerData.addMoney(winnings);
            return Text.literal("You won $"+winnings+"!").formatted(Formatting.GREEN);
        }

        // randomInt2 can be from 0-1
        int randomInt2 = random.nextInt(2);
        if (randomInt2 == 1) {
            playerData.subtractMoney(losings);
            return Text.literal("You lost $"+ losings +"!").formatted(Formatting.RED);
        }

        playerData.subtractMoney(amount);
        return Text.literal("You lost it all..").formatted(Formatting.DARK_RED);
    }

    public double getPlayerBalance(UUID playerUuid) {
        PlayerData playerData = players.get(playerUuid);
        if (Objects.isNull(playerData))
            return 0.0;

        return playerData.getMoney();
    }
}
