/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.Component
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
 *  org.bukkit.util.Vector
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package de.nick.casino.machine;

import de.nick.casino.CasinoPlugin;
import de.nick.casino.Msg;
import de.nick.casino.game.RouletteSpin;
import de.nick.casino.machine.Machine;
import de.nick.casino.machine.MachineType;
import de.nick.casino.menu.RouletteMenu;
import de.nick.casino.util.Displays;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class RouletteMachine
extends Machine {
    private static final int[][] FOOTPRINT = new int[][]{{0, 0, 0}, {1, 0, 0}, {0, 0, 1}, {1, 0, 1}};
    public static final String[] COLOR_KEYS = new String[]{"gui.roulettecasino.red", "gui.roulettecasino.black", "gui.roulettecasino.green"};
    private static final TextColor[] HEADER_COLORS = new TextColor[]{TextColor.color((int)16739163), TextColor.color((int)13224401), TextColor.color((int)5628026)};
    private static final Color RED_CONFETTI = Color.fromRGB((int)14629707);
    private static final Color GOLD_CONFETTI = Color.fromRGB((int)16765288);
    private static final Color GREEN_CONFETTI = Color.fromRGB((int)3594374);
    private int frame;
    private boolean locked;
    private RouletteSpin.Plan plan;
    private long spinStart;
    private int stepsRemaining;
    private int result;
    private final int[] displayedBets = new int[3];
    private int displayTicks;
    private boolean payoutsPending;
    private ItemDisplay rotor;
    private ItemDisplay ball;
    private TextDisplay panel;
    private boolean panelDirty = true;

    public RouletteMachine(CasinoPlugin casinoPlugin, UUID uUID, String string, int n, int n2, int n3, int n4) {
        super(casinoPlugin, uUID, MachineType.ROULETTE, string, n, n2, n3, n4);
    }

    @Override
    public int[][] footprintOffsets() {
        return FOOTPRINT;
    }

    @Override
    public Material bedrockMaterial(int n) {
        return Material.GREEN_WOOL;
    }

    @Override
    public int maxMultiplier() {
        return 36;
    }

    public static int multiplier(int n) {
        return n == 2 ? 36 : 2;
    }

    @Override
    public boolean isBusy() {
        return this.locked;
    }

    @Override
    protected void spawnVisualsImpl(World world) {
        for (int i = 0; i < 4; ++i) {
            this.track(Displays.item(world, (double)(this.x + (i & 1)) + 0.5, (double)this.y + 0.5, (double)(this.z + (i >> 1)) + 0.5, Key.key((String)"casino", (String)("roulette_body_" + i)), new Quaternionf(), false, this.visualTag()));
        }
        this.rotor = this.track(Displays.item(world, (double)this.x + 1.0, (double)this.y + 0.5, (double)this.z + 1.0, Key.key((String)"casino", (String)("roulette_rotor" + this.plugin.rotSuffix())), RouletteMachine.rotorRotation(this.frame), false, this.visualTag()));
        this.ball = this.track(Displays.item(world, (double)this.x + 1.0, (double)this.y + 0.5, (double)this.z + 1.0, Key.key((String)"casino", (String)("roulette_ball" + this.plugin.rotSuffix())), new Quaternionf(), false, this.visualTag()));
        this.panel = this.track(Displays.text(world, (double)this.x + 1.0, (double)this.y + 1.95, (double)this.z + 1.0, this.visualTag()));
        this.panelDirty = true;
        this.applyBall(2.0 * (double)this.frame, 12.8, 0.0, false);
        this.updatePanel();
    }

    private static Quaternionf rotorRotation(double d) {
        return new Quaternionf().rotationY((float)(d * 0.16981581911296179));
    }

    private void applyBall(double d, double d2, double d3, boolean bl) {
        if (this.ball == null || !this.ball.isValid()) {
            return;
        }
        double d4 = (d + 0.5) * 0.16981581911296179 - 1.5707963267948966;
        double d5 = RouletteSpin.ballRadius(d3);
        Vector3f vector3f = new Vector3f((float)(d5 * Math.cos(d4) / 16.0), (float)(d2 / 16.0 - 0.5), (float)(d5 * Math.sin(d4) / 16.0));
        Quaternionf quaternionf = new Quaternionf().rotationXYZ((float)(d4 * 3.0), 0.0f, (float)(d4 * 2.0));
        this.ball.setInterpolationDuration(bl ? 1 : 0);
        Displays.transform(this.ball, vector3f, quaternionf);
    }

    private void applyRotor(double d, boolean bl) {
        if (this.rotor == null || !this.rotor.isValid()) {
            return;
        }
        this.rotor.setInterpolationDuration(bl ? 1 : 0);
        Displays.transform(this.rotor, new Vector3f(), RouletteMachine.rotorRotation(d));
    }

    @Override
    public void openGame(Player player) {
        if (this.locked) {
            Msg.actionbar(player, Msg.tr("message.casino.roulette_busy", new Object[0]));
            return;
        }
        new RouletteMenu(this.plugin, this, player).open();
    }

    public Component start(Player player, int[] nArray) {
        int n;
        if (this.locked || this.removed) {
            return Msg.tr("message.casino.roulette_busy", new Object[0]);
        }
        int n2 = -1;
        int n3 = 0;
        for (n = 0; n < 3; ++n) {
            if (nArray[n] <= 0) continue;
            if (n2 >= 0) {
                return Msg.tr("gui.roulettecasino.roulette.several", new Object[0]);
            }
            n2 = n;
            n3 = nArray[n];
        }
        if (n2 < 0) {
            return Msg.tr("message.roulettecasino.no_bet", new Object[0]);
        }
        n = RouletteMachine.multiplier(n2);
        if ((long)n3 > this.maxStake(n)) {
            return Msg.maxStake(this.maxStake(n));
        }
        World world = this.world();
        int n4 = ThreadLocalRandom.current().nextInt(37);
        this.result = n4 == 0 ? 2 : ((n4 & 1) == 0 ? 1 : 0);
        int n5 = this.result == n2 ? n3 * n : 0;
        this.bookRound(player, n3, n5);
        this.payoutsPending = n5 > 0;
        this.locked = true;
        this.plan = RouletteSpin.plan(this.frame, n4);
        this.spinStart = world.getGameTime();
        this.stepsRemaining = this.plan.steps();
        this.displayTicks = 0;
        this.displayedBets[2] = 0;
        this.displayedBets[1] = 0;
        this.displayedBets[0] = 0;
        this.displayedBets[n2] = n3;
        this.panelDirty = true;
        world.playSound(new Location(world, (double)this.x + 0.5, (double)this.y + 0.5, (double)this.z + 0.5), "roulettecasino:start", SoundCategory.BLOCKS, 1.0f, 1.0f);
        this.updatePanel();
        return null;
    }

    @Override
    public void tick(World world, long l, boolean bl) {
        if (this.displayTicks > 0 && --this.displayTicks == 0) {
            this.displayedBets[2] = 0;
            this.displayedBets[1] = 0;
            this.displayedBets[0] = 0;
            this.panelDirty = true;
        }
        if (this.locked) {
            if (this.stepsRemaining > 0) {
                double d = l - this.spinStart;
                double d2 = this.plan.phase(d - 1.0);
                double d3 = this.plan.phase(d);
                this.stepsRemaining = Math.max(1, (int)Math.ceil((double)(this.plan.initial() + this.plan.steps()) - d3));
                Location location = new Location(world, (double)this.x + 0.5, (double)this.y + 0.5, (double)this.z + 0.5);
                if ((int)Math.floor(d2 * 3.0) != (int)Math.floor(d3 * 3.0)) {
                    world.playSound(location, "roulettecasino:tick", SoundCategory.BLOCKS, 0.44f, (float)(0.8 + Math.min(0.45, this.plan.speed(d) * 0.4)));
                }
                this.applyRotor(d3, true);
                this.applyBall(this.plan.ballPhase(d), this.plan.ballHeight(d), this.plan.track(d), true);
                if (d >= (double)this.plan.resultTick()) {
                    this.stepsRemaining = 0;
                    this.frame = this.plan.finalFrame();
                    this.applyRotor(this.frame, true);
                    this.displayTicks = 20;
                    if (this.payoutsPending) {
                        this.celebrate(world);
                    }
                    world.playSound(location, this.payoutsPending ? "roulettecasino:win" : "roulettecasino:lose", SoundCategory.BLOCKS, this.payoutsPending ? 1.2f : 0.95f, 1.0f);
                    this.plugin.machines().markDirty();
                }
            } else if (!this.ejectQueue.isEmpty()) {
                if (l % 3L == 0L) {
                    double d = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
                    this.ejectNext(new Location(world, (double)this.x + 1.0, (double)this.y + 1.0, (double)this.z + 1.0), new Vector(Math.cos(d) * 0.2, 0.3, Math.sin(d) * 0.2));
                    world.playSound(new Location(world, (double)this.x + 0.5, (double)this.y + 0.5, (double)this.z + 0.5), "roulettecasino:payout", SoundCategory.BLOCKS, 0.43f, 0.95f + ThreadLocalRandom.current().nextFloat() * 0.2f);
                }
            } else {
                this.locked = false;
                this.payoutsPending = false;
                this.plan = null;
                this.plugin.machines().markDirty();
            }
        }
        if (this.panelDirty) {
            this.updatePanel();
        }
    }

    private void celebrate(World world) {
        double d = (double)this.x + 1.0;
        double d2 = (double)this.y + 1.1;
        double d3 = (double)this.z + 1.0;
        world.spawnParticle(Particle.DUST, d, d2, d3, 18, 0.52, 0.16, 0.52, 0.18, (Object)new Particle.DustOptions(RED_CONFETTI, 1.15f));
        world.spawnParticle(Particle.DUST, d, d2, d3, 18, 0.52, 0.16, 0.52, 0.18, (Object)new Particle.DustOptions(GOLD_CONFETTI, 1.25f));
        world.spawnParticle(Particle.DUST, d, d2, d3, 18, 0.52, 0.16, 0.52, 0.18, (Object)new Particle.DustOptions(GREEN_CONFETTI, 1.15f));
        world.spawnParticle(Particle.FIREWORK, d, d2, d3, 20, 0.42, 0.2, 0.42, 0.2);
    }

    private void updatePanel() {
        this.panelDirty = false;
        if (this.panel == null || !this.panel.isValid()) {
            return;
        }
        TextComponent.Builder builder = Component.text();
        boolean bl = false;
        for (int i = 0; i < 3; ++i) {
            if (this.displayedBets[i] <= 0) continue;
            if (bl) {
                builder.append((Component)Component.newline());
            }
            bl = true;
            builder.append(((TranslatableComponent)Component.translatable((String)COLOR_KEYS[i]).color(HEADER_COLORS[i])).decorate(TextDecoration.BOLD));
            builder.append((Component)Component.newline());
            ((TextComponent.Builder)builder.append(Msg.diamondIcon())).append((Component)Component.text((String)(" \u00d7" + this.displayedBets[i]), (TextColor)NamedTextColor.WHITE));
        }
        if (!bl) {
            this.panel.text((Component)Component.empty());
            this.panel.setBackgroundColor(Color.fromARGB((int)0));
            this.panel.setTextOpacity((byte)0);
        } else {
            this.panel.text(builder.build());
            this.panel.setBackgroundColor(Color.fromARGB((int)-266188488));
            this.panel.setTextOpacity((byte)-1);
        }
    }

    @Override
    protected void saveState(ConfigurationSection configurationSection) {
        configurationSection.set("frame", (Object)this.frame);
    }

    @Override
    protected void loadState(ConfigurationSection configurationSection) {
        this.frame = Math.floorMod(configurationSection.getInt("frame"), 37);
    }
}

