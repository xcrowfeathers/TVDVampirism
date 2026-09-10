package org.kuro.tvdvampirism.action;

import com.google.common.collect.ImmutableList;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.api.entity.player.actions.IActionHandler;
import de.teamlapen.vampirism.api.entity.player.actions.ILastingAction;
import de.teamlapen.vampirism.entity.player.actions.ActionHandler;
import de.teamlapen.werewolves.api.WReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.skill.SpeciesSkillRules;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public final class SpeciesActionHandler<T extends IFactionPlayer<T>>
        implements IActionHandler<T> {

    private static final String NBT_KEY = "action_handler";
    private static final String CUSTOM = "custom";
    private static final String VAMPIRE = "vampire";
    private static final String WEREWOLF = "werewolf";

    private final CustomFactionPlayer<T> owner;
    private final ActionHandler<T> custom;

    public SpeciesActionHandler(
            T player,
            CustomFactionPlayer<T> owner
    ) {
        this.owner = owner;
        this.custom = new ActionHandler<>(player);
        if (owner instanceof org.kuro.tvdvampirism.faction.player.IOriginalHybridPlayer) {
            unlockRaw(custom, org.kuro.tvdvampirism.registry.BiteContent.FILL_BLOOD_BOTTLE.get());
        }
    }

    @Override
    public void deactivateAllActions() {
        custom.deactivateAllActions();

        if (owner.hasVampireSkillBridge()) {
            owner.getVampireSkillBridge()
                    .stockActionHandler()
                    .deactivateAllActions();
        }

        if (owner.hasWerewolfSkillBridge()) {
            owner.getWerewolfSkillBridge()
                    .stockActionHandler()
                    .deactivateAllActions();
        }
    }

    @Override
    public void extendActionTimer(
            @NotNull ILastingAction<T> action,
            int duration
    ) {
        extendRaw(backend(action), action, duration);
    }

    @Override
    public @NotNull List<IAction<T>> getAvailableActions() {
        LinkedHashSet<IAction<T>> result =
                new LinkedHashSet<>(custom.getAvailableActions());

        if (owner.hasVampireSkillBridge()) {
            addRaw(
                    result,
                    owner.getVampireSkillBridge()
                            .stockActionHandler()
                            .getAvailableActions()
            );
        }

        if (owner.hasWerewolfSkillBridge()) {
            addRaw(
                    result,
                    owner.getWerewolfSkillBridge()
                            .stockActionHandler()
                            .getAvailableActions()
            );
        }

        result.removeIf(action -> !SpeciesSkillRules.allowsAction(owner, action));
        return List.copyOf(result);
    }

    @Override
    public float getPercentageForAction(
            @NotNull IAction<T> action
    ) {
        return percentageRaw(
                backend(action),
                action
        );
    }

    @Override
    public @NotNull ImmutableList<IAction<T>> getUnlockedActions() {
        LinkedHashSet<IAction<T>> result =
                new LinkedHashSet<>(custom.getUnlockedActions());

        if (owner.hasVampireSkillBridge()) {
            addRaw(
                    result,
                    owner.getVampireSkillBridge()
                            .stockActionHandler()
                            .getUnlockedActions()
            );
        }

        if (owner.hasWerewolfSkillBridge()) {
            addRaw(
                    result,
                    owner.getWerewolfSkillBridge()
                            .stockActionHandler()
                            .getUnlockedActions()
            );
        }

        result.removeIf(action -> !SpeciesSkillRules.allowsAction(owner, action));
        return ImmutableList.copyOf(result);
    }

    @Override
    public boolean isActionActive(
            @NotNull ILastingAction<T> action
    ) {
        return isActiveRaw(
                backend(action),
                action
        );
    }

    @Override
    public boolean isActionActive(ResourceLocation id) {
        if (custom.isActionActive(id)) {
            return true;
        }

        if (owner.hasVampireSkillBridge()
                && owner.getVampireSkillBridge()
                .stockActionHandler()
                .isActionActive(id)) {
            return true;
        }

        return owner.hasWerewolfSkillBridge()
                && owner.getWerewolfSkillBridge()
                .stockActionHandler()
                .isActionActive(id);
    }

    @Override
    public boolean isActionOnCooldown(IAction<T> action) {
        return isCooldownRaw(
                backend(action),
                action
        );
    }

    @Override
    public boolean isActionUnlocked(IAction<T> action) {
        if (!SpeciesSkillRules.allowsAction(owner, action)) return false;
        return isUnlockedRaw(
                backend(action),
                action
        );
    }

    @Override
    public void relockActions(Collection<IAction<T>> actions) {
        actions.forEach(action ->
                relockRaw(backend(action), action)
        );
    }

    @Override
    public void resetTimers() {
        custom.resetTimers();

        if (owner.hasVampireSkillBridge()) {
            owner.getVampireSkillBridge()
                    .stockActionHandler()
                    .resetTimers();
        }

        if (owner.hasWerewolfSkillBridge()) {
            owner.getWerewolfSkillBridge()
                    .stockActionHandler()
                    .resetTimers();
        }
    }

    @Override
    public void resetTimer(@NotNull IAction<T> action) {
        resetRaw(
                backend(action),
                action
        );
    }

    @Override
    public IAction.PERM toggleAction(
            IAction<T> action,
            IAction.ActivationContext context
    ) {
        if (!SpeciesSkillRules.allowsAction(owner, action)) return IAction.PERM.DISABLED;
        return toggleRaw(
                backend(action),
                action,
                context
        );
    }

    @Override
    public void deactivateAction(
            ILastingAction<T> action
    ) {
        deactivateRaw(
                backend(action),
                action
        );
    }

    @Override
    public void unlockActions(
            Collection<IAction<T>> actions
    ) {
        actions.forEach(action ->
                unlockRaw(backend(action), action)
        );
    }

    public boolean updateActions() {
        boolean dirty = custom.updateActions();

        if (owner.hasVampireSkillBridge()) {
            dirty |= owner.getVampireSkillBridge()
                    .stockActionHandler()
                    .updateActions();
        }

        if (owner.hasWerewolfSkillBridge()) {
            dirty |= owner.getWerewolfSkillBridge()
                    .stockActionHandler()
                    .updateActions();
        }

        return dirty;
    }

    public void onActionsReactivated() {
        custom.onActionsReactivated();

        if (owner.hasVampireSkillBridge()) {
            owner.getVampireSkillBridge()
                    .stockActionHandler()
                    .onActionsReactivated();
        }

        if (owner.hasWerewolfSkillBridge()) {
            owner.getWerewolfSkillBridge()
                    .stockActionHandler()
                    .onActionsReactivated();
        }
    }

    public @NotNull CompoundTag serializeNBT(
            HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();

        tag.put(
                CUSTOM,
                custom.serializeNBT(provider)
        );

        if (owner.hasVampireSkillBridge()) {
            tag.put(
                    VAMPIRE,
                    owner.getVampireSkillBridge()
                            .stockActionHandler()
                            .serializeNBT(provider)
            );
        }

        if (owner.hasWerewolfSkillBridge()) {
            tag.put(
                    WEREWOLF,
                    owner.getWerewolfSkillBridge()
                            .stockActionHandler()
                            .serializeNBT(provider)
            );
        }

        return tag;
    }

    public @NotNull CompoundTag serializeUpdateNBT(
            HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();

        tag.put(
                CUSTOM,
                custom.serializeUpdateNBT(provider)
        );

        if (owner.hasVampireSkillBridge()) {
            tag.put(
                    VAMPIRE,
                    owner.getVampireSkillBridge()
                            .stockActionHandler()
                            .serializeUpdateNBT(provider)
            );
        }

        if (owner.hasWerewolfSkillBridge()) {
            tag.put(
                    WEREWOLF,
                    owner.getWerewolfSkillBridge()
                            .stockActionHandler()
                            .serializeUpdateNBT(provider)
            );
        }

        return tag;
    }

    public void deserializeNBT(
            HolderLookup.Provider provider,
            @NotNull CompoundTag tag
    ) {
        tag = filterActionState(tag);
        if (tag.contains(CUSTOM, Tag.TAG_COMPOUND)) {
            custom.deserializeNBT(
                    provider,
                    tag.getCompound(CUSTOM)
            );
        } else {
            custom.deserializeNBT(provider, tag);
        }

        if (owner.getSkillProfile().hasVampireTree()
                && tag.contains(VAMPIRE, Tag.TAG_COMPOUND)) {
            owner.getVampireSkillBridge()
                    .stockActionHandler()
                    .deserializeNBT(
                            provider,
                            tag.getCompound(VAMPIRE)
                    );
        }

        if (owner.getSkillProfile().hasWerewolfTree()
                && tag.contains(WEREWOLF, Tag.TAG_COMPOUND)) {
            owner.getWerewolfSkillBridge()
                    .stockActionHandler()
                    .deserializeNBT(
                            provider,
                            tag.getCompound(WEREWOLF)
                    );
        }

    }

    public void deserializeUpdateNBT(
            HolderLookup.Provider provider,
            @NotNull CompoundTag tag
    ) {
        tag = filterActionState(tag);
        if (tag.contains(CUSTOM, Tag.TAG_COMPOUND)) {
            custom.deserializeUpdateNBT(
                    provider,
                    tag.getCompound(CUSTOM)
            );
        } else {
            custom.deserializeUpdateNBT(provider, tag);
        }

        if (owner.getSkillProfile().hasVampireTree()
                && tag.contains(VAMPIRE, Tag.TAG_COMPOUND)) {
            owner.getVampireSkillBridge()
                    .stockActionHandler()
                    .deserializeUpdateNBT(
                            provider,
                            tag.getCompound(VAMPIRE)
                    );
        }

        if (owner.getSkillProfile().hasWerewolfTree()
                && tag.contains(WEREWOLF, Tag.TAG_COMPOUND)) {
            owner.getWerewolfSkillBridge()
                    .stockActionHandler()
                    .deserializeUpdateNBT(
                            provider,
                            tag.getCompound(WEREWOLF)
                    );
        }

    }

    public String nbtKey() {
        return NBT_KEY;
    }

    private CompoundTag filterActionState(CompoundTag input) {
        var sunscreen = de.teamlapen.vampirism.entity.player.vampire.actions.VampireActions.SUNSCREEN.get();
        if (SpeciesSkillRules.allowsAction(owner, sunscreen)) return input;
        var result = input.copy();
        String id = de.teamlapen.vampirism.util.RegUtil.id(sunscreen).toString();
        for (CompoundTag state : List.of(result, result.getCompound(CUSTOM), result.getCompound(VAMPIRE), result.getCompound(WEREWOLF))) {
            for (String timers : List.of("actions_active", "actions_cooldown", "actions_cooldown_expected", "actions_duration_expected"))
                state.getCompound(timers).remove(id);
        }
        return result;
    }

    private ActionHandler<?> backend(IAction<?> action) {
        IPlayableFaction<?> faction =
                action.getFaction().orElse(null);

        if (faction == VReference.VAMPIRE_FACTION
                && owner.getSkillProfile().hasVampireTree()) {
            return owner.getVampireSkillBridge()
                    .stockActionHandler();
        }

        if (faction == WReference.WEREWOLF_FACTION
                && owner.getSkillProfile().hasWerewolfTree()) {
            return owner.getWerewolfSkillBridge()
                    .stockActionHandler();
        }

        return custom;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addRaw(
            Collection target,
            Collection source
    ) {
        target.addAll(source);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void extendRaw(
            ActionHandler handler,
            ILastingAction action,
            int duration
    ) {
        handler.extendActionTimer(action, duration);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static float percentageRaw(
            ActionHandler handler,
            IAction action
    ) {
        return handler.getPercentageForAction(action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean isActiveRaw(
            ActionHandler handler,
            ILastingAction action
    ) {
        return handler.isActionActive(action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean isCooldownRaw(
            ActionHandler handler,
            IAction action
    ) {
        return handler.isActionOnCooldown(action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean isUnlockedRaw(
            ActionHandler handler,
            IAction action
    ) {
        return handler.isActionUnlocked(action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void relockRaw(
            ActionHandler handler,
            IAction action
    ) {
        handler.relockActions(List.of(action));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void resetRaw(
            ActionHandler handler,
            IAction action
    ) {
        handler.resetTimer(action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IAction.PERM toggleRaw(
            ActionHandler handler,
            IAction action,
            IAction.ActivationContext context
    ) {
        return handler.toggleAction(
                action,
                context
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void deactivateRaw(
            ActionHandler handler,
            ILastingAction action
    ) {
        handler.deactivateAction(action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void unlockRaw(
            ActionHandler handler,
            IAction action
    ) {
        if (!handler.isActionUnlocked(action)) {
            handler.unlockActions(List.of(action));
        }
    }
}
