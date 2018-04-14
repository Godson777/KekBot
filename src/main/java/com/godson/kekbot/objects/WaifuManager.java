package com.godson.kekbot.objects;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class WaifuManager {
    private Map<String, Integer> waifus = new HashMap<>();
    private Random random = new Random();

    public WaifuManager() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                waifus.clear();
            }
        }, TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1));
    }

    public void addWaifu(String waifu, int rating) {
        if (!waifus.containsKey(waifu)) {
            waifus.put(waifu, rating);
        }
    }

    public int rateWaifu(String waifu) {
        if (waifus.containsKey(waifu)) {
            return waifus.get(waifu);
        } else {
            int rating = random.nextInt(10) + 1;
            addWaifu(waifu, rating);
            return rating;
        }
    }
}
