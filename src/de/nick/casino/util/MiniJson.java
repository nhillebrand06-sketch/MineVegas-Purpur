/*
 * Decompiled with CFR 0.152.
 */
package de.nick.casino.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MiniJson {
    private final String s;
    private int i;

    private MiniJson(String string) {
        this.s = string;
    }

    public static Object parse(String string) {
        MiniJson miniJson = new MiniJson(string);
        miniJson.ws();
        Object object = miniJson.value();
        miniJson.ws();
        if (miniJson.i != miniJson.s.length()) {
            throw new IllegalArgumentException("trailing data at " + miniJson.i);
        }
        return object;
    }

    private void ws() {
        while (this.i < this.s.length() && Character.isWhitespace(this.s.charAt(this.i))) {
            ++this.i;
        }
    }

    private Object value() {
        char c = this.s.charAt(this.i);
        switch (c) {
            case '{': {
                return this.object();
            }
            case '[': {
                return this.array();
            }
            case '\"': {
                return this.string();
            }
            case 't': {
                this.i += 4;
                return Boolean.TRUE;
            }
            case 'f': {
                this.i += 5;
                return Boolean.FALSE;
            }
            case 'n': {
                this.i += 4;
                return null;
            }
        }
        return this.number();
    }

    private Map<String, Object> object() {
        char c;
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
        ++this.i;
        this.ws();
        if (this.s.charAt(this.i) == '}') {
            ++this.i;
            return linkedHashMap;
        }
        do {
            this.ws();
            String string = this.string();
            this.ws();
            ++this.i;
            this.ws();
            linkedHashMap.put(string, this.value());
            this.ws();
        } while ((c = this.s.charAt(this.i++)) != '}');
        return linkedHashMap;
    }

    private List<Object> array() {
        char c;
        ArrayList<Object> arrayList = new ArrayList<Object>();
        ++this.i;
        this.ws();
        if (this.s.charAt(this.i) == ']') {
            ++this.i;
            return arrayList;
        }
        do {
            this.ws();
            arrayList.add(this.value());
            this.ws();
        } while ((c = this.s.charAt(this.i++)) != ']');
        return arrayList;
    }

    private String string() {
        StringBuilder stringBuilder = new StringBuilder();
        ++this.i;
        char c;
        while ((c = this.s.charAt(this.i++)) != '\"') {
            if (c == '\\') {
                char c2 = this.s.charAt(this.i++);
                switch (c2) {
                    case 'n': {
                        stringBuilder.append('\n');
                        break;
                    }
                    case 't': {
                        stringBuilder.append('\t');
                        break;
                    }
                    case 'r': {
                        stringBuilder.append('\r');
                        break;
                    }
                    case 'b': {
                        stringBuilder.append('\b');
                        break;
                    }
                    case 'f': {
                        stringBuilder.append('\f');
                        break;
                    }
                    case 'u': {
                        stringBuilder.append((char)Integer.parseInt(this.s.substring(this.i, this.i + 4), 16));
                        this.i += 4;
                        break;
                    }
                    default: {
                        stringBuilder.append(c2);
                        break;
                    }
                }
                continue;
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    private Number number() {
        int n = this.i;
        while (this.i < this.s.length() && "+-0123456789.eE".indexOf(this.s.charAt(this.i)) >= 0) {
            ++this.i;
        }
        String string = this.s.substring(n, this.i);
        if (string.contains(".") || string.contains("e") || string.contains("E")) {
            return Double.parseDouble(string);
        }
        return Long.parseLong(string);
    }
}

