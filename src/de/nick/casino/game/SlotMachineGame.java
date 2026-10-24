/*
 * Decompiled with CFR 0.152.
 */
package de.nick.casino.game;

public final class SlotMachineGame {
    public static final int REELS = 3;
    public static final int FACES = 8;
    public static final int SEVEN = 0;
    public static final int DIAMOND = 1;
    public static final int BAR = 2;
    public static final int BELL = 3;
    public static final int LEMON = 4;
    public static final int CHERRY = 5;
    public static final int SYMBOLS = 6;
    public static final int ANY = -1;
    public static final String[] NAMES = new String[]{"seven", "diamond", "bar", "bell", "lemon", "cherry"};
    private static final int[][] STRIPS = new int[][]{{0, 5, 3, 4, 2, 5, 1, 4}, {0, 4, 1, 5, 3, 4, 2, 5}, {0, 5, 2, 4, 1, 5, 3, 4}};
    public static final Row[] PAYTABLE = new Row[]{new Row(0, 0, 0, 100), new Row(1, 1, 1, 30), new Row(2, 2, 2, 15), new Row(3, 3, 3, 10), new Row(4, 4, 4, 6), new Row(5, 5, 5, 4), new Row(5, 5, -1, 3), new Row(5, -1, -1, 2)};

    private SlotMachineGame() {
    }

    public static int symbol(int n, int n2) {
        return STRIPS[n][Math.floorMod(n2, 8)];
    }

    public static int[] symbols(int[] nArray) {
        return new int[]{SlotMachineGame.symbol(0, nArray[0]), SlotMachineGame.symbol(1, nArray[1]), SlotMachineGame.symbol(2, nArray[2])};
    }

    private static boolean fits(int n, int n2) {
        return n == -1 || n == n2;
    }

    public static Result evaluate(int n, int n2, int n3) {
        for (int i = 0; i < PAYTABLE.length; ++i) {
            Row row = PAYTABLE[i];
            if (!SlotMachineGame.fits(row.a, n) || !SlotMachineGame.fits(row.b, n2) || !SlotMachineGame.fits(row.c, n3)) continue;
            return new Result(row.multiplier, i, SlotMachineGame.kindOf(row.multiplier));
        }
        return new Result(0, -1, Kind.NONE);
    }

    public static Result evaluate(int[] nArray) {
        int[] nArray2 = SlotMachineGame.symbols(nArray);
        return SlotMachineGame.evaluate(nArray2[0], nArray2[1], nArray2[2]);
    }

    public static Kind kindOf(int n) {
        if (n >= 100) {
            return Kind.JACKPOT;
        }
        if (n >= 15) {
            return Kind.BIG;
        }
        if (n >= 2) {
            return Kind.WIN;
        }
        return n == 1 ? Kind.PUSH : Kind.NONE;
    }

    public static double expectedReturn() {
        long l = 0L;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                int n = 0;
                while (n < 8) {
                    l += (long)SlotMachineGame.evaluate((int[])new int[]{i, j, n++}).multiplier;
                }
            }
        }
        return (double)l / 512.0;
    }

    public static double hitFrequency() {
        int n = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                for (int k = 0; k < 8; ++k) {
                    if (SlotMachineGame.evaluate((int[])new int[]{i, j, k}).multiplier <= 0) continue;
                    ++n;
                }
            }
        }
        return (double)n / 512.0;
    }

    public record Row(int a, int b, int c, int multiplier) {
    }

    public record Result(int multiplier, int row, Kind kind) {
    }

    public static enum Kind {
        NONE,
        PUSH,
        WIN,
        BIG,
        JACKPOT;

    }
}

