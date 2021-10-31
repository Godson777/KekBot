package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Eval extends Command {

    public Eval() {
        name = "eval";
        category = new Category("Bot Owner");
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            try {
                engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util, com.godson.kekbot);");
            } catch (ScriptException e) {
                e.printStackTrace();
            }
            try {
                engine.put("command", this);
                engine.put("event", event);
                engine.put("client", event.getClient());
                engine.put("guild", event.getGuild());
                engine.put("channel", event.getTextChannel());
                engine.put("jda", event.getJDA());
                Object out = engine.eval(
                        "(function() {" +
                                "with (imports) {" +
                                event.combineArgs() +
                                "}" +
                                "})();");
                event.getChannel().sendMessage(out == null ? "`Success! (Unless you're trying to find an object, then it failed...)`" : "`" + out + "`").queue();
            } catch (ScriptException e) {
                event.getChannel().sendMessage("```js\n" + e.getMessage() + "```").queue();
            } catch (Exception e) {
                event.getChannel().sendMessage("Exception was thrown:\n```java\n" + e + "```").queue();
            }
        } else event.getChannel().sendMessage("No arguments.").queue();
    }
}
