package ong.aldenw.handlers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.PluginState;

import java.util.ArrayList;

public class ExchangeHandler {
    private final ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();

    public ExchangeHandler() {

    }

    public ExchangeHandler(NbtCompound nbtCompound) {
        NbtList ratesNbt = nbtCompound.getList("rates", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < ratesNbt.size(); i++) {
            exchangeRates.add(new ExchangeRate(ratesNbt.getCompound(i)));
        }
    }

    public NbtCompound writeNbt() {
        NbtCompound ehNbt = new NbtCompound();

        NbtList ratesNbt = new NbtList();
        for (ExchangeRate rate : exchangeRates) {
            ratesNbt.add(rate.writeNbt());
        }
        ehNbt.put("rates", ratesNbt);

        return ehNbt;
    }

    public Text addRate(Item item, double cost) {
        cost = Math.floor(cost * 100.0) / 100.0;
        if (cost <= 0.0) return Text.literal("Cost must be more than 0").formatted(Formatting.DARK_RED);
        for (ExchangeRate rate : exchangeRates) {
            if (item.equals(rate.item)) {
                if (rate.cost != cost)
                    rate.cost = cost;
                return Text.empty().append(Text.literal("Set ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal("'s cost to " + cost).formatted(Formatting.GOLD));
            }
        }
        exchangeRates.add(new ExchangeRate(item, cost));
        return Text.empty().append(Text.literal("Set ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal("'s cost to " + cost).formatted(Formatting.GOLD));
    }

    public Text removeRate(Item item) {
        for (ExchangeRate rate : exchangeRates) {
            if (item.equals(rate.item)) {
                exchangeRates.remove(rate);
                return Text.empty().append(Text.literal("Removed ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal("'s exchange rate").formatted(Formatting.GOLD));
            }
        }
        return Text.literal("Exchange rate not found").formatted(Formatting.DARK_RED);
    }

    public Text forFunds(Item item, int amount, ServerPlayerEntity player, MinecraftServer server) {
        double cost = getCostForItem(item) * amount;
        int inventoryCount = player.getInventory().count(item);

        if (amount <= 0)
            return Text.literal("Amount must be greater than 0").formatted(Formatting.DARK_RED);
        if (cost < 0)
            return Text.literal("Item is not exchangeable").formatted(Formatting.DARK_RED);
        if (inventoryCount < amount)
            return Text.literal("You do not have enough " + item.getName().getString() + "s").formatted(Formatting.DARK_RED);

        int remaining = amount;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack currentStack = player.getInventory().getStack(i);
            if (currentStack.getItem().equals(item)) {
                if (currentStack.getCount() >= remaining) {
                    currentStack.decrement(remaining);
                    break;
                }
                else {
                    remaining -= currentStack.getCount();
                    player.getInventory().removeStack(i);
                }
            }
        }

        PluginState.get(server).bankHandler.adjust(player.getUuid(), cost);
        return Text.literal("Exchanged x" + amount + " " + item.getName().getString() + "(s) for $" + cost).formatted(Formatting.GOLD);
    }

    public Text forItem(ItemStackArgument item, int amount, ServerPlayerEntity player, MinecraftServer server) throws CommandSyntaxException {
        BankHandler bankHandler = PluginState.get(server).bankHandler;
        double cost = getCostForItem(item.getItem()) * amount;

        if (amount <= 0)
            return Text.literal("Amount must be greater than 0").formatted(Formatting.DARK_RED);
        if (cost < 0)
            return Text.literal("Item is not exchangeable").formatted(Formatting.DARK_RED);
        if (bankHandler.getPlayerBalance(player.getUuid()) < cost)
            return Text.literal("You do not have enough funds").formatted(Formatting.DARK_RED);

        ItemStack items = item.createStack(amount, false);
        boolean dropAll = player.getInventory().insertStack(items);

        if (!dropAll || !items.isEmpty()) {
            ItemEntity itemEntity = player.dropItem(items, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }

        PluginState.get(server).bankHandler.adjust(player.getUuid(), -1*cost);
        return Text.literal("Exchanged $" + cost + " for x" + amount + " " + item.getItem().getName().getString() + "s").formatted(Formatting.GOLD);
    }

    public double getCostForItem(Item item) {
        for (ExchangeRate rate : exchangeRates) {
            if (item.equals(rate.item)) {
                return rate.cost;
            }
        }
        return -1;
    }

    public ArrayList<Item> getItemsForCost(double amount) {
        ArrayList<Item> items = new ArrayList<>();
        for (ExchangeRate rate : exchangeRates) {
            if (rate.cost <= amount) {
                items.add(rate.item);
            }
        }
        return items;
    }

    public ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<>();
        for (ExchangeRate rate : exchangeRates) {
            items.add(rate.item);
        }
        return items;
    }

    public static class ExchangeRate {
        public Item item;
        public double cost;

        public ExchangeRate(Item item, double cost) {
            this.item = item;
            this.cost = cost;
        }

        public ExchangeRate(NbtCompound nbtCompound) {
            this.item = Item.byRawId(nbtCompound.getInt("itemId"));
            this.cost = nbtCompound.getDouble("cost");
        }

        public NbtCompound writeNbt() {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putInt("itemId", Item.getRawId(item));
            nbtCompound.putDouble("cost", cost);
            System.out.println("building up compound type " + nbtCompound.getType());
            return nbtCompound;
        }
    }
}
