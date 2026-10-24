/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.datacomponent.DataComponentTypes
 *  io.papermc.paper.datacomponent.item.ItemLore
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.text.format.TextDecoration$State
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 */
package de.nick.casino.menu;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.machine.Machine;
import de.nick.casino.util.Bedrock;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class CasinoMenu
implements InventoryHolder {
    protected final CasinoPlugin plugin;
    protected final Player player;
    protected final boolean bedrock;
    protected Inventory inv;

    protected CasinoMenu(CasinoPlugin casinoPlugin, Player player) {
        this.plugin = casinoPlugin;
        this.player = player;
        this.bedrock = Bedrock.isBedrock(player);
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public abstract void open();

    public Machine machine() {
        return null;
    }

    public static boolean isPlainDiamond(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() == Material.DIAMOND && itemStack.isSimilar(new ItemStack(Material.DIAMOND));
    }

    public void onClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
    }

    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        inventoryDragEvent.setCancelled(true);
    }

    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
    }

    protected ItemStack button(Material material, Component component2, List<Component> list) {
        ItemStack itemStack;
        if (this.bedrock) {
            itemStack = new ItemStack(material);
        } else {
            itemStack = new ItemStack(Material.PAPER);
            itemStack.setData(DataComponentTypes.ITEM_MODEL, (Object)Key.key((String)"casino", (String)"empty"));
        }
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, (Object)component2.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        if (list != null && !list.isEmpty()) {
            itemStack.setData(DataComponentTypes.LORE, (Object)ItemLore.lore((List)list.stream().map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList()));
        }
        return itemStack;
    }

    protected static ItemStack named(Material material, int n, Component component2, List<Component> list) {
        ItemStack itemStack = new ItemStack(material, Math.max(1, Math.min(material.getMaxStackSize(), n)));
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, (Object)component2.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        if (list != null && !list.isEmpty()) {
            itemStack.setData(DataComponentTypes.LORE, (Object)ItemLore.lore((List)list.stream().map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList()));
        }
        return itemStack;
    }

    protected static ItemStack filler() {
        ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, (Object)Component.text((String)" "));
        return itemStack;
    }

    protected static Component white(String string) {
        return Component.text((String)string, (TextColor)NamedTextColor.WHITE);
    }

    protected static Component gray(String string) {
        return Component.text((String)string, (TextColor)NamedTextColor.GRAY);
    }
}

