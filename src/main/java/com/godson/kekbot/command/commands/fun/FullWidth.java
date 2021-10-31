package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import org.apache.commons.lang3.StringUtils;

public class FullWidth extends Command {
    private final String[] toReplace = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
            "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3",
            "4", "5", "6", "7", "8", "9", "0", "-", "=", "\\", "]", "[", "{", "}", "/", ",", ".", "!", "?",
            "@", "#", "$", "%", "^", "&", "*", "(", ")", "~", "`", "\"", "'", " "};
    private final String[] replacements = {"ａ", "ｂ", "ｃ", "ｄ", "ｅ", "ｆ", "ｇ", "ｈ", "ｉ", "ｊ", "ｋ", "ｌ", "ｍ", "ｎ",
            "ｏ", "ｐ", "ｑ", "ｒ", "ｓ", "ｔ", "ｕ", "ｖ", "ｗ", "ｘ", "ｙ", "ｚ", "Ａ", "Ｂ", "Ｃ", "Ｄ", "Ｅ",
            "Ｆ", "Ｇ", "Ｈ", "Ｉ", "Ｊ", "Ｋ", "Ｌ", "Ｍ", "Ｎ", "Ｏ", "Ｐ", "Ｑ", "Ｒ", "Ｓ", "Ｔ", "Ｕ", "Ｖ",
            "Ｗ", "Ｘ", "Ｙ", "Ｚ", "１", "２", "３", "４", "５", "６", "７", "８", "９", "０", "－", "＝", "＼",
            "]" ,"[", "｛", "｝", "／", ",", "．", "！", "？", "＠", "＃", "＄", "％", "＾", "＆", "＊", "（", "）", "~", "`", "”", "’", "　"};

    public FullWidth() {
        name = "fullwidth";
        description = "Makes your text ＦＵＬＬＷＩＤＴＨ.";
        usage.add("fullwidth <text>");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 5;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length == 0) {
            event.getChannel().sendMessage("Ｉ　ｓｕｐｐｏｓｅ　ｙｏｕ　ｄｏｎ’ｔ　ｗａｎｔ　ａｎｙｔｈｉｎｇ　ｃｏｎｖｅｒｔｅｄ，　ｆａｉｒ　ｅｎｏｕｇｈ．．．").queue();
        } else {
            event.getChannel().sendMessage(StringUtils.replaceEach(event.combineArgs(), toReplace, replacements)).queue();
        }
    }
}
