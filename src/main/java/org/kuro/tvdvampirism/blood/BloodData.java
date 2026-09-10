package org.kuro.tvdvampirism.blood;

import de.teamlapen.lib.lib.storage.ISyncableSaveData;
import de.teamlapen.vampirism.api.entity.player.vampire.IBloodStats;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModAttributes;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModTags;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

public final class BloodData implements IBloodStats, ISyncableSaveData {

    public static final String NBT_KEY = "blood_data";
    private static final float MAX_EXHAUSTION = 40.0F;

    private final Player player;

    private int bloodLevel;
    private int maxBlood;
    private int previousBloodLevel;
    private int bloodTimer;

    private float saturationLevel = 5.0F;
    private float exhaustionLevel;

    private boolean changed;
    private double normalFeedingCredit;
    private int heartRemainder;

    public int normalFeedingGain(int amount, double multiplier) {
        normalFeedingCredit += multiplier;
        int pulses = (int) Math.floor(normalFeedingCredit + 1.0E-9);
        normalFeedingCredit -= pulses;
        return amount * pulses;
    }

    public int quarterHeartGain(int amount) {
        int total = Math.max(0, amount) + heartRemainder;
        heartRemainder = total % 4;
        return total / 4;
    }

    public BloodData(Player player, int initialCapacity) {
        this.player = player;
        this.maxBlood = Math.max(1, initialCapacity);
        this.bloodLevel = maxBlood;
        this.previousBloodLevel = bloodLevel;
    }

    @Override
    public int getBloodLevel() {
        return bloodLevel;
    }

    @Override
    public int getMaxBlood() {
        return maxBlood;
    }

    @Override
    public int getPrevBloodLevel() {
        return previousBloodLevel;
    }

    @Override
    public boolean needsBlood() {
        return bloodLevel < maxBlood;
    }

    public float getSaturationLevel() {
        return saturationLevel;
    }

    public float getExhaustionLevel() {
        return exhaustionLevel;
    }

    public void setBloodLevel(int amount) {
        int newLevel = Math.max(0, Math.min(amount, maxBlood));
        if (newLevel != bloodLevel) {
            bloodLevel = newLevel;
            saturationLevel = Math.min(saturationLevel, bloodLevel);
            changed = true;
        }
    }

    public void setMaxBlood(int amount) {
        int newMax = Math.max(1, amount);
        if (newMax == maxBlood) {
            return;
        }

        maxBlood = newMax;
        bloodLevel = Math.min(bloodLevel, maxBlood);
        previousBloodLevel = Math.min(previousBloodLevel, maxBlood);
        saturationLevel = Math.min(saturationLevel, bloodLevel);
        changed = true;
    }

    public int addBlood(int amount, float saturationModifier) {
        int safeAmount = Math.max(0, amount);
        int added = Math.min(safeAmount, maxBlood - bloodLevel);

        if (added > 0) {
            bloodLevel += added;
            saturationLevel = Math.min(
                    saturationLevel + added * Math.max(0.0F, saturationModifier) * 2.0F,
                    bloodLevel
            );
            changed = true;
        }

        return safeAmount - added;
    }

    public boolean removeBlood(int amount, boolean allowPartial) {
        int safeAmount = Math.max(0, amount);

        if (bloodLevel >= safeAmount) {
            bloodLevel -= safeAmount;
            saturationLevel = Math.min(saturationLevel, bloodLevel);
            changed = true;
            return true;
        }

        if (allowPartial) {
            bloodLevel = 0;
            saturationLevel = 0.0F;
            changed = true;
        }

        return false;
    }

    public void addExhaustion(float amount) {
        if (amount <= 0.0F) {
            return;
        }

        float modifier = (float) player.getAttributeValue(ModAttributes.BLOOD_EXHAUSTION);
        exhaustionLevel = Math.min(exhaustionLevel + amount * modifier, MAX_EXHAUSTION);
    }

    public boolean tickServer() {
        previousBloodLevel = bloodLevel;

        FoodData food = player.getFoodData();
        food.setFoodLevel(10);

        addExhaustion(food.getExhaustionLevel());
        food.setExhaustion(0.0F);

        Difficulty difficulty = player.level().getDifficulty();
        float exhaustionGate = player.level()
                .getBiome(player.blockPosition())
                .is(ModTags.Biomes.IS_VAMPIRE_BIOME) ? 6.0F : 4.0F;

        if (exhaustionLevel > exhaustionGate) {
            exhaustionLevel -= exhaustionGate;

            if (saturationLevel > 0.0F) {
                saturationLevel = Math.max(saturationLevel - 1.0F, 0.0F);
                changed = true;
            } else if (difficulty != Difficulty.PEACEFUL
                    || VampirismConfig.BALANCE.vpBloodUsagePeaceful.get()) {
                bloodLevel = Math.max(bloodLevel - 1, 0);
            }
        }

        tickRegeneration(difficulty);

        boolean sync = changed || previousBloodLevel != bloodLevel;
        changed = false;
        return sync;
    }

    private void tickRegeneration(Difficulty difficulty) {
        boolean regen = player.level()
                .getGameRules()
                .getBoolean(GameRules.RULE_NATURAL_REGENERATION);

        if (regen && saturationLevel > 0.0F && player.isHurt() && bloodLevel >= maxBlood) {
            if (++bloodTimer >= 10) {
                float saturation = Math.min(saturationLevel, 6.0F);
                player.heal((saturation / 6.0F) * getHealModifier());
                addExhaustion(saturation);
                bloodTimer = 0;
            }
            return;
        }

        if (regen && bloodLevel > 0 && player.isHurt()) {
            ++bloodTimer;

            boolean fastHeal = bloodLevel >= 18 && bloodTimer >= 80;
            boolean slowHeal = bloodTimer >= 300;

            if (fastHeal || slowHeal) {
                player.heal((fastHeal ? 1.0F : 0.5F) * getHealModifier());
                addExhaustion(fastHeal ? 6.0F : 3.0F);
                bloodTimer = 0;
            }
            return;
        }

        if (bloodLevel <= 0) {
            if (++bloodTimer >= 80) {
                if (player.getHealth() > 10.0F
                        || difficulty == Difficulty.HARD
                        || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
                    player.addEffect(new MobEffectInstance(ModEffects.NO_BLOOD, 150));
                }
                bloodTimer = 0;
            }
            return;
        }

        bloodTimer = 0;
    }

    private float getHealModifier() {
        return 1.0F + FactionPlayerHandler.get(player).getCurrentLevelRelative() * 0.5F;
    }

    @Override
    public void deserializeNBT(
            @NotNull HolderLookup.Provider provider,
            @NotNull CompoundTag tag
    ) {
        maxBlood = Math.max(
                1,
                tag.contains("max_blood")
                        ? tag.getInt("max_blood")
                        : maxBlood
        );

        bloodLevel = Math.max(0, Math.min(tag.getInt("blood_level"), maxBlood));
        previousBloodLevel = bloodLevel;
        saturationLevel = Math.max(0.0F, Math.min(tag.getFloat("saturation"), bloodLevel));
        exhaustionLevel = Math.max(
                0.0F,
                Math.min(tag.getFloat("exhaustion"), MAX_EXHAUSTION)
        );
        bloodTimer = Math.max(0, tag.getInt("blood_timer"));
        normalFeedingCredit = Math.max(0, Math.min(1, tag.getDouble("normal_feeding_credit")));
        heartRemainder = Math.floorMod(tag.getInt("heart_remainder"), 4);
        changed = false;
    }

    @Override
    public @NotNull CompoundTag serializeNBT(
            @NotNull HolderLookup.Provider provider
    ) {
        CompoundTag tag = serializeUpdateNBT(provider);
        tag.putFloat("exhaustion", exhaustionLevel);
        tag.putInt("blood_timer", bloodTimer);
        tag.putDouble("normal_feeding_credit", normalFeedingCredit);
        tag.putInt("heart_remainder", heartRemainder);
        return tag;
    }

    @Override
    public void deserializeUpdateNBT(
            HolderLookup.Provider provider,
            @NotNull CompoundTag tag
    ) {
        if (tag.contains("max_blood")) {
            maxBlood = Math.max(1, tag.getInt("max_blood"));
        }

        if (tag.contains("blood_level")) {
            bloodLevel = Math.max(0, Math.min(tag.getInt("blood_level"), maxBlood));
            previousBloodLevel = bloodLevel;
        }

        if (tag.contains("saturation")) {
            saturationLevel = Math.max(0.0F, Math.min(tag.getFloat("saturation"), bloodLevel));
        }

        changed = false;
    }

    @Override
    public @NotNull CompoundTag serializeUpdateNBT(
            HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("blood_level", bloodLevel);
        tag.putInt("max_blood", maxBlood);
        tag.putFloat("saturation", saturationLevel);
        return tag;
    }

    @Override
    public String nbtKey() {
        return NBT_KEY;
    }
}
