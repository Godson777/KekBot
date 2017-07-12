package com.godson.kekbot.Shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Shop<T> {
    private List<T> inventory = new ArrayList<T>();
    private Map<T, Integer> limitedQuantity = new HashMap<>();

    Shop() {}

    public List<T> getInventory() {
        return inventory;
    }

    void addToInventory(T item) {
        inventory.add(item);
    }

    public void addLimitedItem(T item, int quantity) {
        inventory.add(item);
        limitedQuantity.put(item, quantity);
    }

    public boolean isItemLimited(T item) {
        return limitedQuantity.containsKey(item);
    }
}
