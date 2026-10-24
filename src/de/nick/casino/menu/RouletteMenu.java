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
import de.nick.casino.machine.RouletteMachine;
import de.nick.casino.menu.BetMenu;
import de.nick.casino.util.GuiMeta;
import de.nick.casino.util.TitleBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public final class RouletteMenu
extends BetMenu {
    private static final TextColor[] HINT = new TextColor[]{TextColor.color((int)0xFF7070), TextColor.color((int)11187916), TextColor.color((int)7264412)};
    private final RouletteMachine roulette;

    public RouletteMenu(CasinoPlugin casinoPlugin, RouletteMachine rouletteMachine, Player player) {
        super(casinoPlugin, rouletteMachine, player, casinoPlugin.meta().intMatrix("roulette", "bets"), RouletteMenu.toSet(casinoPlugin.meta().intList("roulette", "button")));
        this.roulette = rouletteMachine;
    }

    static Set<Integer> toSet(int[] nArray) {
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (int n : nArray) {
            hashSet.add(n);
        }
        return hashSet;
    }

    @Override
    public void open() {
        if (!this.claim()) {
            return;
        }
        GuiMeta guiMeta = this.plugin.meta();
        TextComponent textComponent = this.bedrock ? Component.text((String)"Roulette \u2013 Setze auf eine Farbe") : new TitleBuilder(guiMeta).glyph("bg_roulette", -23).build();
        this.inv = Bukkit.createInventory((InventoryHolder)this, (int)54, (Component)textComponent);
        long l = this.roulette.maxStake(2);
        long l2 = this.roulette.maxStake(36);
        List list = List.of((Object)Component.text((String)"Lege Diamanten auf genau eine Farbe.", (TextColor)NamedTextColor.GRAY), (Object)Component.text((String)("Max. Einsatz Rot/Schwarz: " + l), (TextColor)NamedTextColor.GOLD), (Object)Component.text((String)("Max. Einsatz Gr\u00fcn: " + l2), (TextColor)NamedTextColor.GOLD));
        Object object = this.buttonSlots.iterator();
        while (object.hasNext()) {
            int n = (Integer)object.next();
            this.inv.setItem(n, this.button(Material.LIME_CONCRETE, (Component)Component.text((String)"DREHEN", (TextColor)NamedTextColor.WHITE), list));
        }
        if (this.bedrock) {
            object = new HashSet(this.buttonSlots);
            int[][] nArray = this.groups;
            int n = nArray.length;
            for (int i = 0; i < n; ++i) {
                int[] nArray2;
                for (int n2 : nArray2 = nArray[i]) {
                    object.add(n2);
                }
            }
            this.inv.setItem(1, RouletteMenu.named(Material.RED_WOOL, 1, (Component)Component.text((String)"ROT \u00d72", (TextColor)NamedTextColor.RED), null));
            this.inv.setItem(4, RouletteMenu.named(Material.BLACK_WOOL, 1, (Component)Component.text((String)"SCHWARZ \u00d72", (TextColor)NamedTextColor.DARK_GRAY), null));
            this.inv.setItem(7, RouletteMenu.named(Material.LIME_WOOL, 1, (Component)Component.text((String)"GR\u00dcN \u00d736", (TextColor)NamedTextColor.GREEN), null));
            object.add(1);
            object.add(4);
            object.add(7);
            for (int i = 0; i < 54; ++i) {
                if (object.contains(i)) continue;
                this.inv.setItem(i, RouletteMenu.filler());
            }
        }
        this.player.openInventory(this.inv);
        if (l2 <= 0L && l <= 0L) {
            Msg.actionbar(this.player, Msg.maxStake(0L));
        } else {
            Msg.actionbar(this.player, (Component)Component.text((String)("Max. Einsatz: " + l + " (Gr\u00fcn: " + l2 + ")"), (TextColor)NamedTextColor.GOLD));
        }
    }

    @Override
    protected void onSelectionChanged(int n) {
        if (n >= 0) {
            Msg.actionbar(this.player, (Component)Component.text((String)Msg.raw("message.casino.picked", Msg.colorName(n)), (TextColor)HINT[n]));
        } else if (n == -2) {
            Msg.actionbar(this.player, (Component)Component.text((String)Msg.raw("gui.roulettecasino.roulette.several", new Object[0]), (TextColor)TextColor.color((int)16739163)));
        }
    }

    @Override
    protected boolean play(int[] nArray) {
        Component component = this.roulette.start(this.player, nArray);
        if (component != null) {
            Msg.actionbar(this.player, component);
            return false;
        }
        return true;
    }
}

