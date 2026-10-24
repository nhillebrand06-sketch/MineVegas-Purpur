/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.util.Vector
 *  org.joml.Quaternionf
 */
package de.nick.casino.machine;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.machine.MachineType;
import de.nick.casino.machine.RouletteMachine;
import de.nick.casino.machine.SlotMachine;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

public abstract class Machine {
    protected final CasinoPlugin plugin;
    public final UUID id;
    public final MachineType type;
    public final String worldName;
    public final int x;
    public final int y;
    public final int z;
    public final int facing;
    public UUID owner;
    public String ownerName;
    public long bank;
    public long wagered;
    public long paidOut;
    public long games;
    protected final List<Entity> visuals = new ArrayList<Entity>();
    protected boolean visible;
    public UUID currentUser;
    public boolean removed;
    protected final Deque<Integer> ejectQueue = new ArrayDeque<Integer>();
    protected UUID payoutPlayer;

    protected Machine(CasinoPlugin casinoPlugin, UUID uUID, MachineType machineType, String string, int n, int n2, int n3, int n4) {
        this.plugin = casinoPlugin;
        this.id = uUID;
        this.type = machineType;
        this.worldName = string;
        this.x = n;
        this.y = n2;
        this.z = n3;
        this.facing = n4;
    }

    public abstract int[][] footprintOffsets();

    public abstract Material bedrockMaterial(int var1);

    protected abstract void spawnVisualsImpl(World var1);

    public abstract void tick(World var1, long var2, boolean var4);

    public abstract boolean isBusy();

    public abstract int maxMultiplier();

    public abstract void openGame(Player var1);

    protected void saveState(ConfigurationSection configurationSection) {
    }

    protected void loadState(ConfigurationSection configurationSection) {
    }

    public World world() {
        return Bukkit.getWorld((String)this.worldName);
    }

    public List<Block> footprint() {
        World world = this.world();
        ArrayList<Block> arrayList = new ArrayList<Block>();
        if (world == null) {
            return arrayList;
        }
        for (int[] nArray : this.footprintOffsets()) {
            arrayList.add(world.getBlockAt(this.x + nArray[0], this.y + nArray[1], this.z + nArray[2]));
        }
        return arrayList;
    }

    public boolean occupies(String string, int n, int n2, int n3) {
        if (!this.worldName.equals(string)) {
            return false;
        }
        for (int[] nArray : this.footprintOffsets()) {
            if (this.x + nArray[0] != n || this.y + nArray[1] != n2 || this.z + nArray[2] != n3) continue;
            return true;
        }
        return false;
    }

    public Location local(double d, double d2, double d3) {
        double d4 = d;
        double d5 = d3;
        for (int i = 0; i < Math.floorMod(this.facing, 4); ++i) {
            double d6 = 16.0 - d5;
            d5 = d4;
            d4 = d6;
        }
        return new Location(this.world(), (double)this.x + d4 / 16.0, (double)this.y + d2 / 16.0, (double)this.z + d5 / 16.0);
    }

    public Quaternionf facingRotation() {
        return new Quaternionf().rotationY((float)((double)(-this.facing) * Math.PI / 2.0));
    }

    public int[] frontStep() {
        int[] nArray;
        switch (Math.floorMod(this.facing, 4)) {
            case 0: {
                int[] nArray2 = new int[2];
                nArray2[0] = 0;
                nArray = nArray2;
                nArray2[1] = 1;
                break;
            }
            case 1: {
                int[] nArray3 = new int[2];
                nArray3[0] = -1;
                nArray = nArray3;
                nArray3[1] = 0;
                break;
            }
            case 2: {
                int[] nArray4 = new int[2];
                nArray4[0] = 0;
                nArray = nArray4;
                nArray4[1] = -1;
                break;
            }
            default: {
                int[] nArray5 = new int[2];
                nArray5[0] = 1;
                nArray = nArray5;
                nArray5[1] = 0;
            }
        }
        return nArray;
    }

    public Location center() {
        return new Location(this.world(), (double)this.x + 0.5, (double)this.y + 0.5, (double)this.z + 0.5);
    }

    public boolean isOwner(Player player) {
        return this.owner != null && this.owner.equals(player.getUniqueId());
    }

    public boolean isVisible() {
        return this.visible;
    }

    public Set<Long> coveredChunks() {
        HashSet<Long> hashSet = new HashSet<Long>();
        for (int[] nArray : this.footprintOffsets()) {
            hashSet.add(Machine.chunkKey(this.x + nArray[0] >> 4, this.z + nArray[2] >> 4));
        }
        Object object = this.extraVisualPoints().iterator();
        while (object.hasNext()) {
            double[] dArray = (double[])object.next();
            hashSet.add(Machine.chunkKey((int)Math.floor(dArray[0]) >> 4, (int)Math.floor(dArray[1]) >> 4));
        }
        return hashSet;
    }

    protected List<double[]> extraVisualPoints() {
        return List.of();
    }

    public static long chunkKey(int n, int n2) {
        return (long)n << 32 ^ (long)n2 & 0xFFFFFFFFL;
    }

    public void spawnVisuals() {
        World world = this.world();
        if (world == null || this.visible || this.removed) {
            return;
        }
        for (long l : this.coveredChunks()) {
            if (world.isChunkLoaded((int)(l >> 32), (int)l)) continue;
            return;
        }
        this.visible = true;
        this.spawnVisualsImpl(world);
    }

    public void despawnVisuals() {
        for (Entity entity : this.visuals) {
            if (entity == null || !entity.isValid()) continue;
            entity.remove();
        }
        this.visuals.clear();
        this.visible = false;
    }

    protected <T extends Entity> T track(T t) {
        this.visuals.add(t);
        return t;
    }

    public long maxStake(int n) {
        if (n <= 1) {
            return Long.MAX_VALUE;
        }
        return this.bank / (long)(n - 1);
    }

    protected void bookRound(Player player, int n, int n2) {
        int n3;
        this.bank += (long)n;
        this.bank -= (long)n2;
        this.wagered += (long)n;
        this.paidOut += (long)n2;
        ++this.games;
        this.payoutPlayer = player.getUniqueId();
        for (int i = n2; i > 0; i -= n3) {
            n3 = Math.min(64, i);
            this.ejectQueue.add(n3);
        }
        this.plugin.machines().markDirty();
    }

    protected void ejectNext(Location location, Vector vector) {
        Integer n = this.ejectQueue.pollFirst();
        if (n == null || location.getWorld() == null) {
            return;
        }
        Item item2 = location.getWorld().dropItem(location, new ItemStack(Material.DIAMOND, n.intValue()), item -> {
            item.setVelocity(vector);
            item.setPickupDelay(8);
            if (this.payoutPlayer != null) {
                item.setOwner(this.payoutPlayer);
            }
            item.setUnlimitedLifetime(true);
        });
        if (item2 != null) {
            this.plugin.machines().markDirty();
        }
    }

    public long takeUndelivered() {
        long l = 0L;
        for (int n : this.ejectQueue) {
            l += (long)n;
        }
        this.ejectQueue.clear();
        return l;
    }

    public UUID payoutPlayer() {
        return this.payoutPlayer;
    }

    public void save(ConfigurationSection configurationSection) {
        configurationSection.set("type", (Object)this.type.id);
        configurationSection.set("world", (Object)this.worldName);
        configurationSection.set("x", (Object)this.x);
        configurationSection.set("y", (Object)this.y);
        configurationSection.set("z", (Object)this.z);
        configurationSection.set("facing", (Object)this.facing);
        configurationSection.set("owner", this.owner == null ? null : this.owner.toString());
        configurationSection.set("ownerName", (Object)this.ownerName);
        configurationSection.set("bank", (Object)this.bank);
        configurationSection.set("wagered", (Object)this.wagered);
        configurationSection.set("paidOut", (Object)this.paidOut);
        configurationSection.set("games", (Object)this.games);
        long l = 0L;
        for (int n : this.ejectQueue) {
            l += (long)n;
        }
        configurationSection.set("pendingPayout", (Object)(l > 0L ? Long.valueOf(l) : null));
        configurationSection.set("pendingPlayer", l > 0L && this.payoutPlayer != null ? this.payoutPlayer.toString() : null);
        this.saveState(configurationSection.createSection("state"));
    }

    public static Machine load(CasinoPlugin casinoPlugin, UUID uUID, ConfigurationSection configurationSection) {
        MachineType machineType = MachineType.byId(configurationSection.getString("type", ""));
        if (machineType == null) {
            return null;
        }
        String string = configurationSection.getString("world");
        int n = configurationSection.getInt("x");
        int n2 = configurationSection.getInt("y");
        int n3 = configurationSection.getInt("z");
        int n4 = configurationSection.getInt("facing");
        Machine machine = Machine.create(casinoPlugin, uUID, machineType, string, n, n2, n3, n4);
        String string2 = configurationSection.getString("owner");
        machine.owner = string2 == null ? null : UUID.fromString(string2);
        machine.ownerName = configurationSection.getString("ownerName");
        machine.bank = configurationSection.getLong("bank");
        machine.wagered = configurationSection.getLong("wagered");
        machine.paidOut = configurationSection.getLong("paidOut");
        machine.games = configurationSection.getLong("games");
        ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection("state");
        if (configurationSection2 != null) {
            machine.loadState(configurationSection2);
        }
        return machine;
    }

    public static Machine create(CasinoPlugin casinoPlugin, UUID uUID, MachineType machineType, String string, int n, int n2, int n3, int n4) {
        return switch (machineType) {
            default -> throw new MatchException(null, null);
            case MachineType.ROULETTE -> new RouletteMachine(casinoPlugin, uUID, string, n, n2, n3, n4);
            case MachineType.SLOT_MACHINE -> new SlotMachine(casinoPlugin, uUID, string, n, n2, n3, n4);
        };
    }

    protected String visualTag() {
        return this.id.toString();
    }

    protected static boolean viewersNear(Location location, double d) {
        if (location.getWorld() == null) {
            return false;
        }
        double d2 = d * d;
        for (Player player : location.getWorld().getPlayers()) {
            if (!(player.getLocation().distanceSquared(location) <= d2)) continue;
            return true;
        }
        return false;
    }
}

