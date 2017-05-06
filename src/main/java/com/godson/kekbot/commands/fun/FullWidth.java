package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import org.apache.commons.lang3.StringUtils;

public class FullWidth {
    private static String[] toReplace = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
            "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3",
            "4", "5", "6", "7", "8", "9", "0", "-", "=", "\\", "]", "[", "{", "}", "/", ",", ".", "!",
            "@", "#", "$", "%", "^", "&", "*", "(", ")", "~", "`", "\"", "'", " "};
    private static String[] replacements = {"ａ", "ｂ", "ｃ", "ｄ", "ｅ", "ｆ", "ｇ", "ｈ", "ｉ", "ｊ", "ｋ", "ｌ", "ｍ", "ｎ",
            "ｏ", "ｐ", "ｑ", "ｒ", "ｓ", "ｔ", "ｕ", "ｖ", "ｗ", "ｘ", "ｙ", "ｚ", "Ａ", "Ｂ", "Ｃ", "Ｄ", "Ｅ",
            "Ｆ", "Ｇ", "Ｈ", "Ｉ", "Ｊ", "Ｋ", "Ｌ", "Ｍ", "Ｎ", "Ｏ", "Ｐ", "Ｑ", "Ｒ", "Ｓ", "Ｔ", "Ｕ", "Ｖ",
            "Ｗ", "Ｘ", "Ｙ", "Ｚ", "１", "２", "３", "４", "５", "６", "７", "８", "９", "０", "－", "＝", "＼",
            "]" ,"[", "｛", "｝", "／", ",", "．", "！", "＠", "＃", "＄", "％", "＾", "＆", "＊", "（", "）", "~", "`", "”", "’", "　"};

    public static Command fullwidth = new Command("fullwidth")
            .withCategory(CommandCategory.FUN)
            .withUsage("{p}fullwidth <text>")
            .withDescription("Makes your text ＦＵＬＬＷＩＤＴＨ")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                if (rawSplit.length < 2) {
                    context.getTextChannel().sendMessage("Ｉ　ｓｕｐｐｏｓｅ　ｙｏｕ　ｄｏｎ’ｔ　ｗａｎｔ　ａｎｙｔｈｉｎｇ　ｃｏｎｖｅｒｔｅｄ，　ｆａｉｒ　ｅｎｏｕｇｈ．．．").queue();
                } else {
                    context.getTextChannel().sendMessage(StringUtils.replaceEach(rawSplit[1], toReplace, replacements)).queue();
                }
            });
}
