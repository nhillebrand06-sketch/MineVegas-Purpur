/*
 * Decompiled with CFR 0.152.
 */
package de.nick.casino.game;

public final class CasinoIdleLights {
    public static final int SPAN = 120;
    public static final int PATTERNS = 4;
    public static final int CROSSFADE = 14;
    public static final int RETURN = 14;

    private CasinoIdleLights() {
    }

    public static double clock(double d, int n, int n2, int n3) {
        return d + (double)Math.floorMod((long)((long)n * 31L + (long)n2 * 17L + (long)n3 * 47L), (int)480);
    }

    private static double smooth(double d) {
        d = Math.clamp((double)d, (double)0.0, (double)1.0);
        return d * d * (3.0 - 2.0 * d);
    }

    private static double wave(double d) {
        return 0.5 + 0.5 * StrictMath.cos(Math.PI * 2 * d);
    }

    private static double pattern(int n, double d, double d2) {
        double d3 = switch (n) {
            case 0 -> Math.pow(CasinoIdleLights.wave(d - d2 / 64.0), 6.0);
            case 1 -> Math.pow(CasinoIdleLights.wave(d * 2.0 + d2 / 80.0), 4.0);
            case 2 -> CasinoIdleLights.wave(d2 / 48.0) * (0.4 + 0.6 * CasinoIdleLights.wave(d * 3.0));
            default -> Math.pow(CasinoIdleLights.wave(d * 5.0 - d2 / 40.0), 3.0);
        };
        return 0.25 + 0.75 * d3;
    }

    public static double bulb(int n, int n2, double d) {
        if (n2 <= 0) {
            throw new IllegalArgumentException("No bulbs");
        }
        double d2 = d - Math.floor(d / 480.0) * 480.0;
        int n3 = (int)(d2 / 120.0);
        double d3 = d2 - (double)(n3 * 120);
        double d4 = (double)Math.floorMod(n, n2) / (double)n2;
        double d5 = CasinoIdleLights.pattern(n3, d4, d);
        double d6 = CasinoIdleLights.pattern(Math.floorMod(n3 - 1, 4), d4, d);
        double d7 = CasinoIdleLights.smooth(d3 / 14.0);
        return d6 * (1.0 - d7) + d5 * d7;
    }

    public static double beacon(double d) {
        return 0.62 + 0.38 * CasinoIdleLights.wave(d / 64.0);
    }

    public static double afterGame(double d, double d2, double d3) {
        double d4 = CasinoIdleLights.smooth(d3 / 14.0);
        return d * (1.0 - d4) + d2 * d4;
    }
}

