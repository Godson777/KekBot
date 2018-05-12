package com.godson.kekbot;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LocaleUtils {

    private static final String bundle = "locale.KekBot";

    public static String getString(String id, Locale locale) {
        try {
            ResourceBundle labels = ResourceBundle.getBundle("locale.KekBot", locale);
            return labels.getString(id);
        } catch (MissingResourceException e) {
            return id;
        }
    }
}
