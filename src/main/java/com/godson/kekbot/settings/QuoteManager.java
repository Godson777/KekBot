package com.godson.kekbot.settings;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuoteManager {
    @SerializedName("list")
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

    public List<String> search(String quote) {
        List <String> retList = new ArrayList<>();
        String reg = "(?i).*(" + quote + ").*";

        for(int i = 0; i < quotes.size(); i++){
            if(quotes.get(i).toString().matches(reg)){
                retList.add("`" + Integer.toString(i + 1) + ".` " + quotes.get(i).toString());
            }
        }
        return retList;
    }

    public String quote() {
        Random random = new Random();
        int index = random.nextInt(quotes.size());
        return quotes.get(index);
    }

    public String getQuote(int quoteNumber){
        return quotes.get(quoteNumber);
    }

    public void editQuote(int quoteNumber, String quote){
        quotes.set(quoteNumber, quote);
    }

    public void removeQuote(int quoteNumber) {
        quotes.remove(quoteNumber);
    }
}
