/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.ComponentLike
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.TextComponent$Builder
 *  net.kyori.adventure.text.TranslatableComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.SoundCategory
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.ItemDisplay
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.TextDisplay
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.util.Vector
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package de.nick.casino.machine;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.Msg;
import de.nick.casino.game.CasinoIdleLights;
import de.nick.casino.game.SlotMachineGame;
import de.nick.casino.game.SlotMachineSpin;
import de.nick.casino.machine.Machine;
import de.nick.casino.machine.MachineType;
import de.nick.casino.menu.SlotMenu;
import de.nick.casino.util.Displays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class SlotMachine
extends Machine {
    private static final int[][] FOOTPRINT = new int[][]{{0, 0, 0}, {0, 1, 0}, {0, 2, 0}};
    public static final double[] LEVER_PIVOT = new double[]{20.6, 9.5, 8.0};
    private static final double[] DRUM_X0 = new double[]{-0.5, 5.5, 11.5};
    private static final int[] FRAMES = new int[]{64, 16, 16};
    private static final TextColor[] RESULT_COLOR = new TextColor[]{TextColor.color((int)0xFF5555), TextColor.color((int)0xFFFF55), TextColor.color((int)0x55FF55), TextColor.color((int)0x55FFFF), TextColor.color((int)0xFFAA00)};
    private static final TextColor[] RESULT_NUMBER = new TextColor[]{TextColor.color((int)0xFFFFFF), TextColor.color((int)0xFFFFFF), TextColor.color((int)0xFFFF55), TextColor.color((int)0xFFFF55), TextColor.color((int)0xFFFFFF)};
    private static final TextColor TITLE = TextColor.color((int)16379073);
    private static final Color GOLD_CONFETTI = Color.fromRGB((int)16765288);
    private static final Color RED_CONFETTI = Color.fromRGB((int)14629707);
    private static final Color GREEN_CONFETTI = Color.fromRGB((int)3594374);
    private static final String[] ICONS = new String[]{"seven", "diamond_sym", "bar", "bell", "lemon", "cherry"};
    private int[] stops = new int[3];
    private int[] from = new int[3];
    private long seed;
    private SlotMachineSpin.Plan plan;
    private long spinStart = -1L;
    private boolean locked;
    private int stage;
    private int cueIndex;
    private int displayTicks;
    private int displayedBet;
    private final ItemDisplay[] drums = new ItemDisplay[3];
    private final String[] drumState = new String[3];
    private ItemDisplay lights;
    private String lightState = "";
    private ItemDisplay lever;
    private double leverState = Double.NaN;
    private TextDisplay panel;
    private String panelState = "";

    public SlotMachine(CasinoPlugin casinoPlugin, UUID uUID, String string, int n, int n2, int n3, int n4) {
        super(casinoPlugin, uUID, MachineType.SLOT_MACHINE, string, n, n2, n3, n4);
    }

    @Override
    public int[][] footprintOffsets() {
        return FOOTPRINT;
    }

    @Override
    public Material bedrockMaterial(int n) {
        return switch (n) {
            case 0 -> Material.RED_TERRACOTTA;
            case 1 -> Material.GOLD_BLOCK;
            default -> Material.REDSTONE_LAMP;
        };
    }

    @Override
    public int maxMultiplier() {
        return 100;
    }

    @Override
    public boolean isBusy() {
        return this.locked;
    }

    @Override
    protected List<double[]> extraVisualPoints() {
        Location location = this.local(LEVER_PIVOT[0], LEVER_PIVOT[1], LEVER_PIVOT[2]);
        return List.of((Object)new double[]{location.getX(), location.getZ()});
    }

    public int[] stops() {
        return (int[])this.stops.clone();
    }

    @Override
    protected void spawnVisualsImpl(World world) {
        int n;
        Quaternionf quaternionf = this.facingRotation();
        String[] stringArray = new String[]{"lower", "upper", "top"};
        for (n = 0; n < 3; ++n) {
            this.track(Displays.item(world, (double)this.x + 0.5, (double)this.y + 0.5 + (double)n, (double)this.z + 0.5, Key.key((String)"casino", (String)("slot_body_" + stringArray[n])), quaternionf, false, this.visualTag()));
        }
        for (n = 0; n < 3; ++n) {
            this.drums[n] = this.track(Displays.item(world, (double)this.x + 0.5, (double)this.y + 1.5, (double)this.z + 0.5, Key.key((String)"casino", (String)("slot_drum_" + n + "_0_0")), quaternionf, true, this.visualTag()));
            this.drumState[n] = "";
        }
        this.lights = this.track(Displays.item(world, (double)this.x + 0.5, (double)this.y + 2.5, (double)this.z + 0.5, Key.key((String)"casino", (String)("slot_lights" + this.plugin.rotSuffix())), quaternionf, true, this.visualTag()));
        this.lightState = "";
        Location location = this.local(LEVER_PIVOT[0], LEVER_PIVOT[1], LEVER_PIVOT[2]);
        this.lever = this.track(Displays.item(world, location.getX(), location.getY(), location.getZ(), Key.key((String)"casino", (String)("slot_lever" + this.plugin.rotSuffix())), this.leverRotation(-14.0), false, this.visualTag()));
        this.leverState = Double.NaN;
        this.panel = this.track(Displays.text(world, (double)this.x + 0.5, (double)this.y + 3.3, (double)this.z + 0.5, this.visualTag()));
        this.panelState = "";
        this.render(world, world.getGameTime(), true);
    }

    private Quaternionf leverRotation(double d) {
        return this.facingRotation().mul((Quaternionfc)new Quaternionf().rotationX((float)Math.toRadians(d)));
    }

    private void render(World world, long l, boolean bl) {
        double d;
        double d2;
        double d3;
        int n;
        double d4 = CasinoIdleLights.clock(l, this.x, this.y, this.z);
        double[] dArray = new double[3];
        double[] dArray2 = new double[3];
        double[] dArray3 = new double[15];
        SlotMachineSpin.Plan plan = this.plan;
        if (plan != null && this.spinStart >= 0L) {
            double d5 = l - this.spinStart;
            for (n = 0; n < 3; ++n) {
                dArray[n] = plan.turn(n, d5);
                dArray2[n] = plan.speed(n, d5);
            }
            d3 = plan.lever(d5);
            d2 = CasinoIdleLights.afterGame(plan.lamp(d5), CasinoIdleLights.beacon(d4), d5 - (double)plan.endTick());
            d = plan.flash(d5);
            for (n = 0; n < 15; ++n) {
                dArray3[n] = CasinoIdleLights.afterGame(plan.bulb(n, d5), CasinoIdleLights.bulb(n, 15, d4), d5 - (double)plan.endTick());
            }
        } else {
            int n2;
            for (n2 = 0; n2 < 3; ++n2) {
                dArray[n2] = this.stops[n2];
            }
            d3 = -14.0;
            d2 = CasinoIdleLights.beacon(d4);
            d = 1.0;
            for (n2 = 0; n2 < 15; ++n2) {
                dArray3[n2] = CasinoIdleLights.bulb(n2, 15, d4);
            }
        }
        for (int i = 0; i < 3; ++i) {
            if (this.drums[i] == null || !this.drums[i].isValid()) continue;
            int n3 = dArray2[i] < 0.35 ? 0 : (dArray2[i] < 0.62 ? 1 : 2);
            n = FRAMES[n3];
            double d6 = dArray[i] - 8.0 * Math.floor(dArray[i] / 8.0);
            int n4 = Math.floorMod((int)Math.round(d6 * (double)n / 8.0), n);
            String string = "slot_drum_" + i + "_" + n3 + "_" + n4;
            String string2 = string + "|" + Math.round(d * 100.0);
            if (!bl && string2.equals(this.drumState[i])) continue;
            this.drumState[i] = string2;
            ArrayList<Color> arrayList = null;
            if (d < 0.999) {
                arrayList = new ArrayList<Color>();
                for (int j = 0; j < 16; ++j) {
                    double d7 = -Math.toRadians(60.0) + 2.0 * Math.toRadians(60.0) * ((double)j + 0.5) / 16.0;
                    arrayList.add(Displays.gray((0.42 + 0.58 * Math.pow(Math.cos(d7), 3.2)) * d));
                }
                arrayList.add(Displays.gray(0.45 * d));
            }
            this.drums[i].setItemStack(Displays.modelItem(Key.key((String)"casino", (String)string), arrayList));
        }
        if (this.lights != null && this.lights.isValid()) {
            ArrayList<Color> arrayList = new ArrayList<Color>(16);
            StringBuilder stringBuilder = new StringBuilder();
            for (n = 0; n < 15; ++n) {
                Color color = Displays.gray(0.28 + 0.72 * dArray3[n]);
                arrayList.add(color);
                stringBuilder.append(color.getRed()).append(',');
            }
            Color color = Displays.gray(0.3 + 0.7 * d2);
            arrayList.add(color);
            stringBuilder.append(color.getRed());
            String string = stringBuilder.toString();
            if (bl || !string.equals(this.lightState)) {
                this.lightState = string;
                this.lights.setItemStack(Displays.modelItem(Key.key((String)"casino", (String)("slot_lights" + this.plugin.rotSuffix())), arrayList));
            }
        }
        if (this.lever != null && this.lever.isValid() && (bl || Math.abs(d3 - this.leverState) > 0.01 || Double.isNaN(this.leverState))) {
            this.leverState = d3;
            Displays.transform(this.lever, new Vector3f(), this.leverRotation(d3));
        }
        this.updatePanel(l);
    }

    private void updatePanel(long l) {
        if (this.panel == null || !this.panel.isValid()) {
            return;
        }
        boolean bl = this.plan != null && this.spinStart >= 0L && l - this.spinStart >= (long)this.plan.resultTick();
        String string = this.displayedBet + "|" + bl + "|" + Arrays.toString(this.stops);
        if (string.equals(this.panelState)) {
            return;
        }
        this.panelState = string;
        if (this.displayedBet <= 0) {
            this.panel.text((Component)Component.empty());
            this.panel.setBackgroundColor(Color.fromARGB((int)0));
            return;
        }
        TextComponent.Builder builder = Component.text();
        builder.append(Component.translatable((String)"gui.roulettecasino.slot.panel_title").color(TITLE));
        builder.append((Component)Component.newline());
        ((TextComponent.Builder)builder.append(Msg.diamondIcon())).append((Component)Component.text((String)(" \u00d7" + this.displayedBet), (TextColor)NamedTextColor.WHITE));
        if (bl && this.plan != null) {
            builder.append((Component)Component.newline());
            int[] nArray = SlotMachineGame.symbols(this.stops);
            for (int i = 0; i < 3; ++i) {
                if (i > 0) {
                    builder.append((Component)Component.text((String)" "));
                }
                builder.append(((TextComponent)Component.text((String)this.plugin.meta().icons.get(ICONS[nArray[i]])).font(Msg.ICON_FONT)).color((TextColor)NamedTextColor.WHITE));
            }
            builder.append((Component)Component.newline());
            builder.append(SlotMachine.resultText(this.plan.result()));
        }
        this.panel.text(builder.build());
        this.panel.setBackgroundColor(Color.fromARGB((int)-266188488));
    }

    private static Component resultText(SlotMachineGame.Result result) {
        int n = result.kind().ordinal();
        String string = switch (result.kind()) {
            default -> throw new MatchException(null, null);
            case SlotMachineGame.Kind.NONE -> "gui.roulettecasino.slot.result_lose";
            case SlotMachineGame.Kind.PUSH -> "gui.roulettecasino.slot.result_push";
            case SlotMachineGame.Kind.WIN, SlotMachineGame.Kind.BIG -> "gui.roulettecasino.slot.result_win";
            case SlotMachineGame.Kind.JACKPOT -> "gui.roulettecasino.slot.result_jackpot";
        };
        TranslatableComponent translatableComponent = result.kind() != SlotMachineGame.Kind.NONE && result.kind() != SlotMachineGame.Kind.PUSH ? Component.translatable((String)string, (ComponentLike[])new ComponentLike[]{Component.text((String)("\u00d7" + result.multiplier())).color(RESULT_NUMBER[n])}) : Component.translatable((String)string);
        return translatableComponent.color(RESULT_COLOR[n]).decorate(TextDecoration.BOLD);
    }

    @Override
    public void openGame(Player player) {
        if (this.locked) {
            Msg.actionbar(player, Msg.tr("message.roulettecasino.slot_machine.busy", new Object[0]));
            return;
        }
        new SlotMenu(this.plugin, this, player).open();
    }

    public Component start(Player player, int n) {
        if (this.locked || this.removed) {
            return Msg.tr("message.roulettecasino.slot_machine.busy", new Object[0]);
        }
        if (n <= 0) {
            return Msg.tr("message.roulettecasino.slot_machine.no_bet", new Object[0]);
        }
        if ((long)n > this.maxStake(100)) {
            return Msg.maxStake(this.maxStake(100));
        }
        World world = this.world();
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        int[] nArray = new int[]{threadLocalRandom.nextInt(8), threadLocalRandom.nextInt(8), threadLocalRandom.nextInt(8)};
        this.from = (int[])this.stops.clone();
        this.stops = nArray;
        this.seed = threadLocalRandom.nextLong();
        this.plan = SlotMachineSpin.plan(this.from, this.stops, this.seed);
        this.spinStart = world.getGameTime();
        this.stage = 0;
        this.cueIndex = 0;
        this.displayTicks = 0;
        this.locked = true;
        int n2 = SlotMachineGame.evaluate(this.stops).multiplier();
        this.bookRound(player, n, n * n2);
        this.displayedBet = n;
        return null;
    }

    @Override
    public void tick(World world, long l, boolean bl) {
        boolean bl2;
        if (this.displayTicks > 0 && --this.displayTicks == 0) {
            this.displayedBet = 0;
        }
        if (this.locked) {
            SlotMachineSpin.Plan plan = this.plan;
            if (plan == null) {
                this.locked = false;
            } else {
                long l2 = l - this.spinStart;
                if (l2 < 0L) {
                    this.spinStart = l;
                    l2 = 0L;
                }
                this.cues(world, plan, l2);
                this.teaseSparks(world, plan, l2);
                if (this.stage < 1 && l2 >= (long)plan.resultTick()) {
                    this.stage = 1;
                    this.displayTicks = 70;
                    if (l2 - (long)plan.resultTick() <= 10L) {
                        this.announce(world, plan);
                    }
                    this.plugin.machines().markDirty();
                }
                if (this.stage >= 1) {
                    this.afterburst(world, plan, l2 - (long)plan.resultTick());
                }
                if (this.stage >= 1 && l2 >= (long)(plan.resultTick() + 12) && !this.ejectQueue.isEmpty()) {
                    int n;
                    int n2 = this.ejectQueue.size() > 48 ? 1 : (n = this.ejectQueue.size() > 16 ? 2 : 3);
                    if (l % (long)n == 0L) {
                        int[] nArray = this.frontStep();
                        double d = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.16;
                        Location location = this.local(8.0, 7.0, 17.0);
                        this.ejectNext(location, new Vector((double)nArray[0] * 0.18 + (double)nArray[1] * d, 0.22, (double)nArray[1] * 0.18 + (double)nArray[0] * d));
                        if (n > 1 || l % 2L == 0L) {
                            world.playSound(this.local(8.0, 6.5, 16.5), "roulettecasino:slot_payout", SoundCategory.BLOCKS, 0.5f, 0.9f + ThreadLocalRandom.current().nextFloat() * 0.25f);
                        }
                    }
                } else if (this.stage >= 1 && this.ejectQueue.isEmpty() && l2 >= (long)plan.readyTick()) {
                    this.locked = false;
                    this.plugin.machines().markDirty();
                }
                if (this.locked || l2 >= (long)(plan.endTick() + 20)) {
                    // empty if block
                }
            }
        }
        if (this.plan != null && !this.locked && this.spinStart >= 0L && l - this.spinStart > (long)(this.plan.endTick() + 40)) {
            this.plan = null;
            this.spinStart = -1L;
        }
        boolean bl3 = bl2 = this.plan != null || bl;
        if (bl2 && (this.plan != null || l % 2L == 0L)) {
            this.render(world, l, false);
        } else if (this.displayTicks == 0 && this.displayedBet == 0) {
            this.updatePanel(l);
        }
    }

    private void sound(World world, String string, double d, double d2, double d3, float f, float f2) {
        world.playSound(this.local(d, d2, d3), "roulettecasino:" + string, SoundCategory.BLOCKS, f, f2);
    }

    private void cues(World world, SlotMachineSpin.Plan plan, long l) {
        List<SlotMachineSpin.Cue> list = plan.cues();
        while (this.cueIndex < list.size() && (long)list.get(this.cueIndex).tick() <= l) {
            SlotMachineSpin.Cue cue;
            double d = (cue = list.get(this.cueIndex++)).reel() >= 0 ? DRUM_X0[cue.reel()] + 2.5 : 8.0;
            switch (cue.sound()) {
                case LEVER: {
                    this.sound(world, "slot_lever", 20.6, 10.0, 12.0, cue.volume(), cue.pitch());
                    break;
                }
                case WHIRR: {
                    this.sound(world, "slot_whirr", 8.0, 25.75, 14.5, cue.volume(), cue.pitch());
                    break;
                }
                case CLICK: {
                    this.sound(world, "slot_click", d, 25.75, 14.5, cue.volume(), cue.pitch());
                    break;
                }
                case STOP: {
                    this.sound(world, "slot_stop", d, 25.75, 14.5, cue.volume(), cue.pitch());
                    Location location = this.local(d, 25.75, 15.5);
                    world.spawnParticle(Particle.CRIT, location, 5, 0.12, 0.1, 0.04, 0.12);
                }
            }
        }
    }

    private void teaseSparks(World world, SlotMachineSpin.Plan plan, long l) {
        double d;
        if (plan.tease() && l % 3L == 0L && (d = plan.speed(2, l)) > 0.0 && d < 0.55) {
            world.spawnParticle(Particle.END_ROD, this.local(8.0, 25.75, 15.5), 3, 0.38, 0.22, 0.04, 0.02);
        }
    }

    private void announce(World world, SlotMachineSpin.Plan plan) {
        switch (plan.result().kind()) {
            case JACKPOT: {
                this.sound(world, "slot_jackpot", 8.0, 25.75, 14.5, 1.5f, 1.0f);
                this.burst(world, 1.0, true);
                break;
            }
            case BIG: {
                this.sound(world, "slot_win", 8.0, 25.75, 14.5, 1.3f, 1.0f);
                this.burst(world, 0.8, false);
                break;
            }
            case WIN: {
                this.sound(world, "slot_win", 8.0, 25.75, 14.5, 1.05f, 1.0f);
                this.burst(world, 0.5, false);
                break;
            }
            case PUSH: {
                this.sound(world, "slot_push", 8.0, 25.75, 14.5, 0.9f, 1.0f);
                world.spawnParticle(Particle.END_ROD, this.local(8.0, 7.5, 17.0), 8, 0.25, 0.1, 0.1, 0.03);
                break;
            }
            default: {
                this.sound(world, "slot_lose", 8.0, 25.75, 14.5, 0.95f, 1.0f);
                world.spawnParticle(Particle.SMOKE, this.local(8.0, 39.5, 7.5), 6, 0.12, 0.05, 0.12, 0.01);
                world.spawnParticle(Particle.ELECTRIC_SPARK, this.local(8.0, 25.75, 15.5), 6, 0.4, 0.2, 0.05, 0.1);
            }
        }
    }

    private void afterburst(World world, SlotMachineSpin.Plan plan, long l) {
        SlotMachineGame.Kind kind = plan.result().kind();
        if (kind == SlotMachineGame.Kind.JACKPOT) {
            if (l >= 5L && l <= 35L && l % 5L == 0L) {
                this.burst(world, 0.8 - 0.1 * (double)(l / 5L - 1L), false);
            }
        } else if (kind == SlotMachineGame.Kind.BIG) {
            if (l == 7L || l == 14L) {
                this.burst(world, 0.5, false);
            }
        } else if (kind == SlotMachineGame.Kind.WIN && l == 8L) {
            this.burst(world, 0.35, false);
        }
    }

    private void burst(World world, double d, boolean bl) {
        ItemStack itemStack = new ItemStack(Material.GOLD_NUGGET);
        Location location = this.local(8.0, 8.0, 17.5);
        world.spawnParticle(Particle.ITEM, location, (int)Math.round(26.0 * d), 0.22, 0.1, 0.12, 0.2, (Object)itemStack);
        Location location2 = this.local(8.0, 28.75, 16.0);
        int n = (int)Math.round(26.0 * d);
        world.spawnParticle(Particle.DUST, location2, n, 0.55, 0.45, 0.25, 0.16, (Object)new Particle.DustOptions(GOLD_CONFETTI, 1.25f));
        world.spawnParticle(Particle.DUST, location2, n, 0.55, 0.45, 0.25, 0.16, (Object)new Particle.DustOptions(RED_CONFETTI, 1.15f));
        world.spawnParticle(Particle.DUST, location2, n, 0.55, 0.45, 0.25, 0.16, (Object)new Particle.DustOptions(GREEN_CONFETTI, 1.15f));
        Location location3 = this.local(8.0, 40.0, 7.5);
        world.spawnParticle(Particle.FIREWORK, location3, (int)Math.round(22.0 * d), 0.25, 0.2, 0.25, 0.22);
        world.spawnParticle(Particle.END_ROD, location3, (int)Math.round(14.0 * d), 0.25, 0.2, 0.25, 0.16);
        if (bl) {
            world.spawnParticle(Particle.TOTEM_OF_UNDYING, location2, 36, 0.5, 0.5, 0.3, 0.35);
            world.spawnParticle(Particle.ITEM, location3.clone().add(0.0, -0.3, 0.0), 30, 0.3, 0.1, 0.3, 0.28, (Object)itemStack);
        }
    }

    @Override
    protected void saveState(ConfigurationSection configurationSection) {
        configurationSection.set("stops", (Object)Arrays.stream(this.stops).boxed().toList());
    }

    @Override
    protected void loadState(ConfigurationSection configurationSection) {
        List list = configurationSection.getIntegerList("stops");
        if (list.size() == 3) {
            for (int i = 0; i < 3; ++i) {
                this.stops[i] = Math.floorMod((Integer)list.get(i), 8);
            }
        }
    }
}

