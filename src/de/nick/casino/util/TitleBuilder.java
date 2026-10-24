/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.TextComponent$Builder
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 */
package de.nick.casino.util;

import de.nick.casino.util.GuiMeta;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class TitleBuilder {
    public static final Key GUI_FONT = Key.key((String)"casino", (String)"gui");
    private final GuiMeta meta;
    private final TextComponent.Builder out = Component.text();
    private int cursor = 8;

    public TitleBuilder(GuiMeta guiMeta) {
        this.meta = guiMeta;
    }

    private void moveTo(int n) {
        int n2 = n - this.cursor;
        if (n2 != 0) {
            this.out.append(Component.text((String)this.meta.shift(n2)).font(GUI_FONT));
            this.cursor = n;
        }
    }

    public TitleBuilder glyph(String string, int n) {
        this.moveTo(n);
        this.out.append(((TextComponent)Component.text((String)this.meta.glyphChar.get(string)).font(GUI_FONT)).color((TextColor)NamedTextColor.WHITE));
        this.cursor += this.meta.glyphAdvance.get(string).intValue();
        return this;
    }

    public TitleBuilder text(String string, int n, int n2, TextColor textColor, boolean bl) {
        String string2 = this.meta.textFonts.get(n2);
        if (string2 == null) {
            throw new IllegalArgumentException("No text font for y=" + n2);
        }
        this.moveTo(n);
        Component component = ((TextComponent)((TextComponent)((TextComponent)Component.text((String)string).font(Key.key((String)string2))).color(textColor)).decoration(TextDecoration.BOLD, bl)).decoration(TextDecoration.ITALIC, false);
        this.out.append(component);
        this.cursor += this.meta.textWidth(string, bl);
        return this;
    }

    public TitleBuilder centeredText(String string, int n, int n2, TextColor textColor, boolean bl) {
        return this.text(string, n - this.meta.textWidth(string, bl) / 2, n2, textColor, bl);
    }

    public Component build() {
        return this.out.build();
    }
}

