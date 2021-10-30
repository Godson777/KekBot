package com.godson.kekbot.objects;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class MarkovChain {

    private final BreakIterator sentenceIterator = BreakIterator.getSentenceInstance();
    private final BreakIterator wordIterator = BreakIterator.getWordInstance();

    private final Map<String, List<String>> singleWords = new TreeMap<>();
    private final Map<String, List<String>> dictionary = new TreeMap<>();

    private final Random random = new Random();

    private final String[] blacklistedWords = {"nigger", "isis", "jews", "faggot", "cripple"};

    public MarkovChain() {

    }

    public String removeBlacklistedWords(String string) {
        for (String word : blacklistedWords) {
            if (string.contains(word)) string = string.replaceAll(word, "");
        }
        return string;
    }

    public void addDictionary(String string) {
        string = removeBlacklistedWords(string.toLowerCase().trim())
                .replaceAll("((?:https://)|(?:http://)|(?:www\\.))?([a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,3}(?:\\??)[a-zA-Z0-9\\-._?,'/\\\\+&%$#=~]+)", "")
                .replaceAll(":[a-zA-Z0-9_\\-~]+:", "");
        for (final String sentence : splitSentences(string)) {
            String lastWord = null, lastLastWord = null;
            for (final String word : addDots(splitWords(sentence))) {
                if (lastLastWord != null) {
                    final String key = lastLastWord + ' ' + lastWord;
                    List<String> value = dictionary.get(key);
                    if (value == null)
                        value = new ArrayList<>();
                    value.add(word);
                    dictionary.put(key, value);
                }
                if (lastWord != null) {
                    final String key = lastWord;
                    List<String> value = singleWords.get(key);
                    if (value == null)
                        value = new ArrayList<>();
                    value.add(word);
                    singleWords.put(key, value);
                }
                lastLastWord = lastWord;
                lastWord = word;
            }
        }
    }

    private List<String> splitSentences(final String string) {
        sentenceIterator.setText(string);
        final List<String> sentences = new ArrayList<>();
        for (int start = sentenceIterator.first(), end = sentenceIterator.next(); end != BreakIterator.DONE; start = end, end = sentenceIterator.next()) {
            sentences.add(string.substring(start, end).trim());
        }
        return sentences;
    }

    private List<String> splitWords(final String string) {
        wordIterator.setText(string);
        final List<String> words = new ArrayList<>();
        for (int start = wordIterator.first(), end = wordIterator.next(); end != BreakIterator.DONE; start = end, end = wordIterator.next()) {
            String word = string.substring(start, end).trim();
            if (word.length() > 0 && Character.isLetterOrDigit(word.charAt(0)))
                words.add(word);
        }
        return words;
    }

    private List<String> addDots(List<String> words) {
        words.add(0, ".");
        words.add(".");
        return words;
    }

    public String generateSentence(int count) {
        StringBuilder target = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String w1 = ".";
            String w2 = pickRandom(singleWords.get(w1));
            while (w2 != null) {
                    target.append(w2).append(w2.equals(".") ? "" : " ");
                if (w2.equals(".")) {
                    if (target.toString().contains(" ")) target.deleteCharAt(target.lastIndexOf(" "));
                    break;
                }
                String w3 = pickRandom(dictionary.get(w1 + " " + w2));
                w1 = w2;
                w2 = w3;
            }
            if (i < count+1) target.append("\n");
        }
        return target.toString();
    }

    private String pickRandom(List<String> alternatives) {
        return alternatives.get(random.nextInt(alternatives.size()));
    }
}
