package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Utils;

import java.util.Random;

public class SlotMachine {
    private static Random random = new Random();

    public static Command slotMachine = new Command("slots")
            .withCategory(CommandCategory.FUN)
            .withAliases("slot", "slotmachine")
            .withDescription("Plays a slot machine.")
            .withUsage("{p}slots <topkeks to bet>")
            .onExecuted(context -> {
                if (context.getArgs().length > 0) {
                    int toSpend;
                    try {
                        toSpend = Integer.valueOf(context.getArgs()[0]);
                    } catch (NumberFormatException e) {
                        context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + context.getArgs()[1] + "`")).queue();
                        return;
                    }
                    if (toSpend >= 25 && toSpend <= 200) {
                        Profile profile = Profile.getProfile(context.getAuthor());
                        if (!profile.canSpend(toSpend)) {
                            context.getTextChannel().sendMessage("You don't have that many topkeks to bet with.").queue();
                            return;
                        }
                        profile.spendTopKeks(toSpend);
                        SlotValue slot1 = getRandomSlot();
                        SlotValue slot2 = getRandomSlot();
                        SlotValue slot3 = getRandomSlot();
                        context.getTextChannel().sendMessage(
                                (isMatch(slot1, slot2, slot3) ? "You won! You've received " + CustomEmote.printPrice(toSpend * slot1.getMultiplier()) + " as payment. (" + CustomEmote.printPrice(toSpend * (slot1.getMultiplier() - 1)) + " actual gain.)" : "Darn, better luck next time...") +
                                "\n" + CustomEmote.BLANK + slot1.getPrev().getEmote() + slot2.getPrev().getEmote() + slot3.getPrev().getEmote() + "\n" +
                               CustomEmote.RIGHTHAND + slot1.getEmote() + slot2.getEmote() + slot3.getEmote() + CustomEmote.LEFTHAND + "\n" +
                                CustomEmote.BLANK + slot1.getNext().getEmote() + slot2.getNext().getEmote() + slot3.getNext().getEmote()).queue();
                        if (isMatch(slot1, slot2, slot3)) profile.addTopKeks(toSpend * slot1.getMultiplier());
                        profile.save();
                    } else context.getTextChannel().sendMessage("You can only spend between 25 and 200 topkeks per spin.").queue();
                } else context.getTextChannel().sendMessage("You have to supply how many topkeks you're willing to spend with this slot machine.").queue();
            });

    private static boolean isMatch(SlotValue slot1, SlotValue slot2, SlotValue slot3) {
        return slot1 == slot2 && slot1 == slot3;
    }

    private static SlotValue getRandomSlot() {
        return SlotValue.values()[random.nextInt(SlotValue.values().length)];
    }

}


enum SlotValue {
    SALT(1.25, "<:PJSalt:259843755207688192>"),
    GRAND(1.50, "<:GRAND:230132601707429889>"),
    KAPPA(1.75, "<:Kappa:273978011395686400>"),
    GRAND_7(2, "<:slot7:370034694059851776>");

    private double multiplier;
    private String emote;

    SlotValue(double multiplier, String emote) {
        this.multiplier = multiplier;
        this.emote = emote;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getEmote() {
        return emote;
    }

    public SlotValue getNext() {
        if (this == values()[values().length-1]) return values()[0];
        else return values()[this.ordinal()+1];
    }

    public SlotValue getPrev() {
        if (this == values()[0]) return values()[values().length-1];
        else return values()[this.ordinal()-1];
    }
}