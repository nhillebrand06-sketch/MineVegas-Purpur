/*
 * Decompiled with CFR 0.152.
 */
package de.nick.casino.util;

import de.nick.casino.util.MiniJson;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GuiMeta {
    public final Map<String, String> glyphChar = new HashMap<String, String>();
    public final Map<String, Integer> glyphAdvance = new HashMap<String, Integer>();
    public final Map<Integer, String> neg = new HashMap<Integer, String>();
    public final Map<Integer, String> pos = new HashMap<Integer, String>();
    public final Map<Integer, String> textFonts = new HashMap<Integer, String>();
    public final Map<Character, Integer> advances = new HashMap<Character, Integer>();
    public final Map<String, String> icons = new HashMap<String, String>();
    public final Map<String, Object> layouts;

    public GuiMeta(InputStream inputStream) throws Exception {
        Map map = (Map)MiniJson.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        Map map2 = (Map)map.get("glyphs");
        for (Map.Entry entry : map2.entrySet()) {
            Map map3 = (Map)entry.getValue();
            this.glyphChar.put((String)entry.getKey(), (String)map3.get("char"));
            this.glyphAdvance.put((String)entry.getKey(), ((Number)map3.get("advance")).intValue());
        }
        for (Map.Entry entry : ((Map)map.get("neg")).entrySet()) {
            this.neg.put(Integer.parseInt((String)entry.getKey()), (String)entry.getValue());
        }
        for (Map.Entry entry : ((Map)map.get("pos")).entrySet()) {
            this.pos.put(Integer.parseInt((String)entry.getKey()), (String)entry.getValue());
        }
        for (Map.Entry entry : ((Map)map.get("textFonts")).entrySet()) {
            this.textFonts.put(Integer.parseInt((String)entry.getKey()), (String)entry.getValue());
        }
        for (Map.Entry entry : ((Map)map.get("advances")).entrySet()) {
            if (((String)entry.getKey()).length() != 1) continue;
            this.advances.put(Character.valueOf(((String)entry.getKey()).charAt(0)), ((Number)entry.getValue()).intValue());
        }
        for (Map.Entry entry : ((Map)map.get("icons")).entrySet()) {
            this.icons.put((String)entry.getKey(), (String)entry.getValue());
        }
        this.layouts = (Map)map.get("layouts");
    }

    public int[] intList(String string, String string2) {
        List list = (List)((Map)this.layouts.get(string)).get(string2);
        int[] nArray = new int[list.size()];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = ((Number)list.get(i)).intValue();
        }
        return nArray;
    }

    public int[][] intMatrix(String string, String string2) {
        List list = (List)((Map)this.layouts.get(string)).get(string2);
        int[][] nArrayArray = new int[list.size()][];
        for (int i = 0; i < nArrayArray.length; ++i) {
            List list2 = (List)list.get(i);
            nArrayArray[i] = new int[list2.size()];
            for (int j = 0; j < list2.size(); ++j) {
                nArrayArray[i][j] = ((Number)list2.get(j)).intValue();
            }
        }
        return nArrayArray;
    }

    public int intValue(String string, String string2) {
        return ((Number)((Map)this.layouts.get(string)).get(string2)).intValue();
    }

    public int textWidth(String string, boolean bl) {
        int n = 0;
        for (char c : string.toCharArray()) {
            n += this.advances.getOrDefault(Character.valueOf(c), 6) + (bl && c != ' ' ? 1 : 0);
        }
        return n;
    }

    public String shift(int n3) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<Integer, String> map = n3 < 0 ? this.neg : this.pos;
        int n4 = Math.abs(n3);
        ArrayList<Integer> arrayList = new ArrayList<Integer>(map.keySet());
        arrayList.sort((n, n2) -> Integer.compare(n2, n));
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            int n5 = (Integer)iterator.next();
            while (n4 >= n5) {
                stringBuilder.append(map.get(n5));
                n4 -= n5;
            }
        }
        return stringBuilder.toString();
    }
}

