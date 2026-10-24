/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.Bukkit
 *  org.bukkit.block.Block
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package de.nick.casino;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.Listeners;
import de.nick.casino.Msg;
import de.nick.casino.machine.Machine;
import de.nick.casino.machine.MachineType;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class CasinoCommand
implements CommandExecutor,
TabCompleter {
    private final CasinoPlugin plugin;

    public CasinoCommand(CasinoPlugin casinoPlugin) {
        this.plugin = casinoPlugin;
    }

    private static void say(CommandSender commandSender, String string) {
        commandSender.sendMessage(Component.text((String)"[Casino] ", (TextColor)Msg.GOLD).append((Component)Component.text((String)string, (TextColor)NamedTextColor.WHITE)));
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (stringArray.length == 0) {
            CasinoCommand.say(commandSender, "/casino give <Spieler> <roulette|slot_machine> [Anzahl] \u00b7 /casino info \u00b7 /casino pack \u00b7 /casino reload");
            return true;
        }
        switch (stringArray[0].toLowerCase()) {
            case "give": {
                if (!commandSender.hasPermission("casino.admin")) {
                    CasinoCommand.say(commandSender, "Daf\u00fcr fehlt dir die Berechtigung.");
                    return true;
                }
                if (stringArray.length < 3) {
                    CasinoCommand.say(commandSender, "/casino give <Spieler> <roulette|slot_machine> [Anzahl]");
                    return true;
                }
                Player player = Bukkit.getPlayerExact((String)stringArray[1]);
                MachineType machineType = MachineType.byId(stringArray[2]);
                if (player == null || machineType == null) {
                    CasinoCommand.say(commandSender, "Spieler oder Maschinentyp nicht gefunden.");
                    return true;
                }
                int n = 1;
                if (stringArray.length >= 4) {
                    try {
                        n = Math.max(1, Math.min(64, Integer.parseInt(stringArray[3])));
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                Listeners.giveItem(player, this.plugin.machineItem(machineType, n));
                CasinoCommand.say(commandSender, n + "\u00d7 " + machineType.germanName + " an " + player.getName() + " gegeben.");
                break;
            }
            case "info": {
                Machine machine;
                if (!(commandSender instanceof Player)) {
                    return true;
                }
                Player player = (Player)commandSender;
                Block block = player.getTargetBlockExact(6);
                Machine machine2 = machine = block == null ? null : this.plugin.machines().at(block);
                if (machine == null) {
                    CasinoCommand.say(commandSender, "Schau auf eine Casino-Maschine.");
                    return true;
                }
                CasinoCommand.say(commandSender, machine.type.germanName + " von " + machine.ownerName + " \u00b7 Bank: " + machine.bank + " Diamanten \u00b7 Max. Einsatz: " + machine.maxStake(machine.maxMultiplier()) + " (bei \u00d7" + machine.maxMultiplier() + ") \u00b7 Runden: " + machine.games);
                break;
            }
            case "pack": {
                if (!(commandSender instanceof Player)) break;
                Player player = (Player)commandSender;
                this.plugin.pack().send(player);
                CasinoCommand.say(commandSender, "Ressourcenpaket wird erneut gesendet.");
                break;
            }
            case "reload": {
                if (!commandSender.hasPermission("casino.admin")) {
                    return true;
                }
                this.plugin.reloadConfig();
                CasinoCommand.say(commandSender, "Config neu geladen (Port-\u00c4nderungen erst nach Neustart).");
                break;
            }
            case "rotation": {
                if (!commandSender.hasPermission("casino.admin")) {
                    return true;
                }
                if (stringArray.length < 2 || !stringArray[1].equalsIgnoreCase("xyz") && !stringArray[1].equalsIgnoreCase("zyx")) {
                    CasinoCommand.say(commandSender, "/casino rotation <xyz|zyx> \u2013 aktuell: " + this.plugin.getConfig().getString("debug.rotation-order", "xyz"));
                    return true;
                }
                this.plugin.getConfig().set("debug.rotation-order", (Object)stringArray[1].toLowerCase());
                this.plugin.saveConfig();
                this.plugin.machines().despawnAll();
                this.plugin.machines().spawnAllLoaded();
                CasinoCommand.say(commandSender, "Modell-Variante " + stringArray[1].toLowerCase() + " aktiv \u2013 sieht der Roulette-Kessel jetzt richtig aus?");
                break;
            }
            default: {
                CasinoCommand.say(commandSender, "Unbekannter Befehl.");
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] stringArray) {
        ArrayList<String> arrayList;
        block4: {
            block5: {
                block3: {
                    arrayList = new ArrayList<String>();
                    if (stringArray.length != 1) break block3;
                    for (String string2 : List.of((Object)"give", (Object)"info", (Object)"pack", (Object)"reload", (Object)"rotation")) {
                        if (!string2.startsWith(stringArray[0].toLowerCase())) continue;
                        arrayList.add(string2);
                    }
                    break block4;
                }
                if (stringArray.length != 2 || !stringArray[0].equalsIgnoreCase("give")) break block5;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getName().toLowerCase().startsWith(stringArray[1].toLowerCase())) continue;
                    arrayList.add(player.getName());
                }
                break block4;
            }
            if (stringArray.length != 3 || !stringArray[0].equalsIgnoreCase("give")) break block4;
            for (MachineType machineType : MachineType.values()) {
                if (!machineType.id.startsWith(stringArray[2].toLowerCase())) continue;
                arrayList.add(machineType.id);
            }
        }
        return arrayList;
    }
}

