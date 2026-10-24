/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.datacomponent.DataComponentTypes
 *  io.papermc.paper.datacomponent.item.ItemLore
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package de.nick.casino;

import de.nick.casino.CasinoCommand;
import de.nick.casino.Listeners;
import de.nick.casino.PackServer;
import de.nick.casino.machine.Machine;
import de.nick.casino.machine.MachineManager;
import de.nick.casino.machine.MachineType;
import de.nick.casino.menu.CasinoMenu;
import de.nick.casino.util.Displays;
import de.nick.casino.util.GuiMeta;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.io.InputStream;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CasinoPlugin
extends JavaPlugin {
    private GuiMeta meta;
    private MachineManager machines;
    private PackServer pack;
    private NamespacedKey machineKey;

    public void onEnable() {
        Object object;
        this.saveDefaultConfig();
        Displays.init((Plugin)this);
        this.machineKey = new NamespacedKey((Plugin)this, "machine");
        try {
            object = this.getResource("gui-meta.json");
            try {
                this.meta = new GuiMeta((InputStream)object);
            }
            finally {
                if (object != null) {
                    ((InputStream)object).close();
                }
            }
        }
        catch (Exception exception) {
            this.getLogger().severe("gui-meta.json konnte nicht gelesen werden: " + String.valueOf(exception));
            Bukkit.getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        this.machines = new MachineManager(this);
        this.machines.load();
        this.pack = new PackServer(this);
        try {
            this.pack.start();
        }
        catch (Exception exception) {
            this.getLogger().severe("Der Webserver f\u00fcr das Ressourcenpaket konnte nicht starten: " + String.valueOf(exception));
        }
        Bukkit.getPluginManager().registerEvents((Listener)new Listeners(this), (Plugin)this);
        object = new CasinoCommand(this);
        PluginCommand pluginCommand = this.getCommand("casino");
        if (pluginCommand != null) {
            pluginCommand.setExecutor((CommandExecutor)object);
            pluginCommand.setTabCompleter((TabCompleter)object);
        }
        Bukkit.getScheduler().runTask((Plugin)this, () -> {
            this.machines.despawnAll();
            this.machines.spawnAllLoaded();
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.pack.send(player);
                for (Machine machine : this.machines.all()) {
                    this.machines.sendBedrockBlocks(player, machine);
                }
            }
        });
        Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> this.machines.tick(), 1L, 1L);
        Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> this.machines.saveIfDirty(), 200L, 200L);
        this.getLogger().info("MineVegas Casino (Plugin-Port) aktiv - " + this.machines.all().size() + " Maschinen geladen.");
    }

    public void onDisable() {
        if (this.machines != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!(player.getOpenInventory().getTopInventory().getHolder(false) instanceof CasinoMenu)) continue;
                player.closeInventory();
            }
            this.machines.shutdown();
        }
        if (this.pack != null) {
            this.pack.stop();
        }
    }

    public String rotSuffix() {
        return "zyx".equalsIgnoreCase(this.getConfig().getString("debug.rotation-order", "xyz")) ? "_zyx" : "";
    }

    public GuiMeta meta() {
        return this.meta;
    }

    public MachineManager machines() {
        return this.machines;
    }

    public PackServer pack() {
        return this.pack;
    }

    public ItemStack machineItem(MachineType machineType, int n) {
        ItemStack itemStack = new ItemStack(Material.PAPER, Math.max(1, Math.min(64, n)));
        itemStack.setData(DataComponentTypes.ITEM_MODEL, (Object)machineType.itemModel);
        itemStack.setData(DataComponentTypes.ITEM_NAME, (Object)Component.text((String)machineType.germanName, (TextColor)NamedTextColor.GOLD));
        itemStack.setData(DataComponentTypes.LORE, (Object)ItemLore.lore((List)List.of((Object)((TextComponent)Component.text((String)"Rechtsklick auf den Boden: aufstellen", (TextColor)NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)), (Object)((TextComponent)Component.text((String)"Du bist die Bank dieser Maschine.", (TextColor)NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)), (Object)((TextComponent)Component.text((String)"Schleichen + Rechtsklick: Bank \u00b7 Schleichen + Linksklick: abbauen", (TextColor)NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)))));
        itemStack.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.machineKey, PersistentDataType.STRING, (Object)machineType.id));
        return itemStack;
    }

    public MachineType machineType(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }
        String string = (String)itemStack.getPersistentDataContainer().get(this.machineKey, PersistentDataType.STRING);
        return string == null ? null : MachineType.byId(string);
    }
}

