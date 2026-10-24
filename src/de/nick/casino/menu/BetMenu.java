/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.InventoryAction
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package de.nick.casino.menu;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.Msg;
import de.nick.casino.machine.Machine;
import de.nick.casino.menu.CasinoMenu;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class BetMenu
extends CasinoMenu {
    protected final Machine machine;
    protected final int[][] groups;
    protected final Set<Integer> buttonSlots;
    private boolean consumed;
    private boolean closed;

    protected BetMenu(CasinoPlugin casinoPlugin, Machine machine, Player player, int[][] nArray, Set<Integer> set) {
        super(casinoPlugin, player);
        this.machine = machine;
        this.groups = nArray;
        this.buttonSlots = set;
    }

    protected abstract boolean play(int[] var1);

    @Override
    public Machine machine() {
        return this.machine;
    }

    protected void onSelectionChanged(int n) {
    }

    protected int groupOf(int n) {
        for (int i = 0; i < this.groups.length; ++i) {
            for (int n2 : this.groups[i]) {
                if (n2 != n) continue;
                return i;
            }
        }
        return -1;
    }

    protected int stakedGroup() {
        int n = -1;
        for (int i = 0; i < this.groups.length; ++i) {
            for (int n2 : this.groups[i]) {
                ItemStack itemStack = this.inv.getItem(n2);
                if (itemStack == null || itemStack.isEmpty()) continue;
                if (n == -1) {
                    n = i;
                    continue;
                }
                if (n == i) continue;
                return -2;
            }
        }
        return n;
    }

    protected int[] stakes() {
        int[] nArray = new int[this.groups.length];
        for (int i = 0; i < this.groups.length; ++i) {
            for (int n : this.groups[i]) {
                ItemStack itemStack = this.inv.getItem(n);
                if (!BetMenu.isPlainDiamond(itemStack)) continue;
                int n2 = i;
                nArray[n2] = nArray[n2] + itemStack.getAmount();
            }
        }
        return nArray;
    }

    private boolean canPlaceIn(int n) {
        int n2 = this.groupOf(n);
        int n3 = this.stakedGroup();
        return n2 >= 0 && (n3 == -1 || n3 == n2);
    }

    private void afterChange() {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (this.closed) {
                return;
            }
            int n = this.stakedGroup();
            this.onSelectionChanged(n);
        });
    }

    @Override
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        int n = inventoryClickEvent.getRawSlot();
        int n2 = this.inv.getSize();
        InventoryAction inventoryAction = inventoryClickEvent.getAction();
        if (n < 0) {
            return;
        }
        if (n < n2) {
            if (this.buttonSlots.contains(n)) {
                inventoryClickEvent.setCancelled(true);
                if (inventoryClickEvent.getCursor() == null || inventoryClickEvent.getCursor().isEmpty()) {
                    Bukkit.getScheduler().runTask((Plugin)this.plugin, this::pressPlay);
                }
                return;
            }
            if (this.groupOf(n) < 0) {
                inventoryClickEvent.setCancelled(true);
                return;
            }
            switch (inventoryAction) {
                case PICKUP_ALL: 
                case PICKUP_HALF: 
                case PICKUP_SOME: 
                case PICKUP_ONE: 
                case MOVE_TO_OTHER_INVENTORY: 
                case NOTHING: {
                    this.afterChange();
                    break;
                }
                case PLACE_ALL: 
                case PLACE_ONE: 
                case PLACE_SOME: 
                case SWAP_WITH_CURSOR: {
                    ItemStack itemStack = inventoryClickEvent.getCursor();
                    if (!BetMenu.isPlainDiamond(itemStack)) {
                        inventoryClickEvent.setCancelled(true);
                        Msg.actionbar(this.player, Msg.tr("message.casino.only_diamonds", new Object[0]));
                        return;
                    }
                    if (!this.canPlaceIn(n)) {
                        inventoryClickEvent.setCancelled(true);
                        Msg.actionbar(this.player, Msg.tr("gui.roulettecasino.roulette.several", new Object[0]));
                        return;
                    }
                    this.afterChange();
                    break;
                }
                default: {
                    inventoryClickEvent.setCancelled(true);
                }
            }
            return;
        }
        if (inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            inventoryClickEvent.setCancelled(true);
            ItemStack itemStack = inventoryClickEvent.getCurrentItem();
            if (!BetMenu.isPlainDiamond(itemStack)) {
                Msg.actionbar(this.player, Msg.tr("message.casino.only_diamonds", new Object[0]));
                return;
            }
            int n3 = this.stakedGroup();
            if (n3 == -2) {
                return;
            }
            if (n3 == -1) {
                n3 = 0;
            }
            int n4 = itemStack.getAmount();
            for (int n5 : this.groups[n3]) {
                int n6;
                if (n4 <= 0) break;
                ItemStack itemStack2 = this.inv.getItem(n5);
                if (itemStack2 == null || itemStack2.isEmpty()) {
                    n6 = Math.min(64, n4);
                    this.inv.setItem(n5, new ItemStack(Material.DIAMOND, n6));
                    n4 -= n6;
                    continue;
                }
                if (!BetMenu.isPlainDiamond(itemStack2) || (n6 = Math.min(64 - itemStack2.getAmount(), n4)) <= 0) continue;
                itemStack2.setAmount(itemStack2.getAmount() + n6);
                n4 -= n6;
            }
            if (n4 <= 0) {
                inventoryClickEvent.setCurrentItem(null);
            } else {
                itemStack.setAmount(n4);
            }
            this.afterChange();
        } else if (inventoryAction == InventoryAction.COLLECT_TO_CURSOR) {
            inventoryClickEvent.setCancelled(true);
        }
    }

    @Override
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        Map.Entry entry2;
        int n = this.inv.getSize();
        HashMap<Integer, ItemStack> hashMap = new HashMap<Integer, ItemStack>();
        for (Map.Entry entry2 : inventoryDragEvent.getNewItems().entrySet()) {
            if ((Integer)entry2.getKey() >= n) continue;
            hashMap.put((Integer)entry2.getKey(), (ItemStack)entry2.getValue());
        }
        if (hashMap.isEmpty()) {
            return;
        }
        if (!BetMenu.isPlainDiamond(inventoryDragEvent.getOldCursor())) {
            inventoryDragEvent.setCancelled(true);
            return;
        }
        int n2 = -1;
        entry2 = hashMap.keySet().iterator();
        while (entry2.hasNext()) {
            int n3 = (Integer)entry2.next();
            int n4 = this.groupOf(n3);
            if (n4 < 0 || n2 != -1 && n4 != n2) {
                inventoryDragEvent.setCancelled(true);
                return;
            }
            n2 = n4;
        }
        int n5 = this.stakedGroup();
        if (n5 == -2 || n5 >= 0 && n5 != n2) {
            inventoryDragEvent.setCancelled(true);
            Msg.actionbar(this.player, Msg.tr("gui.roulettecasino.roulette.several", new Object[0]));
            return;
        }
        this.afterChange();
    }

    private void pressPlay() {
        if (this.closed || this.player.getOpenInventory().getTopInventory().getHolder(false) != this) {
            return;
        }
        int[] nArray = this.stakes();
        if (this.play(nArray)) {
            this.consumed = true;
            int[][] nArray2 = this.groups;
            int n = nArray2.length;
            for (int i = 0; i < n; ++i) {
                int[] nArray3;
                for (int n2 : nArray3 = nArray2[i]) {
                    this.inv.setItem(n2, null);
                }
            }
            this.player.closeInventory();
        }
    }

    @Override
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        this.closed = true;
        if (this.machine.currentUser != null && this.machine.currentUser.equals(this.player.getUniqueId())) {
            this.machine.currentUser = null;
        }
        if (this.consumed) {
            return;
        }
        int[][] nArray = this.groups;
        int n = nArray.length;
        for (int i = 0; i < n; ++i) {
            int[] nArray2;
            for (int n2 : nArray2 = nArray[i]) {
                ItemStack itemStack = this.inv.getItem(n2);
                if (itemStack == null || itemStack.isEmpty()) continue;
                this.inv.setItem(n2, null);
                for (ItemStack itemStack2 : this.player.getInventory().addItem(new ItemStack[]{itemStack}).values()) {
                    this.player.getWorld().dropItem(this.player.getLocation(), itemStack2, item -> item.setOwner(this.player.getUniqueId()));
                }
            }
        }
    }

    protected boolean claim() {
        InventoryHolder inventoryHolder;
        Player player;
        if (this.machine.currentUser != null && !this.machine.currentUser.equals(this.player.getUniqueId()) && (player = Bukkit.getPlayer((UUID)this.machine.currentUser)) != null && player.isOnline() && (inventoryHolder = player.getOpenInventory().getTopInventory().getHolder(false)) instanceof BetMenu) {
            BetMenu betMenu = (BetMenu)inventoryHolder;
            if (betMenu.machine == this.machine) {
                Msg.actionbar(this.player, Msg.tr("message.casino.in_use", player.getName()));
                return false;
            }
        }
        this.machine.currentUser = this.player.getUniqueId();
        return true;
    }
}

