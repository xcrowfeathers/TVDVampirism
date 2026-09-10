package org.kuro.tvdvampirism.skill.bridge;

import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.api.entity.player.actions.IActionHandler;
import de.teamlapen.vampirism.entity.player.actions.ActionHandler;
import de.teamlapen.werewolves.api.entities.player.IWerewolfPlayer;
import de.teamlapen.werewolves.api.entities.werewolf.WerewolfForm;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayerSpecialAttributes;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfInventory;
import de.teamlapen.werewolves.entities.player.werewolf.actions.WerewolfFormAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

import java.util.List;
import java.util.Optional;

public final class WerewolfSkillPlayerBridge extends WerewolfPlayer {

    private final CustomFactionPlayer<?> owner;

    public WerewolfSkillPlayerBridge(CustomFactionPlayer<?> owner) {
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
    public WerewolfInventory getInventory() {
        return actual().getInventory();
    }

    @NotNull
    @Override
    public WerewolfPlayerSpecialAttributes getSpecialAttributes() {
        return actual().getSpecialAttributes();
    }

    @NotNull
    @Override
    public WerewolfForm getForm() {
        return actual().getForm();
    }

    @Override
    public void setForm(
            WerewolfFormAction action,
            WerewolfForm form
    ) {
        actual().setForm(action, form);
    }

    @Override
    public void switchForm(WerewolfForm form) {
        actual().switchForm(form);
    }

    @Nullable
    @Override
    public WerewolfFormAction getLastFormAction() {
        return actual().getLastFormAction();
    }

    @Override
    public void checkToolDamage(
            @NotNull ItemStack from,
            @NotNull ItemStack itemInHand,
            boolean forceCalculation
    ) {
        actual().checkToolDamage(
                from,
                itemInHand,
                forceCalculation
        );
    }

    @Override
    public boolean canWearArmor(ItemStack stack) {
        return super.canWearArmor(getForm(), stack);
    }

    @Override
    public boolean canWearArmor(List<ItemStack> stacks) {
        return super.canWearArmor(getForm(), stacks);
    }

    @Override
    public Optional<Tier> getDigDropTier() {
        return actual().getDigDropTier();
    }

    @Override
    public float getDigSpeed() {
        return actual().getDigSpeed();
    }

    @NotNull
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ISkillHandler<IWerewolfPlayer> getSkillHandler() {
        return (ISkillHandler) owner.getSkillHandler();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NotNull IActionHandler<IWerewolfPlayer> getActionHandler() {
        return (IActionHandler) owner.getActionHandler();
    }

    @SuppressWarnings("unchecked")
    public ActionHandler<IWerewolfPlayer> stockActionHandler() {
        return (ActionHandler<IWerewolfPlayer>) super.getActionHandler();
    }

    private WerewolfPlayer actual() {
        return org.kuro.tvdvampirism.compat.SpeciesCompatibility.rawWerewolf(asEntity());
    }
}
