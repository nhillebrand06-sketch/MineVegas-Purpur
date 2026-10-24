/*
 * Decompiled with CFR 0.152.
 */
package de.nick.casino.game;

public final class RouletteSpin {
    public static final int POCKETS = 37;
    public static final double ANGLE = 0.16981581911296179;
    public static final int DURATION = 160;

    public static Plan plan(int n, int n2) {
        if (n2 >= 0 && n2 < 37) {
            return new Plan(n, 37 + Math.floorMod(n2 * 25 % 37 - n, 37));
        }
        throw new IllegalArgumentException("Roulette roll");
    }

    public static int pocket(int n) {
        return Math.floorMod(3 * n, 37);
    }

    public static int color(int n) {
        return n == 0 ? 2 : ((n & 1) == 0 ? 1 : 0);
    }

    public static double ballRadius(double d) {
        return 8.0 + 2.1 * d;
    }

    public static double ballHeight(double d, double d2) {
        double d3 = 3.0 * d;
        double d4 = Math.pow(Math.sin(Math.PI * d3), 2.0);
        return 12.8 + 0.75 * d2 + 0.26 * (1.0 - d2) * d4;
    }

    public static double smooth(double d) {
        double d2 = Math.clamp((double)d, (double)0.0, (double)1.0);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    private RouletteSpin() {
    }

    public record Plan(int initial, int steps) {
        public Plan {
            if (n < 0 || n >= 37 || n2 < 1 || n2 > 73) {
                throw new IllegalArgumentException("Roulette plan");
            }
        }

        public int finalFrame() {
            return Math.floorMod(this.initial + this.steps, 37);
        }

        public double phase(double d) {
            double d2 = Math.clamp((double)(d / 160.0), (double)0.0, (double)1.0);
            return (double)this.initial + (double)this.steps * (1.0 - (1.0 - d2) * (1.0 - d2) * (1.0 - d2));
        }

        public double speed(double d) {
            if (!(d < 0.0) && !(d >= 160.0)) {
                double d2 = d / 160.0;
                return (double)this.steps * 3.0 * (1.0 - d2) * (1.0 - d2) / 160.0;
            }
            return 0.0;
        }

        public double track(double d) {
            return 1.0 - RouletteSpin.smooth((d / 160.0 - 0.58) / 0.28);
        }

        public double contactTick() {
            return 160.0 * (1.0 - Math.cbrt(0.5 / (3.0 * (double)this.steps)));
        }

        public double settleDuration() {
            return 1.2 / (3.0 * this.speed(this.contactTick()));
        }

        public double ballPhase(double d) {
            double d2 = this.contactTick();
            if (d <= d2) {
                return 2.0 * this.phase(d);
            }
            double d3 = Math.clamp((double)((d - d2) / this.settleDuration()), (double)0.0, (double)1.0);
            double d4 = -2.0 * d3 * d3 * d3 + 3.0 * d3 * d3 + 2.4 * (d3 * d3 * d3 - 2.0 * d3 * d3 + d3);
            double d5 = 0.025 * Math.sin(Math.PI * 4 * d3) * d3 * (1.0 - d3) * (1.0 - d3);
            double d6 = 3.0 * (double)(this.initial + this.steps) - 0.5 + 0.5 * d4 + d5;
            return d6 - this.phase(d);
        }

        public double ballHeight(double d) {
            double d2 = this.contactTick();
            double d3 = this.track(d);
            if (d <= d2) {
                return RouletteSpin.ballHeight(this.phase(d), d3);
            }
            double d4 = Math.clamp((double)((d - d2) / this.settleDuration()), (double)0.0, (double)1.0);
            double d5 = Math.exp(-3.0 * d4) * Math.pow(Math.cos(Math.PI * 2 * d4), 2.0) * (1.0 - RouletteSpin.smooth(d4));
            return 12.8 + 0.75 * d3 + 0.26 * (1.0 - d3) * d5;
        }

        public int resultTick() {
            return 160;
        }
    }
}

