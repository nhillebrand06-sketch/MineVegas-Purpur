/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.datacomponent.DataComponentTypes
 *  io.papermc.paper.datacomponent.item.CustomModelData
 *  net.kyori.adventure.key.Key
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.World
 *  org.bukkit.entity.Display$Billboard
 *  org.bukkit.entity.Display$Brightness
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.ItemDisplay
 *  org.bukkit.entity.ItemDisplay$ItemDisplayTransform
 *  org.bukkit.entity.TextDisplay
 *  org.bukkit.entity.TextDisplay$TextAlignment
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.util.Transformation
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package de.nick.casino.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import java.util.List;
import net.kyori.adventure.key.Key;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class Displays {
    public static NamespacedKey VISUAL_KEY;

    private Displays() {
    }

    public static void init(Plugin plugin) {
        VISUAL_KEY = new NamespacedKey(plugin, "visual");
    }

    public static ItemStack modelItem(Key key) {
        ItemStack itemStack = new ItemStack(Material.PAPER);
        itemStack.setData(DataComponentTypes.ITEM_MODEL, (Object)key);
        return itemStack;
    }

    public static ItemStack modelItem(Key key, List<Color> list) {
        ItemStack itemStack = Displays.modelItem(key);
        if (list != null && !list.isEmpty()) {
            itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, (Object)((CustomModelData)CustomModelData.customModelData().addColors(list).build()));
        }
        return itemStack;
    }

    public static ItemDisplay item(World world, double d, double d2, double d3, Key key, Quaternionf quaternionf, boolean bl, String string) {
        Location location = new Location(world, d, d2, d3, 0.0f, 0.0f);
        return (ItemDisplay)world.spawn(location, ItemDisplay.class, itemDisplay -> {
            itemDisplay.setPersistent(false);
            itemDisplay.setItemStack(Displays.modelItem(key));
            itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            itemDisplay.setTransformation(new Transformation(new Vector3f(), new Quaternionf((Quaternionfc)quaternionf), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf()));
            itemDisplay.setInterpolationDuration(1);
            itemDisplay.setTeleportDuration(1);
            itemDisplay.setViewRange(1.5f);
            if (bl) {
                itemDisplay.setBrightness(new Display.Brightness(15, 15));
            }
            itemDisplay.getPersistentDataContainer().set(VISUAL_KEY, PersistentDataType.STRING, (Object)string);
        });
    }

    public static TextDisplay text(World world, double d, double d2, double d3, String string) {
        Location location = new Location(world, d, d2, d3, 0.0f, 0.0f);
        return (TextDisplay)world.spawn(location, TextDisplay.class, textDisplay -> {
            textDisplay.setPersistent(false);
            textDisplay.setBillboard(Display.Billboard.CENTER);
            textDisplay.setBackgroundColor(Color.fromARGB((int)-266188488));
            textDisplay.setShadowed(true);
            textDisplay.setLineWidth(400);
            textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
            textDisplay.setBrightness(new Display.Brightness(15, 15));
            textDisplay.setViewRange(0.15625f);
            textDisplay.setTransformation(new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(0.44f, 0.44f, 0.44f), new Quaternionf()));
            textDisplay.setInterpolationDuration(3);
            textDisplay.getPersistentDataContainer().set(VISUAL_KEY, PersistentDataType.STRING, (Object)string);
        });
    }

    public static void transform(ItemDisplay itemDisplay, Vector3f vector3f, Quaternionf quaternionf) {
        itemDisplay.setInterpolationDelay(0);
        itemDisplay.setTransformation(new Transformation(vector3f, quaternionf, new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf()));
    }

    public static boolean isVisual(Entity entity) {
        return entity.getPersistentDataContainer().has(VISUAL_KEY, PersistentDataType.STRING);
    }

    public static Color gray(double d) {
        int n = (int)Math.round(255.0 * Math.max(0.0, Math.min(1.0, d)));
        return Color.fromRGB((int)n, (int)n, (int)n);
    }
}

