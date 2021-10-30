package com.godson.kekbot.games;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RussianRoulette extends Game {

    private final List<User> alive = new ArrayList<>();
    private final List<Boolean> bullets = new ArrayList<>();
    private final Random random = new Random();
    private final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);
    private int round = 0;
    private int noDeaths = 0;


    String[] preps = {"It's high noon...", "Say your prayers...", "Say hello to my little friend!", "It's over...", "You're mine...",
            "Goodbye, old friend...", "Kiss your ass goodbye...", "Reach for the sky...", "I've got a bullet with your name on it...", "fuck you die...",
            "See you in hell...", "Smile for the camera...", "It's pizza time...", "Vibe Check...",
            "When will you learn? *When will you learn?* ***That your actions have consequences!?***",
            "Wanna play the knife game?", "This town ain't big enough for the two of us!", "You better not die, I placed all my bets on you...",
            "Peace was never an option...", "Die, Potato!", "I baked you a pie..."};

    String[] misfires = {"Oh.", "Fuck.", "Shit.", "Well that was anti-climactic.", "Huh.",
            "Huh. Well lucky you.", "Well that sucked.", "This is a bruh moment.", "Nevermind.", "I'm sorry I ever doubted you.",
            "The game was rigged from the start.", "Guess this is your lucky day.", "You cheated.", "Wait shit, the bullet wasn't done downloading.", "ok",
            "No one can just deflect the emerald splash...", "Where'd the bullet go?", "aight im headin out",
            "Oh wait, this isn't a gun, this is my usb steering wheel...", "Oh, that wasn't a bullet. It was just a peanut.",
            "THAT'S NOT FAIR I MISCLICKED!", "This gun's so useless, I oughta use my shotgun!", "oi cunt what the fuck",
            "Damn these off-brand bullets...", "Hey, who's the dumbass that loaded this with blanks?!",
            "That was somehow more boring than the 2019 game awards..."};

    String[] successes = {"Oh hey, he's dead!", "Eh, I never liked that guy anyway.",
            "I hit somethin', but it wasn't quite what I was aiming for, so I guess I missed.",
            "Wait, this is a gun? Whoops!", "PogChamp!", "Oops, sorry, my finger slipped.", "Damn, this bullet wasn't for you.",
            "Hey so, I was promised there'd be pizza after this?", "We're still friends, right?", "Dammit, waste of a bullet...",
            "This is what you get for taking my donuts, Bill!", "I thought the safety was on!", "Oh, how the turn tables... Wait.",
            "They're in a better place now...", "Person.exe has crashed.", "Headshot.", "KARRRRRRRRL. THAT KILLS PEOPLE.",
            "Son of a bitch, Clyde! I told you! Get... get in the truck!", "ok", "Wait a sec, this isn't a knife...",
            "I thought this was a camera, shit.", "How do you like that, Obama? I PISSED ON THE MOON, YOU IDIOT!",
            "Sorry sire, it had to be done...", "Game Over. Well, it is for you anyway.", "Shit, I didn't need a murder to happen.",
            "You're too slow!", "Welp, there goes my clean criminal record...", "Fuck, now the FBI is on my tail!",
            "You said this gun was fake!", "Cleanup on aisle 5!", "This is America.",
            "Uh oh, you didn't purchase the \"Survive the Roulette\" DLC! Looks like you die.", "I thought this was a water gun...",
            "Hey! I didn't give you permission to die yet!", "...", "Ooooh that's gotta hurt.", "I didn't know hardcore mode was on...",
            "Alright! I think I killed the spy of the group!", "What? They were mafia!", "How dare you take my diamonds?",
            "**BANG!** ...What? I'm just making sure.", "You said you were in creative mode!", "Ah yes, just like the simulations.",
            "This was *nothing* like the simulations.", "\uD83E\uDD80 __THE EVIDENCE IS GONE!__ \uD83E\uDD80",
            "Wait a minute, this isn't a dream...", "Dio paid me in advance for this."};

    String[] starts = {"Let's get this show on the road!", "Some kid's gonna die tonight!", "Let's get this over with...",
            "Aight, one of y'all gotta die tonight.", "Go! 「Sex Pistols」!", "Alright, I'm doing this one ***blindfolded.***",
            "This time the gun's fully loaded...I think.", "I hope this isn't my replica toy gun...",
            "Don't bother finding me, I have 70 alternative accounts!", "Winner of this round gets a free chocolate bar!",
            "Whoever dies first wins!", "Time to improve my K/D ratio...", "Wait a sec... Didn't you guys die last round?",
            "Did you know that Nintendo Switch has ***games?*** Anyway, LET'S START!"};

    public RussianRoulette(TextChannel channel) {
        super(2, 10, false, channel, "Russian Roulette", true, false);
        isTranslatable = false;
        canQuit = false;
    }

    @Override
    public void startGame() {
        channel.sendMessage("Alright! Let's start the game!").queue();
        alive.addAll(players);
        prepareRound();
    }

    @Override
    public String getRules() {
        return "It's a russian roulette! Everyone takes a turn, waiting to see if luck is truly on their side. Last one left alive wins!";
    }

    private void prepareRound() {
        //Check if there's only one person alive.
        if (alive.size() == 1) {
            //That person wins.
            endGame(alive.get(0), players.size() * 2, players.size() * 2);
            return;
        }

        Collections.shuffle(alive);
        round++;
        loadGun();
        if (round % players.size() == 0) Precision.round(multiplier += (players.size() > 5 ? .4 : .2), 2);
        //Let's list our remaining players, and start the round.
        channel.sendMessage("Round " + round + "!\nPlayers remaining:\n\n" + StringUtils.join(alive.stream().map(User::getAsMention).collect(Collectors.toList()), ", ") + "\n\nRound starting in 5 seconds...")
                .queue(s -> timer.schedule(this::startRound, 5, TimeUnit.SECONDS));
    }

    private void startRound() {
        channel.sendMessage((noDeaths > 0 ? "We've gone " + noDeaths + (noDeaths > 1 ? " rounds" : " round") + " without a death.\n\n" : "") + starts[random.nextInt(starts.length)] + (multiplier > 1 ? " **" + getString("game.multiplier", multiplier) + "**" : "")
                + "\n\n" + getPrepMessage(0)).queue(m -> timer.schedule(() -> shoot(0, m), 3, TimeUnit.SECONDS));
    }

    private String getPrepMessage(int player) {
        return alive.get(player).getAsMention() + ", " + preps[random.nextInt(preps.length)];
    }

    private void shoot(int player, Message message) {
        if (bullets.get(player)) {
            alive.remove(player);
            message.editMessage(message.getContentRaw() + " **BANG!** " + successes[random.nextInt(successes.length)]).queue(m -> {
                noDeaths = 0;
                timer.schedule(this::prepareRound, 5, TimeUnit.SECONDS);
            });
        } else {
            message.editMessage(message.getContentRaw() + " *click.* " + misfires[random.nextInt(misfires.length)])
                    .queue(m -> timer.schedule(() -> nextShot(player, m), 3, TimeUnit.SECONDS));
        }


    }

    private void nextShot(int player, Message message) {
        int nextPlayer = ++player;
        if (nextPlayer > alive.size() - 1) {
            message.editMessage(message.getContentRaw() + "\n\nDamn, no one died.").queue();
            noDeaths++;
            timer.schedule(this::prepareRound, 3, TimeUnit.SECONDS);
            return;
        }

        loadGun();
        message.editMessage(message.getContentRaw() + "\n" + getPrepMessage(nextPlayer)).queue(m -> timer.schedule(() -> shoot(nextPlayer, m), 3, TimeUnit.SECONDS));
    }

    private void loadGun() {
        bullets.clear();
        int bullet = random.nextInt(alive.size());
        for (int i = 0; i < alive.size(); i++) {
            if (i == bullet) {
                if (random.nextInt(6) == 0) bullets.add(false);
                else bullets.add(true);
            }
            else bullets.add(false);
        }
    }
}
