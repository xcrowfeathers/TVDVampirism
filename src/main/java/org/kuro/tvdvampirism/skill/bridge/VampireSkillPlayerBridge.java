package org.kuro.tvdvampirism.skill.bridge;

import de.teamlapen.vampirism.api.entity.player.actions.IActionHandler;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.api.entity.player.vampire.IBloodStats;
import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampireVision;
import de.teamlapen.vampirism.entity.player.actions.ActionHandler;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayerSpecialAttributes;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

public final class VampireSkillPlayerBridge extends VampirePlayer {

    private final CustomFactionPlayer<?> owner;

    public VampireSkillPlayerBridge(CustomFactionPlayer<?> owner) {
        super(owner.asEntity());
        this.owner = owner;
    }

    @Override
    public int getLevel() {
        return owner.getLevel();
    }

    @Override
    public int getMaxLevel() {
        return owner.getMaxLevel();
    }

    @Override
    public @NotNull VampirePlayerSpecialAttributes getSpecialAttributes() {
        return actual().getSpecialAttributes();
    }

    @Override
    public int getBloodLevel() {
        return owner.getBloodData().getBloodLevel();
    }

    @Override
    public float getBloodLevelRelative() {
        int max = owner.getBloodData().getMaxBlood();

        return max <= 0
                ? 0.0F
                : owner.getBloodData().getBloodLevel() / (float) max;
    }

    @NotNull
    @Override
    public IBloodStats getBloodStats() {
        return owner.getBloodData();
    }

    @Override
    public void addExhaustion(float exhaustion) {
        owner.addBloodExhaustion(exhaustion);
    }

    @Override
    public boolean useBlood(int amount, boolean allowPartial) {
        return owner.useBlood(amount, allowPartial);
    }

    @Override
    public boolean wantsBlood() {
        return owner.wantsBlood();
    }

    @Override
    public void drinkBlood(
            int amount,
            float saturationModifier,
            boolean useRemaining,
            IDrinkBloodContext context
    ) {
        var event = de.teamlapen.vampirism.util.VampirismEventFactory
                .fireVampirePlayerDrinkBloodEvent(
                        this,
                        amount,
                        saturationModifier,
                        useRemaining,
                        context
                );

        owner.drinkBlood(
                event.getAmount(),
                event.getSaturation(),
                event.useRemaining(),
                context
        );

        if (!owner.isRemote()) {
            owner.syncBlood(false);
        }
    }

    @NotNull
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ISkillHandler<IVampirePlayer> getSkillHandler() {
        return (ISkillHandler) owner.getSkillHandler();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NotNull IActionHandler<IVampirePlayer> getActionHandler() {
        return (IActionHandler) owner.getActionHandler();
    }

    @Override
    public void activateVision(@Nullable IVampireVision vision) {
        actual().activateVision(vision);
    }

    @Nullable
    @Override
    public IVampireVision getActiveVision() {
        return actual().getActiveVision();
    }

    @Override
    public void unlockVision(@NotNull IVampireVision vision) {
        actual().unlockVision(vision);
    }

    @Override
    public void unUnlockVision(@NotNull IVampireVision vision) {
        actual().unUnlockVision(vision);
    }

    @Override
    public void switchVision() {
        actual().switchVision();
    }

    /*
     * DBNO must always live on Vampirism's real VampirePlayer attachment.
     * The bridge only exposes our custom species to Vampirism.
     */

    @Override
    public boolean isDBNO() {
        return actual().isDBNO();
    }

    @Override
    public int getDbnoTimer() {
        return actual().getDbnoTimer();
    }

    @Override
    public int getDbnoDuration() {
        return Math.max(1, (int) asEntity().getAttributeValue(
                de.teamlapen.vampirism.core.ModAttributes.DBNO_DURATION));
    }

    @Override
    public boolean onDeadlyHit(@NotNull DamageSource source) {
        return actual().onDeadlyHit(source);
    }

    @Override
    public void tryResurrect() {
        actual().tryResurrect();
    }

    @Override
    public void giveUpDBNO() {
        actual().giveUpDBNO();
    }

    @Override
    public float getFeedProgress() {
        return owner.getFeedProgress();
    }

    @Override
    public int removeBlood(float percentage) {
        int removed = (int) Math.ceil(
                getBloodLevel() * percentage
        );

        owner.useBlood(removed, true);
        return removed;
    }

    @Override
    public int onBite(
            de.teamlapen.vampirism.api.entity.vampire.IVampire biter
    ) {
        return removeBlood(
                biter instanceof IVampirePlayer
                        ? 0.2F
                        : 0.08F
        );
    }

    @SuppressWarnings("unchecked")
    public ActionHandler<IVampirePlayer> stockActionHandler() {
        return (ActionHandler<IVampirePlayer>) super.getActionHandler();
    }

    private VampirePlayer actual() {
        return org.kuro.tvdvampirism.compat.SpeciesCompatibility
                .rawVampire(asEntity());
    }
}
