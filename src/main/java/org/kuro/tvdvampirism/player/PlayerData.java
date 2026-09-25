package org.kuro.tvdvampirism.player;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Optional;

public class PlayerData implements INBTSerializable<CompoundTag> {
    private net.minecraft.world.item.ItemStack embeddedDagger = net.minecraft.world.item.ItemStack.EMPTY;
    public boolean isDaggered() { return !embeddedDagger.isEmpty(); }
    public net.minecraft.world.item.ItemStack getEmbeddedDagger() { return embeddedDagger; }
    public void setEmbeddedDagger(net.minecraft.world.item.ItemStack dagger) {
        embeddedDagger = dagger.copy();
        if (embeddedDagger.isEmpty()) daggerRemovalUnlockTick = 0;
    }
    // Ignore the click that inserted the dagger so it cannot remove it immediately.
    private long daggerRemovalUnlockTick;
    public void delayDaggerRemovalUntil(long gameTick) { daggerRemovalUnlockTick = Math.max(0, gameTick); }
    public boolean canRemoveDagger(long gameTick) { return gameTick >= daggerRemovalUnlockTick; }
    // The target lock is temporary; vanilla item use tracks the charge time.
    private java.util.UUID daggerChargeTarget;
    public java.util.UUID getDaggerChargeTarget() { return daggerChargeTarget; }
    public void setDaggerChargeTarget(java.util.UUID target) { daggerChargeTarget = target; }

    private Species legacySpeciesMigration;

    // Save XP and derive Potency and Mastery ranks instead of saving them.
    private int potencyXp = 0;
    private int masteryXp = 0;


    private boolean wolfBiteActive = false;
    private int wolfBiteTicks = 0;
    private boolean wolfBiteTerminalTriggered = false;
    private int wolfBiteDurationTicks;

    public int getWolfBiteDurationTicks() { return wolfBiteDurationTicks; }
    public void setWolfBiteDurationTicks(int ticks) { wolfBiteDurationTicks = Math.max(1, ticks); }
    public void finishWolfBite() {
        wolfBiteActive = false;
        wolfBiteTerminalTriggered = true;
    }

    // -1 means awake, 0 means ready to wake, and positive values count down DBNO.
    private int originalDbnoTicks = -1;
    private boolean vampirismCureActive;
    private boolean vampirismCureTerminalPending;

    public int getOriginalDbnoTicks() {
        return originalDbnoTicks;
    }

    public void setOriginalDbnoTicks(int ticks) {
        originalDbnoTicks = Math.max(-1, ticks);
    }

    public boolean isVampirismCureActive() {
        return vampirismCureActive;
    }

    public void setVampirismCureActive(boolean active) {
        vampirismCureActive = active;
    }

    public boolean isVampirismCureTerminalPending() {
        return vampirismCureTerminalPending;
    }

    public void setVampirismCureTerminalPending(boolean pending) {
        vampirismCureTerminalPending = pending;
    }



    public Optional<Species> getLegacySpeciesMigration() {
        return Optional.ofNullable(legacySpeciesMigration);
    }

    public void clearLegacySpeciesMigration() {
        legacySpeciesMigration = null;
    }



    public int getPotencyXp() {
        return potencyXp;
    }

    public void setPotencyXp(int value) {
        this.potencyXp = Math.max(0, value);
    }

    public void addPotencyXp(int amount) {
        setPotencyXp(this.potencyXp + amount);
    }



    public int getMasteryXp() {
        return masteryXp;
    }

    public void setMasteryXp(int value) {
        this.masteryXp = Math.max(0, value);
    }

    public void addMasteryXp(int amount) {
        setMasteryXp(this.masteryXp + amount);
    }



    public boolean hasWolfBite() {
        return wolfBiteActive;
    }

    public void startWolfBite() {
        wolfBiteDurationTicks = 0;
        wolfBiteActive = true;
        wolfBiteTicks = 0;
        wolfBiteTerminalTriggered = false;
    }

    public void cureWolfBite() {
        wolfBiteDurationTicks = 0;
        wolfBiteActive = false;
        wolfBiteTicks = 0;
        wolfBiteTerminalTriggered = false;
    }

    public int getWolfBiteTicks() {
        return wolfBiteTicks;
    }

    public void setWolfBiteTicks(int value) {
        wolfBiteTicks = Math.max(0, value);
    }

    public void incrementWolfBiteTicks() {
        wolfBiteTicks++;
    }

    public boolean isWolfBiteTerminalTriggered() {
        return wolfBiteTerminalTriggered;
    }

    public void setWolfBiteTerminalTriggered(boolean value) {
        wolfBiteTerminalTriggered = value;
    }



    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {

        CompoundTag tag = new CompoundTag();
        tag.putInt("OriginalDbnoTicks", originalDbnoTicks);
        tag.putBoolean("VampirismCureActive", vampirismCureActive);
        tag.putBoolean("VampirismCureTerminalPending", vampirismCureTerminalPending);
        tag.put("EmbeddedDagger", embeddedDagger.saveOptional(provider));
        tag.putInt("WolfBiteDurationTicks", wolfBiteDurationTicks);

        if (legacySpeciesMigration != null) {
            tag.putString(
                    "LegacySpeciesMigration",
                    legacySpeciesMigration.name()
            );
        }

        tag.putInt(
                "PotencyXp",
                potencyXp
        );

        tag.putInt(
                "MasteryXp",
                masteryXp
        );

        tag.putBoolean(
                "WolfBiteActive",
                wolfBiteActive
        );

        tag.putInt(
                "WolfBiteTicks",
                wolfBiteTicks
        );

        tag.putBoolean(
                "WolfBiteTerminalTriggered",
                wolfBiteTerminalTriggered
        );

        return tag;
    }

    @Override
    public void deserializeNBT(
            HolderLookup.Provider provider,
            CompoundTag tag
    ) {

        legacySpeciesMigration = readLegacySpecies(tag);
        wolfBiteDurationTicks = Math.max(0, tag.getInt("WolfBiteDurationTicks"));
        originalDbnoTicks = tag.contains("OriginalDbnoTicks")
                ? Math.max(-1, tag.getInt("OriginalDbnoTicks")) : -1;
        vampirismCureActive = tag.getBoolean("VampirismCureActive");
        vampirismCureTerminalPending = tag.getBoolean("VampirismCureTerminalPending");
        embeddedDagger = net.minecraft.world.item.ItemStack.parseOptional(provider, tag.getCompound("EmbeddedDagger"));


        potencyXp = Math.max(
                0,
                tag.getInt("PotencyXp")
        );


        if (tag.contains("MasteryXp")) {

            masteryXp = Math.max(
                    0,
                    tag.getInt("MasteryXp")
            );

        } else {

            masteryXp = 0;
        }


        wolfBiteActive =
                tag.getBoolean("WolfBiteActive");

        wolfBiteTicks = Math.max(
                0,
                tag.getInt("WolfBiteTicks")
        );

        wolfBiteTerminalTriggered =
                tag.getBoolean(
                        "WolfBiteTerminalTriggered"
                );
    }

    private static Species readLegacySpecies(CompoundTag tag) {
        String key;
        if (tag.contains("LegacySpeciesMigration")) {
            key = "LegacySpeciesMigration";
        } else if (tag.contains("SpeciesOverride")) {
            key = "SpeciesOverride";
        } else {
            return null;
        }

        try {
            Species species = Species.valueOf(tag.getString(key));
            return species != Species.NONE && species != Species.NORMAL
                    ? species
                    : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
