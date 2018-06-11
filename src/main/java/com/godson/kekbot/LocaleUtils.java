package com.godson.kekbot;

import net.dv8tion.jda.core.utils.tuple.Pair;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class LocaleUtils {

    private static final String bundle = "locale.KekBot";
    public static List<Pair<String, String>> languages = new ArrayList<>();
    static {
        languages.add(Pair.of("English", "en_US"));
        languages.add(Pair.of("English (L33tsp33k)", "en_l33t"));
        languages.add(Pair.of("English (SiivaGunner Edition)", "en_SIIVA"));
    }

    public static String getString(String unlocalizedMessage, String locale, Object... objects) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(LocaleUtils.bundle, new Locale(locale.substring(0, locale.indexOf("_")), locale.substring(locale.indexOf("_") + 1)));

            return new String(String.format(bundle.getString(unlocalizedMessage), objects).getBytes("ISO-8859-1"), "UTF-8");
        } catch (MissingResourceException | UnsupportedEncodingException e) {
            //In case of the event that there's a localized message missing in both the locale and default properties file, throw the unlocalized message at chat instead.
            //This'll also happen is an error happens.
            return unlocalizedMessage;
        }
    }

    public static String getPluralString(long amount, String unlocalizedMessage, String locale, Object... objects) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(LocaleUtils.bundle, new Locale(locale.substring(0, locale.indexOf("_")), locale.substring(locale.indexOf("_") + 1)));
            return new String(String.format(bundle.getString(unlocalizedMessage + (amount != 1 ? ".plural" : ".single")), objects).getBytes("ISO-8859-1"), "UTF-8");
        } catch (MissingResourceException | UnsupportedEncodingException e) {
            //In case of the event that there's a localized message missing in both the locale and default properties file, throw the unlocalized message at chat instead.
            //This'll also happen is an error happens.
            return unlocalizedMessage;
        }
    }
}
