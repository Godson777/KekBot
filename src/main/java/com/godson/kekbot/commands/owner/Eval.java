package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Eval {
    public static Command eval = new Command("eval")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                    String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                    try {
                        engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util);");
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                    if (rawSplit.length == 1) {
                        context.getTextChannel().sendMessage("u wot?").queue();
                    } else {
                        try {
                            engine.put("context", context);
                            engine.put("guild", context.getGuild());
                            engine.put("channel", context.getTextChannel());
                            engine.put("jda", context.getJDA());
                            Object out = engine.eval(
                                    "(function() {" +
                                            "with (imports) {" +
                                            rawSplit[1] +
                                            "}" +
                                            "})();");
                            context.getTextChannel().sendMessage(out == null ? "`Success! (Unless you're trying to find an object, then it failed...)`" : "`" + out.toString() + "`").queue();
                        } catch (ScriptException e) {
                            context.getTextChannel().sendMessage("```js\n" + e.getMessage() + "```").queue();
                        } catch (Exception e) {
                            context.getTextChannel().sendMessage("Exception was thrown:\n```java\n" + e + "```").queue();
                        }
                    }
                }
            });
}
