package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Emojify {
    private static final String[] REPLACEMENT = new String[Character.MAX_VALUE+1];
    static {
        for(int i=Character.MIN_VALUE;i<=Character.MAX_VALUE;i++)
            REPLACEMENT[i] = Character.toString(Character.toLowerCase((char) i));
        REPLACEMENT['a'] =  "\uD83C\uDDE6 ";
        REPLACEMENT['b'] =  "\uD83C\uDDE7 ";
        REPLACEMENT['c'] = "\uD83C\uDDE8 ";
        REPLACEMENT['d'] = "\uD83C\uDDE9 ";
        REPLACEMENT['e'] = "\uD83C\uDDEA ";
        REPLACEMENT['f'] = "\uD83C\uDDEB ";
        REPLACEMENT['g'] = "\uD83C\uDDEC ";
        REPLACEMENT['h'] = "\uD83C\uDDED ";
        REPLACEMENT['i'] = "\uD83C\uDDEE ";
        REPLACEMENT['j'] = "\uD83C\uDDEF ";
        REPLACEMENT['k'] = "\uD83C\uDDF0 ";
        REPLACEMENT['l'] = "\uD83C\uDDF1 ";
        REPLACEMENT['m'] = "\uD83C\uDDF2 ";
        REPLACEMENT['n'] = "\uD83C\uDDF3 ";
        REPLACEMENT['o'] = "\uD83C\uDDF4 ";
        REPLACEMENT['p'] = "\uD83C\uDDF5 ";
        REPLACEMENT['q'] = "\uD83C\uDDF6 ";
        REPLACEMENT['r'] = "\uD83C\uDDF7 ";
        REPLACEMENT['s'] = "\uD83C\uDDF8 ";
        REPLACEMENT['t'] = "\uD83C\uDDF9 ";
        REPLACEMENT['u'] = "\uD83C\uDDFA ";
        REPLACEMENT['v'] = "\uD83C\uDDFB ";
        REPLACEMENT['w'] = "\uD83C\uDDFC ";
        REPLACEMENT['x'] = "\uD83C\uDDFD ";
        REPLACEMENT['y'] = "\uD83C\uDDFE ";
        REPLACEMENT['z'] = "\uD83C\uDDFF ";
        REPLACEMENT['0'] = "0⃣ ";
        REPLACEMENT['1'] = "1⃣ ";
        REPLACEMENT['2'] = "2⃣ ";
        REPLACEMENT['3'] = "3⃣ ";
        REPLACEMENT['4'] = "4⃣ ";
        REPLACEMENT['5'] = "5⃣ ";
        REPLACEMENT['6'] = "6⃣ ";
        REPLACEMENT['7'] = "7⃣ ";
        REPLACEMENT['8'] = "8⃣ ";
        REPLACEMENT['9'] = "9⃣ ";
        REPLACEMENT['!'] = "❗ ";
        REPLACEMENT['?'] = "❓ ";
        REPLACEMENT['-'] = "";
        REPLACEMENT[','] = "";
        REPLACEMENT['.'] = "";
        REPLACEMENT['\''] = "";
        REPLACEMENT['\"'] = "";
    }

    public static String emojify(String word) {
        StringBuilder sb = new StringBuilder(word.length());
        for(int i=0;i<word.length();i++)
            sb.append(REPLACEMENT[word.toLowerCase().charAt(i)]);
        return sb.toString();
    }

    public static Command emojify = new Command("emojify")
            .withAliases("emoji")
            .withCategory(CommandCategory.FUN)
            .withDescription("Converts your text message to a message persisting of emojis. (May go through changes later on.)")
            .withUsage("{p}emojify <messge>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getTextChannel().sendMessageAsync("No message specified! :cry:", null);
                } else {
                    context.getTextChannel().sendMessageAsync(emojify(rawSplit[1]), null);
                }
            });
}
