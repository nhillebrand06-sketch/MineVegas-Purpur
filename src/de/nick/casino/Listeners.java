/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.event.packet.PlayerChunkLoadEvent
 *  org.bukkit.Bukkit
 *  org.bukkit.Chunk
 *  org.bukkit.GameMode
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.entity.Display
 *  org.bukkit.entity.ExperienceOrb
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockExplodeEvent
 *  org.bukkit.event.block.BlockPistonExtendEvent
 *  org.bukkit.event.block.BlockPistonRetractEvent
 *  org.bukkit.event.entity.EntityExplodeEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerResourcePackStatusEvent
 *  org.bukkit.event.player.PlayerResourcePackStatusEvent$Status
 *  org.bukkit.event.world.ChunkLoadEvent
 *  org.bukkit.event.world.ChunkUnloadEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package de.nick.casino;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.Msg;
import de.nick.casino.machine.Machine;
import de.nick.casino.machine.MachineType;
import de.nick.casino.machine.SlotMachine;
import de.nick.casino.menu.BankMenu;
import de.nick.casino.menu.CasinoMenu;
import de.nick.casino.util.Bedrock;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class Listeners
implements Listener {
    private final CasinoPlugin plugin;

    public Listeners(CasinoPlugin casinoPlugin) {
        this.plugin = casinoPlugin;
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        Machine machine;
        Block block = playerInteractEvent.getClickedBlock();
        Player player = playerInteractEvent.getPlayer();
        if (this.plugin.getConfig().getBoolean("debug.verbose", false)) {
            this.plugin.getLogger().info("interact " + player.getName() + " " + String.valueOf(playerInteractEvent.getAction()) + " " + String.valueOf(playerInteractEvent.getHand()) + " block=" + (block == null ? null : String.valueOf(block.getType()) + "@" + String.valueOf(block.getLocation().toVector())) + " item=" + String.valueOf(playerInteractEvent.getItem() == null ? null : playerInteractEvent.getItem().getType()) + " type=" + String.valueOf((Object)this.plugin.machineType(playerInteractEvent.getItem())) + " cancelled=" + String.valueOf(playerInteractEvent.useInteractedBlock()));
        }
        if (block != null && (machine = this.plugin.machines().at(block)) != null) {
            playerInteractEvent.setCancelled(true);
            if (Bedrock.isBedrock(player)) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.plugin.machines().sendBedrockBlocks(player, machine), 2L);
            }
            if (playerInteractEvent.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (player.isSneaking() && machine.isOwner(player)) {
                    new BankMenu(this.plugin, machine, player).open();
                } else {
                    machine.openGame(player);
                }
            } else if (playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK && player.isSneaking()) {
                this.tryBreak(player, machine);
            }
            return;
        }
        if (playerInteractEvent.getAction() != Action.RIGHT_CLICK_BLOCK || block == null) {
            return;
        }
        machine = playerInteractEvent.getItem();
        MachineType machineType = this.plugin.machineType((ItemStack)machine);
        if (machineType == null) {
            return;
        }
        playerInteractEvent.setCancelled(true);
        if (playerInteractEvent.getHand() != EquipmentSlot.HAND && playerInteractEvent.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        this.place(player, block, playerInteractEvent.getBlockFace(), machineType, (ItemStack)machine);
    }

    private static int facingIndex(BlockFace blockFace) {
        return switch (blockFace) {
            case BlockFace.SOUTH -> 0;
            case BlockFace.WEST -> 1;
            case BlockFace.NORTH -> 2;
            default -> 3;
        };
    }

    private void place(Player player, Block block, BlockFace blockFace, MachineType machineType, ItemStack itemStack) {
        Block block2 = block.isReplaceable() ? block : block.getRelative(blockFace);
        int n = Listeners.facingIndex(player.getFacing().getOppositeFace());
        Machine machine = Machine.create(this.plugin, UUID.randomUUID(), machineType, block2.getWorld().getName(), block2.getX(), block2.getY(), block2.getZ(), n);
        World world = block2.getWorld();
        for (int[] nArray : machine.footprintOffsets()) {
            Block block3 = world.getBlockAt(block2.getX() + nArray[0], block2.getY() + nArray[1], block2.getZ() + nArray[2]);
            if (block3.getY() >= world.getMaxHeight() || block3.getY() < world.getMinHeight() || !block3.isReplaceable() || this.plugin.machines().at(block3) != null || !world.getWorldBorder().isInside(block3.getLocation())) {
                Msg.actionbar(player, Msg.tr("message.casino.no_space", new Object[0]));
                return;
            }
            if (block3.getWorld().getNearbyEntities(block3.getBoundingBox()).stream().allMatch(entity -> entity instanceof Item || entity instanceof Display || entity instanceof ExperienceOrb)) continue;
            Msg.actionbar(player, Msg.tr("message.casino.no_space", new Object[0]));
            return;
        }
        if (machineType == MachineType.SLOT_MACHINE) {
            if (!block2.getRelative(BlockFace.DOWN).getType().isSolid()) {
                Msg.actionbar(player, Msg.tr("message.casino.need_floor", new Object[0]));
                return;
            }
            if (this.slotCrowded(block2, n)) {
                Msg.actionbar(player, Msg.tr("message.roulettecasino.slot_machine.spacing", new Object[0]));
                return;
            }
        }
        machine.owner = player.getUniqueId();
        machine.ownerName = player.getName();
        this.plugin.machines().add(machine);
        if (player.getGameMode() != GameMode.CREATIVE) {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1.0f, 1.0f);
        Msg.chat(player, "message.casino.placed", new Object[0]);
    }

    private boolean slotCrowded(Block block, int n) {
        BlockFace[] blockFaceArray;
        boolean bl = n % 2 == 0;
        for (BlockFace blockFace : blockFaceArray = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            boolean bl2 = blockFace == BlockFace.NORTH || blockFace == BlockFace.SOUTH;
            for (int i = -2; i <= 2; ++i) {
                boolean bl3;
                Block block2 = block.getRelative(blockFace).getRelative(0, i, 0);
                Machine machine = this.plugin.machines().at(block2);
                if (!(machine instanceof SlotMachine)) continue;
                boolean bl4 = bl3 = machine.facing % 2 == 0;
                if (bl2 == bl && bl2 == bl3) continue;
                return true;
            }
        }
        return false;
    }

    private void tryBreak(Player player, Machine machine) {
        Player player2;
        boolean bl = player.hasPermission("casino.admin");
        if (!machine.isOwner(player) && !bl) {
            Msg.actionbar(player, Msg.tr("message.casino.not_owner", machine.ownerName == null ? "?" : machine.ownerName));
            return;
        }
        if (machine.isBusy()) {
            Msg.actionbar(player, Msg.tr("message.casino.busy_break", new Object[0]));
            return;
        }
        long l = machine.bank;
        machine.bank = 0L;
        this.plugin.machines().remove(machine);
        UUID uUID = machine.owner;
        Player player3 = player2 = uUID == null ? null : Bukkit.getPlayer((UUID)uUID);
        if (player2 != null && player2.isOnline()) {
            Listeners.giveDiamonds(player2, l);
            Listeners.giveItem(player2, this.plugin.machineItem(machine.type, 1));
            if (player2.equals((Object)player)) {
                Msg.chat(player, "message.casino.removed", l);
            } else {
                Msg.chat(player, "message.casino.removed_admin", machine.ownerName, l);
                Msg.chat(player2, "message.casino.removed", l);
            }
        } else if (uUID != null) {
            this.plugin.machines().oweDiamonds(uUID, l);
            this.plugin.machines().oweItem(uUID, machine.type.id);
            Msg.chat(player, "message.casino.removed_admin", machine.ownerName, l);
        } else {
            Listeners.giveDiamonds(player, l);
            Listeners.giveItem(player, this.plugin.machineItem(machine.type, 1));
        }
        this.plugin.machines().save();
        player.playSound(machine.center(), Sound.BLOCK_WOOD_BREAK, 1.0f, 0.9f);
    }

    static void giveDiamonds(Player player, long l) {
        int n;
        for (long i = l; i > 0L; i -= (long)n) {
            n = (int)Math.min(64L, i);
            Listeners.giveItem(player, new ItemStack(Material.DIAMOND, n));
        }
    }

    static void giveItem(Player player, ItemStack itemStack) {
        for (ItemStack itemStack2 : player.getInventory().addItem(new ItemStack[]{itemStack}).values()) {
            player.getWorld().dropItem(player.getLocation(), itemStack2, item -> item.setOwner(player.getUniqueId()));
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onBreak(BlockBreakEvent blockBreakEvent) {
        Machine machine = this.plugin.machines().at(blockBreakEvent.getBlock());
        if (machine == null) {
            return;
        }
        blockBreakEvent.setCancelled(true);
        if (blockBreakEvent.getPlayer().isSneaking()) {
            this.tryBreak(blockBreakEvent.getPlayer(), machine);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onEntityExplode(EntityExplodeEvent entityExplodeEvent) {
        entityExplodeEvent.blockList().removeIf(block -> this.plugin.machines().at((Block)block) != null);
    }

    @EventHandler(ignoreCancelled=true)
    public void onBlockExplode(BlockExplodeEvent blockExplodeEvent) {
        blockExplodeEvent.blockList().removeIf(block -> this.plugin.machines().at((Block)block) != null);
    }

    @EventHandler(ignoreCancelled=true)
    public void onPistonExtend(BlockPistonExtendEvent blockPistonExtendEvent) {
        for (Block block : blockPistonExtendEvent.getBlocks()) {
            if (this.plugin.machines().at(block) == null) continue;
            blockPistonExtendEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPistonRetract(BlockPistonRetractEvent blockPistonRetractEvent) {
        for (Block block : blockPistonRetractEvent.getBlocks()) {
            if (this.plugin.machines().at(block) == null) continue;
            blockPistonRetractEvent.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        InventoryHolder inventoryHolder = inventoryClickEvent.getView().getTopInventory().getHolder(false);
        if (inventoryHolder instanceof CasinoMenu) {
            CasinoMenu casinoMenu = (CasinoMenu)inventoryHolder;
            casinoMenu.onClick(inventoryClickEvent);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        InventoryHolder inventoryHolder = inventoryDragEvent.getView().getTopInventory().getHolder(false);
        if (inventoryHolder instanceof CasinoMenu) {
            CasinoMenu casinoMenu = (CasinoMenu)inventoryHolder;
            casinoMenu.onDrag(inventoryDragEvent);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        InventoryHolder inventoryHolder = inventoryCloseEvent.getView().getTopInventory().getHolder(false);
        if (inventoryHolder instanceof CasinoMenu) {
            CasinoMenu casinoMenu = (CasinoMenu)inventoryHolder;
            casinoMenu.onClose(inventoryCloseEvent);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            this.plugin.pack().send(player);
            long l = this.plugin.machines().takeOwedDiamonds(player.getUniqueId());
            if (l > 0L) {
                Listeners.giveDiamonds(player, l);
                Msg.chat(player, "message.casino.delivered", l);
            }
            ArrayList<String> arrayList = new ArrayList<String>(this.plugin.machines().takeOwedItems(player.getUniqueId()));
            for (String string : arrayList) {
                MachineType machineType = MachineType.byId(string);
                if (machineType == null) continue;
                Listeners.giveItem(player, this.plugin.machineItem(machineType, 1));
            }
            if (!arrayList.isEmpty()) {
                Msg.chat(player, "message.casino.delivered_items", new Object[0]);
            }
            if (l > 0L || !arrayList.isEmpty()) {
                this.plugin.machines().save();
            }
        }, 20L);
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent playerResourcePackStatusEvent) {
        if (playerResourcePackStatusEvent.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            this.plugin.getLogger().info(playerResourcePackStatusEvent.getPlayer().getName() + " hat das Casino-Ressourcenpaket abgelehnt.");
        } else if (playerResourcePackStatusEvent.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD || playerResourcePackStatusEvent.getStatus() == PlayerResourcePackStatusEvent.Status.INVALID_URL || playerResourcePackStatusEvent.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_RELOAD) {
            this.plugin.getLogger().warning("Casino-Ressourcenpaket konnte bei " + playerResourcePackStatusEvent.getPlayer().getName() + " nicht geladen werden: " + String.valueOf(playerResourcePackStatusEvent.getStatus()) + " \u2013 stimmt pack.public-url in der config.yml und ist der Port weitergeleitet?");
        }
    }

    @EventHandler
    public void onChunkSent(PlayerChunkLoadEvent playerChunkLoadEvent) {
        Player player = playerChunkLoadEvent.getPlayer();
        if (!Bedrock.isBedrock(player)) {
            return;
        }
        Chunk chunk = playerChunkLoadEvent.getChunk();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                this.plugin.machines().sendBedrockBlocksInChunk(player, chunk);
            }
        }, 2L);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
        Chunk chunk = chunkLoadEvent.getChunk();
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (chunk.isLoaded()) {
                this.plugin.machines().chunkLoaded(chunk);
            }
        });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent chunkUnloadEvent) {
        this.plugin.machines().chunkUnloaded(chunkUnloadEvent.getChunk());
    }
}

