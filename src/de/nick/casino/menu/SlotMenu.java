/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.InventoryHolder
 */
package de.nick.casino.menu;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.Msg;
import de.nick.casino.machine.SlotMachine;
import de.nick.casino.menu.BetMenu;
import de.nick.casino.menu.RouletteMenu;
import de.nick.casino.util.GuiMeta;
import de.nick.casino.util.TitleBuilder;
import java.util.HashSet;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public final class SlotMenu
extends BetMenu {
    private final SlotMachine slot;

    public SlotMenu(CasinoPlugin casinoPlugin, SlotMachine slotMachine, Player player) {
        super(casinoPlugin, slotMachine, player, new int[][]{{casinoPlugin.meta().intValue("slot", "bet")}}, RouletteMenu.toSet(casinoPlugin.meta().intList("slot", "button")));
        this.slot = slotMachine;
    }

    @Override
    public void open() {
        Object object;
        TextComponent textComponent;
        if (!this.claim()) {
            return;
        }
        GuiMeta guiMeta = this.plugin.meta();
        if (this.bedrock) {
            textComponent = Component.text((String)"Spielautomat");
        } else {
            TitleBuilder titleBuilder = new TitleBuilder(guiMeta).glyph("bg_slot", -23);
            int[] nArray = guiMeta.intList("slot", "reelX");
            object = this.slot.stops();
            for (int i = 0; i < 3; ++i) {
                titleBuilder.glyph("reel_" + i + "_" + object[i], nArray[i]);
            }
            textComponent = titleBuilder.build();
        }
        this.inv = Bukkit.createInventory((InventoryHolder)this, (int)54, (Component)textComponent);
        long l = this.slot.maxStake(100);
        object = List.of((Object)Component.text((String)"Lege bis zu 64 Diamanten in das Einsatzfeld.", (TextColor)NamedTextColor.GRAY), (Object)Component.text((String)("Max. Einsatz: " + l), (TextColor)NamedTextColor.GOLD), (Object)Component.text((String)"777 zahlt \u00d7100", (TextColor)NamedTextColor.YELLOW));
        Object object2 = this.buttonSlots.iterator();
        while (object2.hasNext()) {
            int n = (Integer)object2.next();
            this.inv.setItem(n, this.button(Material.LIME_CONCRETE, (Component)Component.text((String)"DREHEN", (TextColor)NamedTextColor.WHITE), (List<Component>)object));
        }
        if (this.bedrock) {
            object2 = new HashSet(this.buttonSlots);
            int[][] nArray = this.groups;
            int n = nArray.length;
            for (int i = 0; i < n; ++i) {
                int[] nArray2;
                for (int n2 : nArray2 = nArray[i]) {
                    object2.add(n2);
                }
            }
            this.inv.setItem(13, SlotMenu.named(Material.GOLD_BLOCK, 1, (Component)Component.text((String)"EINSATZ \u2193", (TextColor)NamedTextColor.GOLD), List.of((Object)Component.text((String)"777 \u00d7100 \u00b7 3 Diamanten \u00d730 \u00b7 3 Balken \u00d715", (TextColor)NamedTextColor.GRAY), (Object)Component.text((String)"3 Glocken \u00d710 \u00b7 3 Zitronen \u00d76 \u00b7 3 Kirschen \u00d74", (TextColor)NamedTextColor.GRAY), (Object)Component.text((String)"2 Kirschen \u00d73 \u00b7 1 Kirsche \u00d72 (von links)", (TextColor)NamedTextColor.GRAY))));
            object2.add(13);
            for (int i = 0; i < 54; ++i) {
                if (object2.contains(i)) continue;
                this.inv.setItem(i, SlotMenu.filler());
            }
        }
        this.player.openInventory(this.inv);
        Msg.actionbar(this.player, (Component)(l <= 0L ? Msg.maxStake(0L) : Component.text((String)("Max. Einsatz: " + l), (TextColor)NamedTextColor.GOLD)));
    }

    @Override
    protected boolean play(int[] nArray) {
        Component component = this.slot.start(this.player, nArray[0]);
        if (component != null) {
            Msg.actionbar(this.player, component);
            return false;
        }
        return true;
    }
}

