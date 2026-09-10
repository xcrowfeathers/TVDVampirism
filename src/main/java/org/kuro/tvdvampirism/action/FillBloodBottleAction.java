package org.kuro.tvdvampirism.action;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.actions.DefaultAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.faction.SpeciesFactions;
import org.kuro.tvdvampirism.faction.player.IOriginalHybridPlayer;
import org.kuro.tvdvampirism.player.Species;
import org.kuro.tvdvampirism.player.SpeciesManager;
import org.kuro.tvdvampirism.registry.TransformationContent;
import java.util.Optional;

public final class FillBloodBottleAction extends DefaultAction<IOriginalHybridPlayer> {
    @Override public boolean isEnabled() { return true; }
    @Override public Optional<IPlayableFaction<?>> getFaction() { return Optional.of(SpeciesFactions.originalHybrid()); }
    @Override public int getCooldown(IOriginalHybridPlayer owner) { return 40; }
    @Override public boolean canBeUsedBy(IOriginalHybridPlayer owner) {
        var player = owner.asEntity();
        return SpeciesManager.getSpecies(player) == Species.ORIGINAL_HYBRID && player.isAlive()
                && !player.isSpectator() && player.getHealth() > 2 && owner.getBloodData().getBloodLevel() >= 8
                && !SpeciesCompatibility.rawVampire(player).isDBNO() && bottleSlot(owner) >= 0;
    }
    private int bottleSlot(IOriginalHybridPlayer owner) {
        var inventory = owner.asEntity().getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++)
            if (inventory.getItem(i).is(Items.GLASS_BOTTLE)) return i;
        return -1;
    }
    @Override protected boolean activate(IOriginalHybridPlayer owner, ActivationContext context) {
        if (!(owner.asEntity() instanceof ServerPlayer player) || !canBeUsedBy(owner)) return false;
        // Preconditions precede all costs. Direct health cost cannot trigger combat or DBNO.
        if (!owner.useBlood(8, false)) return false;
        player.setHealth(player.getHealth() - 2);
        player.getInventory().getItem(bottleSlot(owner)).shrink(1);
        var filled = new ItemStack(TransformationContent.ORIGINAL_HYBRID_BLOOD.get());
        if (!player.getInventory().add(filled)) player.drop(filled, false);
        player.getInventory().setChanged();
        return true;
    }
}
