/*
 * Decompiled with CFR 0.152.
 */
package de.nick.casino.game;

import de.nick.casino.game.SlotMachineGame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SlotMachineSpin {
    public static final int REELS = 3;
    public static final int FACES = 8;
    public static final int BULBS = 15;
    public static final int LEVER_DOWN = 8;
    public static final int LEVER_HOLD = 2;
    public static final int LEVER_BACK = 16;
    public static final double LEVER_REST = -14.0;
    public static final double LEVER_PULLED = 95.0;
    public static final int[] START = new int[]{6, 8, 10};
    public static final int ACCEL = 6;
    public static final double SPEED = 0.85;
    private static final int[] CRUISE = new int[]{16, 30, 44};
    private static final int JITTER = 4;
    public static final int MIN_BRAKE = 8;
    public static final int GAP = 8;
    private static final int TEASE_DECEL = 6;
    private static final int TEASE_CRAWL = 20;
    private static final int TEASE_BRAKE = 6;
    public static final double TEASE_MIN_SPEED = 0.2;
    public static final double RECOIL = 0.1;
    public static final int RECOIL_TICKS = 12;
    private static final int CLICK_FACES = 6;
    public static final int RESULT_DELAY = 10;
    private static final int[] CELEBRATION = new int[]{24, 30, 44, 60, 96};
    private static final int[] READY = new int[]{0, 0, 8, 16, 40};
    public static final int[] LAST_BURST = new int[]{0, 0, 8, 14, 35};

    private SlotMachineSpin() {
    }

    private static double recoil(double d) {
        return !(d <= 0.0) && !(d >= 12.0) ? -0.1 * StrictMath.exp(-d / 2.2) * StrictMath.sin(Math.PI * 2 * d / 4.5) : 0.0;
    }

    private static double align(double d, int n) {
        double d2 = ((double)n - d) % 8.0;
        return d2 < 0.0 ? d2 + 8.0 : d2;
    }

    private static Reel build(int n, int n2, int n3, int n4, boolean bl) {
        Reel reel = new Reel(START[n], n2);
        reel.add(6.0, 0.14166666666666666);
        reel.add(n4, 0.0);
        if (!bl) {
            double d = 3.4;
            double d2 = d + SlotMachineSpin.align(reel.turn + d, n3);
            double d3 = 2.0 * d2 / 0.85;
            reel.add(d3, -0.85 / d3);
        } else {
            double d = 2.55;
            double d4 = 26.0;
            double d5 = d + 0.2 * d4;
            double d6 = d5 + SlotMachineSpin.align(reel.turn + d5, n3);
            double d7 = (d6 - d) / d4;
            reel.add(6.0, (d7 - 0.85) / 6.0);
            reel.add(20.0, 0.0);
            reel.add(6.0, -d7 / 6.0);
        }
        reel.finish();
        return reel;
    }

    public static Plan plan(int[] nArray, int[] nArray2, long l) {
        return new Plan((int[])nArray.clone(), (int[])nArray2.clone(), l);
    }

    private static final class Reel {
        final double[] t0 = new double[8];
        final double[] turn0 = new double[8];
        final double[] v0 = new double[8];
        final double[] acc = new double[8];
        int count;
        double land;
        double finalTurn;
        private double t;
        private double turn;
        private double v;

        Reel(double d, double d2) {
            this.t = d;
            this.turn = d2;
            this.t0[0] = d;
            this.turn0[0] = d2;
        }

        void add(double d, double d2) {
            this.t0[this.count] = this.t;
            this.turn0[this.count] = this.turn;
            this.v0[this.count] = this.v;
            this.acc[this.count] = d2;
            ++this.count;
            this.turn += this.v * d + 0.5 * d2 * d * d;
            this.v += d2 * d;
            this.t += d;
        }

        void finish() {
            this.land = this.t;
            this.finalTurn = Math.rint(this.turn);
        }

        double turnAt(double d) {
            if (d <= this.t0[0]) {
                return this.turn0[0];
            }
            if (d >= this.land) {
                return this.finalTurn + SlotMachineSpin.recoil(d - this.land);
            }
            int n = this.count - 1;
            while (d < this.t0[n]) {
                --n;
            }
            double d2 = d - this.t0[n];
            return this.turn0[n] + this.v0[n] * d2 + 0.5 * this.acc[n] * d2 * d2;
        }

        double speedAt(double d) {
            if (!(d <= this.t0[0]) && !(d >= this.land)) {
                int n = this.count - 1;
                while (d < this.t0[n]) {
                    --n;
                }
                return this.v0[n] + this.acc[n] * (d - this.t0[n]);
            }
            return 0.0;
        }
    }

    public static final class Plan {
        private final int[] from;
        private final int[] stops;
        private final long seed;
        private final boolean tease;
        private final Reel[] reels = new Reel[3];
        private final SlotMachineGame.Result result;
        private final List<Cue> cues;
        private final int resultTick;
        private final int endTick;

        private Plan(int[] nArray, int[] nArray2, long l) {
            this.from = nArray;
            this.stops = nArray2;
            this.seed = l;
            this.result = SlotMachineGame.evaluate(nArray2);
            this.tease = SlotMachineGame.symbol(0, nArray2[0]) == SlotMachineGame.symbol(1, nArray2[1]);
            Rng rng = new Rng(l);
            double d = -1.0E9;
            for (int i = 0; i < 3; ++i) {
                int n = CRUISE[i] + rng.below(5);
                boolean bl = this.tease && i == 2;
                Reel reel = SlotMachineSpin.build(i, Math.floorMod(nArray[i], 8), Math.floorMod(nArray2[i], 8), n, bl);
                while (reel.land < d + 8.0) {
                    reel = SlotMachineSpin.build(i, Math.floorMod(nArray[i], 8), Math.floorMod(nArray2[i], 8), ++n, bl);
                }
                this.reels[i] = reel;
                d = reel.land;
            }
            this.resultTick = (int)Math.ceil(this.reels[2].land) + 10;
            this.endTick = this.resultTick + CELEBRATION[this.result.kind().ordinal()];
            this.cues = Collections.unmodifiableList(this.makeCues());
        }

        private List<Cue> makeCues() {
            ArrayList<Cue> arrayList = new ArrayList<Cue>();
            arrayList.add(new Cue(0, Sound.LEVER, -1, 1.0f, 1.0f));
            int n = (int)Math.floor(this.reels[2].land) - 12;
            int n2 = START[0] + 2;
            int n3 = 0;
            while (n2 < n) {
                arrayList.add(new Cue(n2, Sound.WHIRR, -1, 0.5f, n3 % 2 == 0 ? 1.0f : 0.96f));
                n2 += 7;
                ++n3;
            }
            for (n3 = 0; n3 < 3; ++n3) {
                Reel reel = this.reels[n3];
                boolean bl = this.tease && n3 == 2;
                for (int i = 2; i < reel.count; ++i) {
                    double d = (i + 1 < reel.count ? reel.t0[i + 1] : reel.land) - reel.t0[i];
                    double d2 = reel.turn0[i] + reel.v0[i] * d + 0.5 * reel.acc[i] * d * d;
                    long l = (long)Math.floor(reel.turn0[i]) + 1L;
                    while ((double)l < d2 - 1.0E-6) {
                        if (bl || !((double)l <= reel.finalTurn - 6.0)) {
                            double d3 = Math.abs(reel.acc[i]) < 1.0E-12 ? ((double)l - reel.turn0[i]) / reel.v0[i] : (-reel.v0[i] + Math.sqrt(Math.max(0.0, reel.v0[i] * reel.v0[i] + 2.0 * reel.acc[i] * ((double)l - reel.turn0[i])))) / reel.acc[i];
                            arrayList.add(new Cue((int)Math.round(reel.t0[i] + d3), Sound.CLICK, n3, 0.42f, 0.88f + 0.1f * (float)n3 + 0.03f * (float)(l % 3L)));
                        }
                        ++l;
                    }
                }
                arrayList.add(new Cue((int)Math.ceil(reel.land), Sound.STOP, n3, 0.9f, 0.94f + 0.05f * (float)n3));
            }
            arrayList.sort((cue, cue2) -> cue.tick() != cue2.tick() ? Integer.compare(cue.tick(), cue2.tick()) : (cue.sound() != cue2.sound() ? cue.sound().compareTo(cue2.sound()) : Integer.compare(cue.reel(), cue2.reel())));
            return arrayList;
        }

        public int[] from() {
            return (int[])this.from.clone();
        }

        public int[] stops() {
            return (int[])this.stops.clone();
        }

        public long seed() {
            return this.seed;
        }

        public SlotMachineGame.Result result() {
            return this.result;
        }

        public boolean tease() {
            return this.tease;
        }

        public List<Cue> cues() {
            return this.cues;
        }

        public double turn(int n, double d) {
            return this.reels[n].turnAt(d);
        }

        public double speed(int n, double d) {
            return this.reels[n].speedAt(d);
        }

        public int landTick(int n) {
            return (int)Math.ceil(this.reels[n].land);
        }

        public boolean landed(int n, double d) {
            return d >= this.reels[n].land;
        }

        public int resultTick() {
            return this.resultTick;
        }

        public int endTick() {
            return this.endTick;
        }

        public int readyTick() {
            return this.resultTick + READY[this.result.kind().ordinal()];
        }

        public int celebration() {
            return CELEBRATION[this.result.kind().ordinal()];
        }

        public double lever(double d) {
            if (d <= 0.0) {
                return -14.0;
            }
            if (d < 8.0) {
                double d2 = d / 8.0;
                return -14.0 + 109.0 * d2 * d2 * (3.0 - 2.0 * d2);
            }
            if (d < 10.0) {
                return 95.0;
            }
            double d3 = (d - 8.0 - 2.0) / 16.0;
            if (d3 >= 1.0) {
                return -14.0;
            }
            double d4 = 1.0 - (1.0 - d3) * (1.0 - d3) * (1.0 - d3);
            return 95.0 + -109.0 * d4;
        }

        private boolean spinning(double d) {
            return d >= 0.0 && d < this.reels[2].land;
        }

        private double sinceResult(double d) {
            return d - (double)this.resultTick;
        }

        private boolean celebrating(double d) {
            double d2 = this.sinceResult(d);
            return d2 >= 0.0 && d2 < (double)CELEBRATION[this.result.kind().ordinal()] && this.result.kind().ordinal() >= SlotMachineGame.Kind.WIN.ordinal();
        }

        public double lamp(double d) {
            if (this.spinning(d)) {
                return (int)Math.floor(d / 4.0) % 2 == 0 ? 1.0 : 0.4;
            }
            if (this.celebrating(d)) {
                int n = this.result.kind() == SlotMachineGame.Kind.JACKPOT ? 2 : 4;
                return (int)Math.floor(this.sinceResult(d) / (double)n) % 2 == 0 ? 1.0 : 0.3;
            }
            return 1.0;
        }

        public double bulb(int n, double d) {
            if (this.spinning(d)) {
                return ((int)Math.floor(d / 2.0) + n) % 3 == 0 ? 1.0 : 0.22;
            }
            if (this.celebrating(d)) {
                int n2 = (int)Math.floor(this.sinceResult(d) / (this.result.kind() == SlotMachineGame.Kind.JACKPOT ? 3.0 : 4.0));
                if (this.result.kind() == SlotMachineGame.Kind.JACKPOT) {
                    return (n2 + n) % 2 == 0 ? 1.0 : 0.22;
                }
                return n2 % 2 == 0 ? 1.0 : 0.22;
            }
            return 1.0;
        }

        public double flash(double d) {
            if (!this.celebrating(d)) {
                return 1.0;
            }
            return (int)Math.floor(this.sinceResult(d) / 4.0) % 2 == 0 ? 1.0 : 0.62;
        }
    }

    public static enum Sound {
        LEVER,
        WHIRR,
        CLICK,
        STOP;

    }

    private static final class Rng {
        private long state;

        Rng(long l) {
            this.state = l;
        }

        long nextLong() {
            long l = this.state += -7046029254386353131L;
            l = (l ^ l >>> 30) * -4658895280553007687L;
            l = (l ^ l >>> 27) * -7723592293110705685L;
            return l ^ l >>> 31;
        }

        double next() {
            return (float)(this.nextLong() >>> 11) * 1.110223E-16f;
        }

        int below(int n) {
            return (int)(this.next() * (double)n);
        }
    }

    public record Cue(int tick, Sound sound, int reel, float volume, float pitch) {
    }
}

