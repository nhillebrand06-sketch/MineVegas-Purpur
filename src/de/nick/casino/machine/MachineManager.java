/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Chunk
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.plugin.Plugin
 */
package de.nick.casino.machine;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.machine.Machine;
import de.nick.casino.menu.CasinoMenu;
import de.nick.casino.util.Bedrock;
import de.nick.casino.util.Displays;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public final class MachineManager {
    private final CasinoPlugin plugin;
    private final Map<UUID, Machine> machines = new LinkedHashMap<UUID, Machine>();
    private final Map<String, Machine> byBlock = new HashMap<String, Machine>();
    private final Map<UUID, Long> owedDiamonds = new HashMap<UUID, Long>();
    private final Map<UUID, List<String>> owedItems = new HashMap<UUID, List<String>>();
    private final File file;
    private boolean dirty;
    private long tickCount;

    private void closeDistantMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            CasinoMenu casinoMenu;
            Object object = player.getOpenInventory().getTopInventory().getHolder(false);
            if (!(object instanceof CasinoMenu) || (casinoMenu = (CasinoMenu)object).machine() == null) continue;
            object = casinoMenu.machine();
            if (player.getWorld().getName().equals(((Machine)object).worldName) && !(player.getLocation().distanceSquared(((Machine)object).center()) > 100.0)) continue;
            player.closeInventory();
        }
    }

    public MachineManager(CasinoPlugin casinoPlugin) {
        this.plugin = casinoPlugin;
        this.file = new File(casinoPlugin.getDataFolder(), "machines.yml");
    }

    private static String key(String string, int n, int n2, int n3) {
        return string + ";" + n + ";" + n2 + ";" + n3;
    }

    public Collection<Machine> all() {
        return this.machines.values();
    }

    public Machine at(Block block) {
        return this.byBlock.get(MachineManager.key(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    public Machine at(String string, int n, int n2, int n3) {
        return this.byBlock.get(MachineManager.key(string, n, n2, n3));
    }

    public void markDirty() {
        this.dirty = true;
    }

    private void index(Machine machine) {
        for (int[] nArray : machine.footprintOffsets()) {
            this.byBlock.put(MachineManager.key(machine.worldName, machine.x + nArray[0], machine.y + nArray[1], machine.z + nArray[2]), machine);
        }
    }

    private void unindex(Machine machine) {
        for (int[] nArray : machine.footprintOffsets()) {
            this.byBlock.remove(MachineManager.key(machine.worldName, machine.x + nArray[0], machine.y + nArray[1], machine.z + nArray[2]));
        }
    }

    public void add(Machine machine) {
        this.machines.put(machine.id, machine);
        this.index(machine);
        for (Block block : machine.footprint()) {
            block.setType(Material.BARRIER, false);
        }
        machine.spawnVisuals();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.sendBedrockBlocksToAll(machine), 2L);
        this.markDirty();
        this.save();
    }

    public void remove(Machine machine) {
        machine.removed = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            CasinoMenu casinoMenu;
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder(false);
            if (!(inventoryHolder instanceof CasinoMenu) || (casinoMenu = (CasinoMenu)inventoryHolder).machine() != machine) continue;
            player.closeInventory();
        }
        machine.despawnVisuals();
        this.machines.remove(machine.id);
        this.unindex(machine);
        for (Block block : machine.footprint()) {
            if (block.getType() != Material.BARRIER) continue;
            block.setType(Material.AIR, false);
        }
        this.markDirty();
        this.save();
    }

    public void oweDiamonds(UUID uUID, long l) {
        if (l <= 0L || uUID == null) {
            return;
        }
        this.owedDiamonds.merge(uUID, l, Long::sum);
        this.markDirty();
    }

    public void oweItem(UUID uUID2, String string) {
        this.owedItems.computeIfAbsent(uUID2, uUID -> new ArrayList()).add(string);
        this.markDirty();
    }

    public long takeOwedDiamonds(UUID uUID) {
        Long l = this.owedDiamonds.remove(uUID);
        if (l != null) {
            this.markDirty();
        }
        return l == null ? 0L : l;
    }

    public List<String> takeOwedItems(UUID uUID) {
        List<String> list = this.owedItems.remove(uUID);
        if (list != null) {
            this.markDirty();
        }
        return list == null ? List.of() : list;
    }

    public void chunkLoaded(Chunk chunk) {
        String string = chunk.getWorld().getName();
        long l = Machine.chunkKey(chunk.getX(), chunk.getZ());
        for (Machine machine : this.machines.values()) {
            if (!machine.worldName.equals(string) || !machine.coveredChunks().contains(l)) continue;
            machine.spawnVisuals();
        }
    }

    public void chunkUnloaded(Chunk chunk) {
        String string = chunk.getWorld().getName();
        long l = Machine.chunkKey(chunk.getX(), chunk.getZ());
        for (Machine machine : this.machines.values()) {
            if (!machine.worldName.equals(string) || !machine.coveredChunks().contains(l)) continue;
            machine.despawnVisuals();
        }
        for (Entity entity : chunk.getEntities()) {
            if (!Displays.isVisual(entity)) continue;
            entity.remove();
        }
    }

    public void spawnAllLoaded() {
        for (Machine machine : this.machines.values()) {
            machine.spawnVisuals();
        }
    }

    public void despawnAll() {
        for (Machine machine : this.machines.values()) {
            machine.despawnVisuals();
        }
        for (Machine machine : Bukkit.getWorlds()) {
            for (Entity entity : machine.getEntities()) {
                if (!Displays.isVisual(entity)) continue;
                entity.remove();
            }
        }
    }

    public void tick() {
        ++this.tickCount;
        if (this.tickCount % 20L == 0L) {
            this.closeDistantMenus();
        }
        for (Machine machine : new ArrayList<Machine>(this.machines.values())) {
            World world = machine.world();
            if (world == null || !machine.isVisible() && (!machine.isBusy() || !world.isChunkLoaded(machine.x >> 4, machine.z >> 4))) continue;
            try {
                boolean bl = Machine.viewersNear(machine.center(), 40.0);
                machine.tick(world, world.getGameTime(), bl);
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().warning("Fehler beim Ticken von " + machine.type.id + " bei " + machine.x + "," + machine.y + "," + machine.z + ": " + String.valueOf(throwable));
            }
        }
    }

    public void sendBedrockBlocks(Player player, Machine machine) {
        if (!Bedrock.isBedrock(player) || !player.getWorld().getName().equals(machine.worldName)) {
            return;
        }
        List<Block> list = machine.footprint();
        for (int i = 0; i < list.size(); ++i) {
            Block block = list.get(i);
            player.sendBlockChange(block.getLocation(), machine.bedrockMaterial(i).createBlockData());
        }
    }

    public void sendBedrockBlocksToAll(Machine machine) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.sendBedrockBlocks(player, machine);
        }
    }

    public void sendBedrockBlocksInChunk(Player player, Chunk chunk) {
        if (!Bedrock.isBedrock(player)) {
            return;
        }
        String string = chunk.getWorld().getName();
        block0: for (Machine machine : this.machines.values()) {
            if (!machine.worldName.equals(string)) continue;
            for (int[] nArray : machine.footprintOffsets()) {
                if (machine.x + nArray[0] >> 4 != chunk.getX() || machine.z + nArray[2] >> 4 != chunk.getZ()) continue;
                this.sendBedrockBlocks(player, machine);
                continue block0;
            }
        }
    }

    public void load() {
        Object object;
        Object object2;
        Object object3;
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection("machines");
        if (configurationSection != null) {
            object3 = configurationSection.getKeys(false).iterator();
            while (object3.hasNext()) {
                object2 = (String)object3.next();
                try {
                    object = Machine.load(this.plugin, UUID.fromString((String)object2), configurationSection.getConfigurationSection((String)object2));
                    if (object == null) continue;
                    this.machines.put(((Machine)object).id, (Machine)object);
                    this.index((Machine)object);
                    String string = configurationSection.getConfigurationSection((String)object2);
                    long l = string.getLong("pendingPayout", 0L);
                    String string2 = string.getString("pendingPlayer");
                    if (l <= 0L || string2 == null) continue;
                    this.owedDiamonds.merge(UUID.fromString(string2), l, Long::sum);
                }
                catch (Exception exception) {
                    this.plugin.getLogger().warning("Maschine " + (String)object2 + " konnte nicht geladen werden: " + String.valueOf(exception));
                }
            }
        }
        if ((object3 = yamlConfiguration.getConfigurationSection("owedDiamonds")) != null) {
            object2 = object3.getKeys(false).iterator();
            while (object2.hasNext()) {
                object = (String)object2.next();
                this.owedDiamonds.merge(UUID.fromString((String)object), object3.getLong((String)object), Long::sum);
            }
        }
        if ((object2 = yamlConfiguration.getConfigurationSection("owedItems")) != null) {
            for (String string : object2.getKeys(false)) {
                this.owedItems.put(UUID.fromString(string), new ArrayList(object2.getStringList(string)));
            }
        }
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        ConfigurationSection configurationSection = yamlConfiguration.createSection("machines");
        for (Machine object2 : this.machines.values()) {
            object2.save(configurationSection.createSection(object2.id.toString()));
        }
        ConfigurationSection configurationSection2 = yamlConfiguration.createSection("owedDiamonds");
        for (Map.Entry<UUID, Long> entry : this.owedDiamonds.entrySet()) {
            configurationSection2.set(entry.getKey().toString(), (Object)entry.getValue());
        }
        ConfigurationSection configurationSection3 = yamlConfiguration.createSection("owedItems");
        for (Map.Entry<UUID, List<String>> entry : this.owedItems.entrySet()) {
            configurationSection3.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            this.plugin.getDataFolder().mkdirs();
            File file = new File(this.file.getParentFile(), "machines.yml.tmp");
            yamlConfiguration.save(file);
            if (!file.renameTo(this.file)) {
                this.file.delete();
                file.renameTo(this.file);
            }
            this.dirty = false;
        }
        catch (Exception exception) {
            this.plugin.getLogger().severe("machines.yml konnte nicht gespeichert werden: " + String.valueOf(exception));
        }
    }

    public void saveIfDirty() {
        if (this.dirty) {
            this.save();
        }
    }

    public void shutdown() {
        for (Machine machine : this.machines.values()) {
            long l = machine.takeUndelivered();
            if (l <= 0L) continue;
            this.oweDiamonds(machine.payoutPlayer(), l);
        }
        this.despawnAll();
        this.save();
    }
}

