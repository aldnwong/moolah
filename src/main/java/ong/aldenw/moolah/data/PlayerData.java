package ong.aldenw.moolah.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class PlayerData {
    private String username;
    private final UUID uuid;
    private final ArrayList<String> notifications;
    private double money;

    public PlayerData(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
        this.money = 0.0;
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

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }

    public double getMoney() {
        return this.money;
    }

    public void setMoney(double money) {
        this.money = Math.floor(money * 100.0) / 100.0;
    }

    public void addMoney(double amount) {
        this.money = Math.floor((this.money + amount) * 100.0) / 100.0;
    }

    public void subtractMoney(double amount) {
        this.money = Math.floor((this.money - amount) * 100.0) / 100.0;
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
