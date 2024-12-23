package ong.aldenw.moolah.handlers;

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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.moolah.PluginState;
import ong.aldenw.moolah.formats.RgbIntFormat;

import java.util.ArrayList;

public class ExchangeHandler {
    private final ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();
    private boolean recentlySorted = false;

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
        recentlySorted = false;
        cost = Math.floor(cost * 100.0) / 100.0;
        if (cost <= 0.0) return Text.literal("Cost must be more than 0").formatted(Formatting.DARK_RED);
        for (ExchangeRate rate : exchangeRates) {
            if (item.equals(rate.item)) {
                if (rate.cost != cost)
                    rate.cost = cost;
                return Text.empty().append(Text.literal("Set ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal("'s cost to $" + cost).formatted(Formatting.GOLD));
            }
        }
        exchangeRates.add(new ExchangeRate(item, cost));
        return Text.empty().append(Text.literal("Set ").formatted(Formatting.GOLD)).append(item.getName()).append(Text.literal("'s cost to $" + cost).formatted(Formatting.GOLD));
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

    public Text forMoney(Item item, int amount, ServerPlayerEntity player, MinecraftServer server) {
        double cost = getCostForItem(item) * amount;
        cost = Math.floor(cost * 100.0) / 100.0;
        int inventoryCount = player.getInventory().count(item);

        if (amount <= 0)
            return Text.literal("Amount must be greater than 0").formatted(Formatting.DARK_RED);
        if (cost < 0)
            return Text.literal("Item is not exchangeable").formatted(Formatting.DARK_RED);
        if (inventoryCount < amount)
            return Text.empty().append(Text.literal("You do not have enough ").formatted(Formatting.DARK_RED)).append(item.getName());

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
        return Text.empty().append(Text.literal("Exchanged ").formatted(Formatting.GOLD)).append(Text.literal(amount+" ").formatted(Formatting.YELLOW)).append(item.getName()).append(Text.literal(" for ").formatted(Formatting.GOLD)).append(Text.literal("$"+cost).formatted(Formatting.GREEN));
    }

    public Text forItem(ItemStackArgument item, int amount, ServerPlayerEntity player, MinecraftServer server) throws CommandSyntaxException {
        BankHandler bankHandler = PluginState.get(server).bankHandler;
        double cost = getCostForItem(item.getItem()) * amount;
        cost = Math.floor(cost * 100.0) / 100.0;

        if (amount <= 0)
            return Text.literal("Amount must be greater than 0").formatted(Formatting.DARK_RED);
        if (cost < 0)
            return Text.literal("Item is not exchangeable").formatted(Formatting.DARK_RED);
        if (bankHandler.getPlayerBalance(player.getUuid()) < cost)
            return Text.literal("You do not have enough money").formatted(Formatting.DARK_RED);

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
        return Text.empty().append(Text.literal("Exchanged ").formatted(Formatting.GOLD)).append(Text.literal("$"+cost).formatted(Formatting.GREEN)).append(Text.literal(" for ").formatted(Formatting.GOLD)).append(Text.literal(amount+" ").formatted(Formatting.YELLOW)).append(item.getItem().getName());
    }

    public Text getRates() {
        if (!recentlySorted) {
            sortRates(0, exchangeRates.size() - 1);
            recentlySorted = true;
        }

        MutableText result = Text.empty().append(Text.literal("\nCurrent exchange rates:\n").formatted(Formatting.GOLD));
        for (ExchangeRate rate : exchangeRates) {
            result.append(Text.literal("One ").formatted(Formatting.GOLD)).append(rate.item.getName()).append(Text.literal(" for ").formatted(Formatting.GOLD)).append(Text.literal("$"+rate.cost).withColor(colorCodeCost(rate.cost))).append(Text.literal("\n"));
        }
        return result;
    }

    public double getCostForItem(Item item) {
        for (ExchangeRate rate : exchangeRates) {
            if (item.equals(rate.item)) {
                return rate.cost;
            }
        }
        return -1;
    }

    public ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<>();
        for (ExchangeRate rate : exchangeRates) {
            items.add(rate.item);
        }
        return items;
    }

    private int colorCodeCost(double cost) {
        if (cost < 100) {
            int intensity = 255 - Math.min(((int)(155 * (cost / 100))), 155);
            return RgbIntFormat.fromThree(0, intensity, 0);
        }

        if (cost < 1000) {
            int intensity = 255 - Math.min(((int)(155 * (cost / 1000))), 155);
            return RgbIntFormat.fromThree(intensity, intensity, 0);
        }

        int intensity = 255 - Math.min(((int)(155 * (cost / 1000))), 170);
        return RgbIntFormat.fromThree(intensity, 0, 0);
    }

    // Alphabetically sorting rates using quicksort (this is just an excuse to learn quicksort)

    private void sortRates(int low, int high) {
        if (low < high) {
            int partition = partitionRates(low, high);
            sortRates(low, partition - 1);
            sortRates(partition + 1, high);
        }
    }

    private int partitionRates(int low, int high) {
        String pivot = exchangeRates.get(high).item.getName().getString();
        int i = low - 1;

        for (int j = low; j <= high-1; j++) {
            if (exchangeRates.get(j).item.getName().getString().compareTo(pivot) < 0) {
                i++;
                swap(i, j);
            }
        }

        swap(i+1, high);
        return i+1;
    }

    private void swap(int from, int to) {
        ExchangeRate hold = exchangeRates.get(from);
        exchangeRates.set(from, exchangeRates.get(to));
        exchangeRates.set(to, hold);
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
            return nbtCompound;
        }
    }
}
