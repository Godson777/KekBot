package com.godson.kekbot.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuoteManager {
    private List<String> quotes;

    public QuoteManager() {
        quotes = new ArrayList<>();
    }

    public QuoteManager(List<String> quotes) {
        this.quotes = quotes;
    }

    public void addQuote(String quote) {
        this.quotes.add(quote);
    }

    public List<String> getList() {
        return quotes;
    }

    public String getQuote() {
        Random random = new Random();
        int index = random.nextInt(quotes.size());
        return quotes.get(index);
    }

    public String getQuote(int quoteNumber){
        return quotes.get(quoteNumber);
    }

    public void removeQuote(int quoteNumber) {
        quotes.remove(quoteNumber);
    }
}
