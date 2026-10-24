/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.entity.Player
 */
package de.nick.casino;

import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

public final class Msg {
    private static final Map<String, String> DE = new HashMap<String, String>();
    public static final Key ICON_FONT = Key.key((String)"casino", (String)"icons");
    public static final TextColor GOLD = TextColor.color((int)0xFFAA00);

    private Msg() {
    }

    public static String raw(String string, Object ... objectArray) {
        String string2 = DE.getOrDefault(string, string);
        return objectArray.length == 0 ? string2 : String.format(string2, objectArray);
    }

    public static Component tr(String string, Object ... objectArray) {
        return Component.text((String)Msg.raw(string, objectArray), (TextColor)NamedTextColor.WHITE);
    }

    public static Component maxStake(long l) {
        if (l <= 0L) {
            return Component.text((String)Msg.raw("message.casino.bank_empty", new Object[0]), (TextColor)NamedTextColor.RED);
        }
        return Component.text((String)Msg.raw("message.casino.max_stake", l), (TextColor)NamedTextColor.RED);
    }

    public static void actionbar(Player player, Component component) {
        player.sendActionBar(component);
    }

    public static void chat(Player player, String string, Object ... objectArray) {
        player.sendMessage(Component.text((String)"[Casino] ", (TextColor)GOLD).append((Component)Component.text((String)Msg.raw(string, objectArray), (TextColor)NamedTextColor.WHITE)));
    }

    public static Component diamondIcon() {
        return ((TextComponent)Component.text((String)"\ue000").font(ICON_FONT)).color((TextColor)NamedTextColor.WHITE);
    }

    public static String colorName(int n) {
        return switch (n) {
            case 0 -> "ROT";
            case 1 -> "SCHWARZ";
            default -> "GR\u00dcN";
        };
    }

    static {
        DE.put("message.roulettecasino.no_bet", "Setze einen Einsatz, bevor du das Roulette drehst.");
        DE.put("gui.roulettecasino.roulette.several", "Nur eine Farbe behalten");
        DE.put("message.roulettecasino.slot_machine.no_bet", "Setze einen Einsatz, bevor du den Hebel ziehst.");
        DE.put("message.roulettecasino.slot_machine.busy", "Der Automat dreht sich.");
        DE.put("message.roulettecasino.slot_machine.spacing", "Lass zwischen zwei Spielautomaten einen Block frei.");
        DE.put("message.casino.roulette_busy", "Das Roulette dreht sich.");
        DE.put("message.casino.in_use", "%s spielt gerade an dieser Maschine.");
        DE.put("message.casino.only_diamonds", "Als Einsatz gehen nur Diamanten.");
        DE.put("message.casino.no_space", "Hier ist nicht genug Platz f\u00fcr diese Maschine.");
        DE.put("message.casino.need_floor", "Der Spielautomat braucht festen Boden.");
        DE.put("message.casino.placed", "Maschine aufgestellt. Du bist die Bank: Schleichen + Rechtsklick \u00f6ffnet die Bank. Zahle Diamanten ein, damit Gewinne ausgezahlt werden k\u00f6nnen.");
        DE.put("message.casino.removed", "Maschine abgebaut. Du hast die Maschine und %s Diamanten aus der Bank zur\u00fcckbekommen.");
        DE.put("message.casino.removed_admin", "Maschine von %s entfernt. Maschine und Bankinhalt (%s Diamanten) werden dem Besitzer \u00fcbergeben.");
        DE.put("message.casino.not_owner", "Nur der Besitzer (%s) kann diese Maschine abbauen.");
        DE.put("message.casino.busy_break", "Warte, bis die laufende Runde vorbei ist.");
        DE.put("message.casino.busy_bank", "W\u00e4hrend einer Runde kann die Bank nicht ge\u00e4ndert werden.");
        DE.put("message.casino.bank_only_owner", "Nur der Besitzer kann die Bank \u00f6ffnen.");
        DE.put("message.casino.max_stake", "Die Bank kann gerade h\u00f6chstens %s Diamanten Einsatz decken.");
        DE.put("message.casino.bank_empty", "Die Bank dieser Maschine ist leer \u2013 der Besitzer muss erst Diamanten einzahlen.");
        DE.put("message.casino.picked", "Gew\u00e4hlt: %s");
        DE.put("message.casino.delivered", "Du hast %s Diamanten aus dem Casino erhalten (w\u00e4hrend du offline warst).");
        DE.put("message.casino.delivered_items", "Du hast Casino-Maschinen zur\u00fcckbekommen, die w\u00e4hrend deiner Abwesenheit abgebaut wurden.");
        DE.put("message.casino.no_diamonds", "Du hast keine Diamanten dabei.");
        DE.put("message.casino.inventory_full", "Dein Inventar ist voll.");
        DE.put("message.casino.pack_declined", "Ohne das Casino-Ressourcenpaket werden die Maschinen nicht richtig angezeigt.");
    }
}

