package org.kuro.tvdvampirism.mastery;

import de.teamlapen.vampirism.entity.vampire.VampireTaskMasterEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;

@EventBusSubscriber(modid="tvdvampirism")
public final class MasteryQuests {
    private MasteryQuests() {}
    public static boolean correctGiver(Player player, Entity giver) {
        var custom=SpeciesCompatibility.customPlayer(player);
        return custom != null && giver instanceof VampireTaskMasterEntity
                && custom.hasWerewolfSkillBridge() == (giver instanceof HybridQuestGiver);
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof VampireTaskMasterEntity giver)) return;
        var player=event.getEntity();
        if (SpeciesCompatibility.customPlayer(player)==null && !(giver instanceof HybridQuestGiver)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.level().isClientSide && correctGiver(player,giver) && player.isAlive() && !player.isSpectator())
            giver.processInteraction(player,giver);
    }
}
