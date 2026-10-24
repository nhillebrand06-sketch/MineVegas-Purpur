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
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package de.nick.casino.menu;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.Msg;
import de.nick.casino.machine.Machine;
import de.nick.casino.menu.CasinoMenu;
import de.nick.casino.util.GuiMeta;
import de.nick.casino.util.TitleBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class BankMenu
extends CasinoMenu {
    private static final int[] AMOUNTS = new int[]{1, 16, 64, Integer.MAX_VALUE};
    private final Machine machine;
    private final int[] depositSlots;
    private final int[] withdrawSlots;

    public BankMenu(CasinoPlugin casinoPlugin, Machine machine, Player player) {
        super(casinoPlugin, player);
        this.machine = machine;
        this.depositSlots = casinoPlugin.meta().intList("bank", "deposit");
        this.withdrawSlots = casinoPlugin.meta().intList("bank", "withdraw");
    }

    @Override
    public Machine machine() {
        return this.machine;
    }

    private static String fmt(long l) {
        return String.format(Locale.GERMANY, "%,d", l);
    }

    private List<String[]> lines() {
        long l = this.machine.wagered - this.machine.paidOut;
        ArrayList<String[]> arrayList = new ArrayList<String[]>();
        arrayList.add(new String[]{"Maschine: " + this.machine.type.germanName, "gold"});
        arrayList.add(new String[]{"Guthaben: " + BankMenu.fmt(this.machine.bank) + " Diamanten", "white"});
        arrayList.add(new String[]{"Max. Einsatz: " + BankMenu.fmt(this.machine.maxStake(this.machine.maxMultiplier())) + " (bei \u00d7" + this.machine.maxMultiplier() + ")", "aqua"});
        arrayList.add(new String[]{"Eingesetzt: " + BankMenu.fmt(this.machine.wagered) + "  Ausgezahlt: " + BankMenu.fmt(this.machine.paidOut), "gray"});
        arrayList.add(new String[]{"Runden: " + BankMenu.fmt(this.machine.games), "gray"});
        arrayList.add(new String[]{"Gewinn der Bank: " + (l >= 0L ? "+" : "") + BankMenu.fmt(l), l >= 0L ? "green" : "red"});
        return arrayList;
    }

    private static TextColor color(String string) {
        return switch (string) {
            case "gold" -> TextColor.color((int)0xFFAA00);
            case "aqua" -> TextColor.color((int)0x55FFFF);
            case "gray" -> TextColor.color((int)0xC8C8C8);
            case "green" -> TextColor.color((int)0x55FF55);
            case "red" -> TextColor.color((int)0xFF5555);
            default -> NamedTextColor.WHITE;
        };
    }

    @Override
    public void open() {
        int n;
        Object object;
        TextComponent textComponent;
        GuiMeta guiMeta = this.plugin.meta();
        List<String[]> list = this.lines();
        if (this.bedrock) {
            textComponent = Component.text((String)("Bank: " + BankMenu.fmt(this.machine.bank) + " Diamanten"));
        } else {
            TitleBuilder titleBuilder = new TitleBuilder(guiMeta).glyph("bg_bank", -23);
            object = guiMeta.intList("bank", "lineY");
            int n2 = guiMeta.intValue("bank", "lineX");
            for (n = 0; n < list.size() && n < ((int[])object).length; ++n) {
                titleBuilder.text(list.get(n)[0], n2, (int)object[n], BankMenu.color(list.get(n)[1]), false);
            }
            textComponent = titleBuilder.build();
        }
        this.inv = Bukkit.createInventory((InventoryHolder)this, (int)54, (Component)textComponent);
        for (int i = 0; i < 4; ++i) {
            object = AMOUNTS[i] == Integer.MAX_VALUE ? "Alle" : String.valueOf(AMOUNTS[i]);
            Material material = AMOUNTS[i] == Integer.MAX_VALUE ? Material.DIAMOND_BLOCK : Material.DIAMOND;
            n = AMOUNTS[i] == Integer.MAX_VALUE ? 1 : AMOUNTS[i];
            this.inv.setItem(this.depositSlots[i], BankMenu.named(material, n, (Component)Component.text((String)((String)object + " einzahlen"), (TextColor)NamedTextColor.GREEN), List.of((Object)Component.text((String)"Aus deinem Inventar in die Bank", (TextColor)NamedTextColor.GRAY))));
            this.inv.setItem(this.withdrawSlots[i], BankMenu.named(material, n, (Component)Component.text((String)((String)object + " abheben"), (TextColor)NamedTextColor.RED), List.of((Object)Component.text((String)"Aus der Bank in dein Inventar", (TextColor)NamedTextColor.GRAY))));
        }
        if (this.bedrock) {
            ArrayList<Component> arrayList = new ArrayList<Component>();
            object = list.iterator();
            while (object.hasNext()) {
                String[] stringArray = (String[])object.next();
                arrayList.add((Component)Component.text((String)stringArray[0], (TextColor)BankMenu.color(stringArray[1])));
            }
            this.inv.setItem(4, BankMenu.named(Material.PAPER, 1, (Component)Component.text((String)"Casino-Bank", (TextColor)NamedTextColor.GOLD), arrayList));
            for (int i = 0; i < 54; ++i) {
                if (this.inv.getItem(i) != null) continue;
                this.inv.setItem(i, BankMenu.filler());
            }
        }
        this.player.openInventory(this.inv);
    }

    @Override
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
        int n = inventoryClickEvent.getRawSlot();
        if (n < 0 || n >= this.inv.getSize()) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            int n2 = AMOUNTS[i];
            if (n == this.depositSlots[i]) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.deposit(n2));
                return;
            }
            if (n != this.withdrawSlots[i]) continue;
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.withdraw(n2));
            return;
        }
    }

    private void deposit(int n) {
        if (this.machine.removed) {
            this.player.closeInventory();
            return;
        }
        if (this.machine.isBusy()) {
            Msg.actionbar(this.player, Msg.tr("message.casino.busy_bank", new Object[0]));
            return;
        }
        int n2 = 0;
        for (ItemStack itemStack : this.player.getInventory().getStorageContents()) {
            if (!BankMenu.isPlainDiamond(itemStack)) continue;
            n2 += itemStack.getAmount();
        }
        int n3 = Math.min(n2, n);
        if (n3 <= 0) {
            Msg.actionbar(this.player, Msg.tr("message.casino.no_diamonds", new Object[0]));
            return;
        }
        int n4 = n3;
        ItemStack[] itemStackArray = this.player.getInventory().getStorageContents();
        for (int i = 0; i < itemStackArray.length && n4 > 0; ++i) {
            ItemStack itemStack = itemStackArray[i];
            if (!BankMenu.isPlainDiamond(itemStack)) continue;
            int n5 = Math.min(n4, itemStack.getAmount());
            itemStack.setAmount(itemStack.getAmount() - n5);
            n4 -= n5;
            itemStackArray[i] = itemStack.getAmount() <= 0 ? null : itemStack;
        }
        this.player.getInventory().setStorageContents(itemStackArray);
        this.machine.bank += (long)n3;
        this.plugin.machines().markDirty();
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
        this.refresh();
    }

    private void withdraw(int n) {
        if (this.machine.removed) {
            this.player.closeInventory();
            return;
        }
        if (this.machine.isBusy()) {
            Msg.actionbar(this.player, Msg.tr("message.casino.busy_bank", new Object[0]));
            return;
        }
        long l = 0L;
        for (ItemStack itemStack : this.player.getInventory().getStorageContents()) {
            if (itemStack == null || itemStack.isEmpty()) {
                l += 64L;
                continue;
            }
            if (!BankMenu.isPlainDiamond(itemStack)) continue;
            l += (long)(64 - itemStack.getAmount());
        }
        long l2 = Math.min(Math.min(this.machine.bank, (long)n), l);
        if (l2 <= 0L && this.machine.bank > 0L) {
            Msg.actionbar(this.player, Msg.tr("message.casino.inventory_full", new Object[0]));
            return;
        }
        if (l2 <= 0L) {
            return;
        }
        this.machine.bank -= l2;
        this.plugin.machines().markDirty();
        long l3 = l2;
        while (l3 > 0L) {
            int n2 = (int)Math.min(64L, l3);
            l3 -= (long)n2;
            for (ItemStack itemStack : this.player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.DIAMOND, n2)}).values()) {
                this.player.getWorld().dropItem(this.player.getLocation(), itemStack, item -> item.setOwner(this.player.getUniqueId()));
            }
        }
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.2f);
        this.refresh();
    }

    private void refresh() {
        if (this.player.getOpenInventory().getTopInventory().getHolder(false) == this) {
            new BankMenu(this.plugin, this.machine, this.player).open();
        }
    }
}

