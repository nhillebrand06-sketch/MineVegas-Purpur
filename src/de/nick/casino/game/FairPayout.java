/*
 * Decompiled with CFR 0.152.
 */
package de.nick.casino.game;

public final class FairPayout {
    private FairPayout() {
    }

    public static int share(long l, int n, int n2) {
        if (l > 0L && n > 0) {
            long l2 = l / (long)n;
            long l3 = l % (long)n;
            return (int)(l2 + (long)((long)Math.floorMod(n2, n) < l3 ? 1 : 0));
        }
        return 0;
    }

    public static int roll(long l, int n, int n2) {
        long l2 = l + -7046029254386353131L * ((long)n + 1L);
        l2 = (l2 ^ l2 >>> 30) * -4658895280553007687L;
        l2 = (l2 ^ l2 >>> 27) * -7723592293110705685L;
        l2 ^= l2 >>> 31;
        return (int)Math.floorMod(l2, (long)n2);
    }

    public static double expected(long l, int n) {
        return n <= 0 ? 0.0 : (double)l / (double)n;
    }
}

