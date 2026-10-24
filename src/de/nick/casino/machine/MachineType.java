/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.key.Key
 */
package de.nick.casino.machine;

import net.kyori.adventure.key.Key;

public enum MachineType {
    ROULETTE("roulette", "roulettecasino:roulette_table", "block.roulettecasino.roulette_table", "Casino-Roulette"),
    SLOT_MACHINE("slot_machine", "roulettecasino:slot_machine", "block.roulettecasino.slot_machine", "Spielautomat");

    public final String id;
    public final Key itemModel;
    public final String translationKey;
    public final String germanName;

    private MachineType(String string2, String string3, String string4, String string5) {
        this.id = string2;
        this.itemModel = Key.key((String)string3);
        this.translationKey = string4;
        this.germanName = string5;
    }

    public static MachineType byId(String string) {
        for (MachineType machineType : MachineType.values()) {
            if (!machineType.id.equalsIgnoreCase(string)) continue;
            return machineType;
        }
        return null;
    }
}

