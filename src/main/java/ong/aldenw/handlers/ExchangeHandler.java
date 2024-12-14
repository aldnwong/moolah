package ong.aldenw.handlers;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

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

    public void addRate(Item item, double cost) {
        if (cost < 0.0) return;
        for (ExchangeRate rate : exchangeRates) {
            if (item.equals(rate.item)) {
                if (rate.cost != cost)
                    rate.cost = cost;
                return;
            }
        }
        exchangeRates.add(new ExchangeRate(item, cost));
    }

    public void removeRate(Item item) {
        for (ExchangeRate rate : exchangeRates) {
            if (item.equals(rate.item)) {
                exchangeRates.remove(rate);
                return;
            }
        }
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
