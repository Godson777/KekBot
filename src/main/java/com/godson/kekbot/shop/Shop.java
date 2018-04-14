package com.godson.kekbot.shop;

import com.godson.kekbot.profile.Profile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Shop<T> {
    protected List<T> inventory = new ArrayList<T>();
    private Map<T, Integer> limitedQuantity = new HashMap<>();

    protected final ShelfType shelfType;
    protected final int height;
    protected final int width;
    protected final int itemsPerPage;

    protected final BufferedImage prevImg = ImageIO.read(new File("resources/shop/prev.png"));
    protected final BufferedImage nextImg = ImageIO.read(new File("resources/shop/next.png"));
    protected final BufferedImage topkek = ImageIO.read(new File("resources/shop/topkek.png"));

    Shop(ShelfType shelfType, int height, int width, int itemsPerPage) throws IOException {
        this.shelfType = shelfType;
        this.height = height;
        this.width = width;
        this.itemsPerPage = itemsPerPage;
    }

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

    private ShelfType getShelfType() {
        return shelfType;
    }

    protected int getNumberOfPages() {
        int pages = 0;
        int items = inventory.size();
        while (items > itemsPerPage) {
            items -= itemsPerPage;
            pages++;
        }
        return pages + 1;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public abstract List<byte[]> draw(Profile profile) throws Exception;

    protected enum ShelfType {
        SHELF_3("resources/shop/3shelf.png", 3);

        private String file;
        private int shelves;

        ShelfType(String file, int shelves) {
            this.file = file;
            this.shelves = shelves;
        }

        public String getFile() {
            return file;
        }

        public int getShelves() {
            return shelves;
        }
    }
}
