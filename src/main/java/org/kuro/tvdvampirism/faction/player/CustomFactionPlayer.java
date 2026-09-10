package org.kuro.tvdvampirism.faction.player;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IActionHandler;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import de.teamlapen.vampirism.entity.player.FactionBasePlayer;
import de.teamlapen.vampirism.entity.player.skills.SkillHandler;
import de.teamlapen.vampirism.fluids.BloodHelper;
import de.teamlapen.vampirism.network.ServerboundSimpleInputEvent;
import de.teamlapen.vampirism.util.DamageHandler;
import de.teamlapen.vampirism.world.ModDamageSources;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.kuro.tvdvampirism.attribute.SpeciesAttributeManager;
import org.kuro.tvdvampirism.attribute.SpeciesAttributes;
import org.kuro.tvdvampirism.blood.BloodData;
import org.kuro.tvdvampirism.blood.FeedingManager;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.FactionRelations;
import org.kuro.tvdvampirism.action.SpeciesActionHandler;
import org.kuro.tvdvampirism.skill.SpeciesSkillAccess;
import org.kuro.tvdvampirism.skill.SpeciesSkillProfile;
import org.kuro.tvdvampirism.skill.bridge.VampireSkillPlayerBridge;
import org.kuro.tvdvampirism.skill.bridge.WerewolfSkillPlayerBridge;
import org.kuro.tvdvampirism.player.SpeciesRules;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class CustomFactionPlayer<T extends IFactionPlayer<T>>
        extends FactionBasePlayer<T> {

    private final SpeciesActionHandler<T> actionHandler;

    private VampireSkillPlayerBridge vampireSkillBridge;
    private WerewolfSkillPlayerBridge werewolfSkillBridge;
    private final SkillHandler<T> skillHandler;
    private final BloodData bloodData;
    private int masteryLevel;
    public int getMasteryLevel() { return masteryLevel; }
    public void setMasteryLevel(int level) { masteryLevel = Math.clamp(level, 0, 5); }

    private int feedingTargetId = -1;
    private Entity serverFeedingTarget;
    private int feedingBloodGained;
    private float feedingLifeDrained;
    private int vampireOverflowPulses;

    public int limitFeedingGain(int amount) {
        int allowed = Math.min(Math.max(0, amount), Math.max(0,
                ServerConfig.AUGUSTINE_FEEDING_CAP.get() - feedingBloodGained));
        feedingBloodGained += allowed;
        return allowed;
    }

    public boolean reachedFeedingCap() {
        return feedingBloodGained >= ServerConfig.AUGUSTINE_FEEDING_CAP.get();
    }

    public float remainingFeedingLifeDrain() {
        return Math.max(0.0F, ServerConfig.AUGUSTINE_FEEDING_CAP.get() - feedingLifeDrained);
    }

    public void recordFeedingLifeDrain(float amount) {
        feedingLifeDrained = Math.min(ServerConfig.AUGUSTINE_FEEDING_CAP.get(),
                feedingLifeDrained + Math.max(0.0F, amount));
    }

    public boolean reachedFeedingLifeDrainCap() {
        return feedingLifeDrained >= ServerConfig.AUGUSTINE_FEEDING_CAP.get();
    }

    public boolean advanceVampireOverflow() {
        if (++vampireOverflowPulses < 2) return false;
        vampireOverflowPulses = 0;
        return true;
    }

    public boolean matchesFeedingTarget(Entity target) {
        return isRemote() || serverFeedingTarget == target;
    }
    private int feedingTicks;
    private int feedingIntervalTicks = 20;
    private boolean equipmentAttributesDirty;

    protected CustomFactionPlayer(Player player) {
        super(player);

        T self = self();
        this.actionHandler = new SpeciesActionHandler<>(self, this);
        this.skillHandler = new SkillHandler<>(self, getFaction());
        this.bloodData = new BloodData(player,
                SpeciesAttributeManager.bloodCapacity(SpeciesAttributes.getProfile(this), 1));
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    public abstract @NotNull SpeciesSkillProfile getSkillProfile();

    /**
     * Every current custom faction player is vampire-based and therefore exposes
     * the Vampire skill bridge. This is a species capability, not an initialization check.
     */
    public final boolean hasVampireSkillBridge() {
        return true;
    }

    /**
     * Only Hybrid profiles expose the Werewolf skill bridge.
     */
    public final boolean hasWerewolfSkillBridge() {
        return switch (getSkillProfile()) {
            case HYBRID, ORIGINAL_HYBRID -> true;
            default -> false;
        };
    }

    public final @NotNull VampireSkillPlayerBridge getVampireSkillBridge() {
        if (vampireSkillBridge == null) {
            vampireSkillBridge = new VampireSkillPlayerBridge(this);
        }

        return vampireSkillBridge;
    }

    public final @NotNull WerewolfSkillPlayerBridge getWerewolfSkillBridge() {
        if (werewolfSkillBridge == null) {
            werewolfSkillBridge = new WerewolfSkillPlayerBridge(this);
        }

        return werewolfSkillBridge;
    }

    @Override
    public boolean canLeaveFaction() {
        return true;
    }

    @Override
    public IFaction<?> getDisguisedAs() {
        return getFaction();
    }

    @Override
    public int getMaxLevel() {
        return getFaction().getHighestReachableLevel();
    }

    @Override
    public Predicate<LivingEntity> getNonFriendlySelector(
            boolean otherFactionPlayers,
            boolean ignoreDisguise
    ) {
        return otherFactionPlayers
                ? entity -> true
                : entity -> FactionRelations.isHostileTarget(
                getFaction(),
                entity,
                ignoreDisguise
        );
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public @NotNull ISkillHandler<T> getSkillHandler() {
        return skillHandler;
    }

    @Override
    public @NotNull IActionHandler<T> getActionHandler() {
        return actionHandler;
    }

    public BloodData getBloodData() {
        return bloodData;
    }

    public void syncBlood(boolean all) {
        syncProperty(bloodData, all);
    }

    public void drinkBlood(
            int amount,
            float saturationModifier,
            IDrinkBloodContext context
    ) {
        drinkBlood(amount, saturationModifier, true, context);
    }

    public void drinkBlood(int amount, float saturationModifier,
                           boolean useRemaining, IDrinkBloodContext context) {
        amount = org.kuro.tvdvampirism.blood.AugustineFeedingPolicy.intake(this, amount, context);
        int overflow = bloodData.addBlood(amount, saturationModifier);

        if (!isRemote() && useRemaining && overflow > 0) {
            if (org.kuro.tvdvampirism.blood.AugustineFeedingPolicy.storeVampireOverflow(this, context)) return;
            BloodHelper.fillBloodIntoInventory(
                    player,
                    overflow * VReference.FOOD_TO_FLUID_BLOOD
            );
        }
    }

    public void addBloodExhaustion(float amount) {
        bloodData.addExhaustion(amount);
    }

    public boolean useBlood(int amount, boolean allowPartial) {
        boolean result = bloodData.removeBlood(amount, allowPartial);

        if (!isRemote()) {
            syncBlood(false);
        }

        return result;
    }

    public boolean wantsBlood() {
        return bloodData.needsBlood();
    }

    public int getFeedingTargetId() {
        return feedingTargetId;
    }

    public float getFeedProgress() {
        if (feedingTargetId < 0 || feedingIntervalTicks <= 0) {
            return 0.0F;
        }

        return feedingTicks / (float) feedingIntervalTicks;
    }

    public void startFeeding(int targetId) {
        if (feedingTargetId != targetId) {
            feedingTicks = 0;
            feedingBloodGained = 0;
            feedingLifeDrained = 0;
            vampireOverflowPulses = 0;
        }

        feedingTargetId = targetId;
        if (!isRemote()) serverFeedingTarget = player.level().getEntity(targetId);
        feedingIntervalTicks = Math.max(
                1,
                ServerConfig.CUSTOM_VAMPIRE_FEEDING_INTERVAL_TICKS.get()
        );

        syncFeedingTarget();
    }

    public void stopFeeding(boolean synchronize) {
        feedingTargetId = -1;
        serverFeedingTarget = null;
        feedingBloodGained = 0;
        feedingLifeDrained = 0;
        vampireOverflowPulses = 0;
        feedingTicks = 0;

        if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }

        if (synchronize) {
            syncFeedingTarget();
        }
    }

    public boolean advanceFeedingTick(int interval) {
        feedingIntervalTicks = Math.max(1, interval);

        if (++feedingTicks < feedingIntervalTicks) {
            return false;
        }

        feedingTicks = 0;
        return true;
    }

    private void syncFeedingTarget() {
        CompoundTag update = new CompoundTag();
        update.putInt("feeding_target", feedingTargetId);
        update.putInt("feeding_interval", feedingIntervalTicks);
        sync(update, true);
    }

    private void tickClientFeeding() {
        if (feedingTargetId < 0) {
            feedingTicks = 0;
            return;
        }

        feedingTicks++;

        if (feedingTicks % 5 == 0 && !FeedingManager.isForcedFeeding(this)) {
            Entity target = VampirismMod.proxy.getMouseOverEntity();

            if (target == null || target.getId() != feedingTargetId) {
                VampirismMod.proxy.sendToServer(
                        new ServerboundSimpleInputEvent(
                                ServerboundSimpleInputEvent.Event.FINISH_SUCK_BLOOD
                        )
                );

                feedingTargetId = -1;
                feedingTicks = 0;
                return;
            }
        }

        if (feedingTicks >= feedingIntervalTicks) {
            feedingTicks = 0;
        }
    }

    @Override
    public void onUpdate() {
        if (getLevel() <= 0) {
            return;
        }

        // Vampirism's own VampirePlayer update returns before blood, action and
        // feeding processing while DBNO. Mirror that boundary for custom factions.
        if (org.kuro.tvdvampirism.compat.SpeciesCompatibility.rawVampire(player).isDBNO()) {
            if (!isRemote() && feedingTargetId >= 0) {
                stopFeeding(true);
            }
            return;
        }

        super.onUpdate();

        if (isRemote()) {
            actionHandler.updateActions();
            tickClientFeeding();
            return;
        }

        org.kuro.tvdvampirism.compat.SpeciesCompatibility.tickVampireEnvironment(this);

        if (equipmentAttributesDirty) {
            SpeciesAttributeManager.updateEquipment(this);
            equipmentAttributesDirty = false;
        } else if (player.tickCount % 128 == 0) {
            SpeciesAttributeManager.updateNaturalArmor(this);
        }

        CompoundTag update = new CompoundTag();

        if (actionHandler.updateActions()) {
            update.put(
                    actionHandler.nbtKey(),
                    actionHandler.serializeUpdateNBT(player.registryAccess())
            );
        }

        if (skillHandler.isDirty()) {
            update.put(
                    skillHandler.nbtKey(),
                    skillHandler.serializeUpdateNBT(player.registryAccess())
            );
        }

        if (bloodData.tickServer()) {
            update.put(
                    bloodData.nbtKey(),
                    bloodData.serializeUpdateNBT(player.registryAccess())
            );
        }

        FeedingManager.tick(this);

        if (!update.isEmpty()) {
            sync(update, true);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        if (getLevel() > 0) {
            super.onDeath(source);
            actionHandler.deactivateAllActions();
        }
    }

    @Override
    public boolean onEntityAttacked(DamageSource source, float amount) {
        if (getLevel() > 0 && SpeciesRules.hasFireVulnerability(player)) {
            if (source.is(DamageTypes.ON_FIRE)) {
                DamageHandler.hurtModded(player, ModDamageSources::vampireOnFire,
                        getVampireSkillBridge().calculateFireDamage(amount));
                return true;
            }
            if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.LAVA)) {
                DamageHandler.hurtModded(player, ModDamageSources::vampireInFire,
                        getVampireSkillBridge().calculateFireDamage(amount));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onChangedDimension(
            ResourceKey<Level> from,
            ResourceKey<Level> to
    ) {
        stopFeeding(true);
    }

    @Override
    public void onJoinWorld() {
        reactivateActions();

        if (getLevel() > 0) {
            SpeciesAttributeManager.refresh(this);
            refreshSkillAccess();
        }
    }

    @Override
    public void onPlayerLoggedIn() {
        reactivateActions();

        if (getLevel() > 0) {
            SpeciesAttributeManager.refresh(this);
            refreshSkillAccess();
        }
    }

    @Override
    public void onLevelChanged(int newLevel, int oldLevel) {
        if (newLevel <= 0) {
            setMasteryLevel(0);
        }

        super.onLevelChanged(newLevel, oldLevel);
        SpeciesAttributeManager.refresh(this, newLevel);

        if (newLevel > 0) {
            refreshSkillAccess();
        }
    }

    public void requestEquipmentAttributeUpdate() {
        equipmentAttributesDirty = true;
    }

    @Override
    public void onPlayerLoggedOut() {
    }

    @Override
    public void onUpdatePlayer(PlayerTickEvent event) {
    }

    private void reactivateActions() {
        if (getLevel() > 0) {
            org.kuro.tvdvampirism.skill.SpeciesSkillRules.cleanLoadedState(this);
            actionHandler.onActionsReactivated();
        }
    }

    private void refreshSkillAccess() {
        if (!isRemote()) {
            SpeciesSkillAccess.refresh(this);
        }
    }

    @Override
    public @NotNull CompoundTag serializeNBT(
            HolderLookup.Provider provider
    ) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putInt("MasteryLevel", masteryLevel);

        tag.put(
                skillHandler.nbtKey(),
                skillHandler.serializeNBT(provider)
        );
        tag.put(
                actionHandler.nbtKey(),
                actionHandler.serializeNBT(provider)
        );
        tag.put(
                bloodData.nbtKey(),
                bloodData.serializeNBT(provider)
        );

        return tag;
    }

    @Override
    public void deserializeNBT(
            HolderLookup.Provider provider,
            CompoundTag tag
    ) {
        super.deserializeNBT(provider, tag);
        setMasteryLevel(tag.getInt("MasteryLevel"));

        skillHandler.deserializeNBT(
                provider,
                tag.getCompound(skillHandler.nbtKey())
        );

        actionHandler.deserializeNBT(
                provider,
                tag.getCompound(actionHandler.nbtKey())
        );

        if (tag.contains(bloodData.nbtKey())) {
            bloodData.deserializeNBT(
                    provider,
                    tag.getCompound(bloodData.nbtKey())
            );
        }
    }

    @Override
    public @NotNull CompoundTag serializeUpdateNBT(
            HolderLookup.Provider provider
    ) {
        CompoundTag tag = super.serializeUpdateNBT(provider);
        tag.putInt("MasteryLevel", masteryLevel);

        tag.put(
                skillHandler.nbtKey(),
                skillHandler.serializeUpdateNBT(provider)
        );

        tag.put(
                actionHandler.nbtKey(),
                actionHandler.serializeUpdateNBT(provider)
        );

        tag.put(
                bloodData.nbtKey(),
                bloodData.serializeUpdateNBT(provider)
        );

        tag.putInt("feeding_target", feedingTargetId);
        tag.put("EmbeddedDagger", org.kuro.tvdvampirism.player.SpeciesManager.getData(player).getEmbeddedDagger().saveOptional(provider));
        tag.putInt("feeding_interval", feedingIntervalTicks);

        return tag;
    }

    @Override
    public void deserializeUpdateNBT(
            HolderLookup.Provider provider,
            CompoundTag tag
    ) {
        super.deserializeUpdateNBT(provider, tag);
        if (tag.contains("MasteryLevel")) setMasteryLevel(tag.getInt("MasteryLevel"));
        if (tag.contains("EmbeddedDagger")) org.kuro.tvdvampirism.player.SpeciesManager.getData(player)
                .setEmbeddedDagger(net.minecraft.world.item.ItemStack.parseOptional(provider, tag.getCompound("EmbeddedDagger")));

        skillHandler.deserializeUpdateNBT(
                provider,
                tag.getCompound(skillHandler.nbtKey())
        );

        actionHandler.deserializeUpdateNBT(
                provider,
                tag.getCompound(actionHandler.nbtKey())
        );

        if (tag.contains(bloodData.nbtKey())) {
            bloodData.deserializeUpdateNBT(
                    provider,
                    tag.getCompound(bloodData.nbtKey())
            );
        }

        if (tag.contains("feeding_interval")) {
            feedingIntervalTicks = Math.max(
                    1,
                    tag.getInt("feeding_interval")
            );
        }

        if (tag.contains("feeding_target")) {
            int newTarget = tag.getInt("feeding_target");

            if (newTarget != feedingTargetId) {
                feedingTicks = 0;
            }

            feedingTargetId = newTarget;
        }
    }

    @Override
    public String nbtKey() {
        return getAttachedKey().getPath();
    }

    @Override
    public abstract @NotNull ResourceLocation getAttachedKey();

    public static <P extends CustomFactionPlayer<?>> Function<IAttachmentHolder, P> factory(
            Function<Player, P> constructor,
            String speciesName
    ) {
        return holder -> createPlayerAttachment(
                holder,
                constructor,
                speciesName
        );
    }

    public static <P extends CustomFactionPlayer<?>> IAttachmentSerializer<CompoundTag, P> serializer(
            Function<Player, P> constructor,
            String speciesName
    ) {
        return new IAttachmentSerializer<>() {

            @Override
            public P read(
                    IAttachmentHolder holder,
                    CompoundTag tag,
                    HolderLookup.Provider provider
            ) {
                P attachment = createPlayerAttachment(
                        holder,
                        constructor,
                        speciesName
                );

                attachment.deserializeNBT(provider, tag);
                return attachment;
            }

            @Override
            public CompoundTag write(
                    P attachment,
                    HolderLookup.Provider provider
            ) {
                return attachment.serializeNBT(provider);
            }
        };
    }

    private static <P extends CustomFactionPlayer<?>> P createPlayerAttachment(
            IAttachmentHolder holder,
            Function<Player, P> constructor,
            String speciesName
    ) {
        if (holder instanceof Player player) {
            return constructor.apply(player);
        }

        throw new IllegalArgumentException(
                "Cannot create "
                        + speciesName
                        + " player attachment for "
                        + holder.getClass().getName()
        );
    }
}
