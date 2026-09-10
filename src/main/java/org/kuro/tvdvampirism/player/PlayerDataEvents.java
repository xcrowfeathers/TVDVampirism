package org.kuro.tvdvampirism.player;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.attribute.SpeciesAttributeManager;
import org.kuro.tvdvampirism.blood.BloodManager;

@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class PlayerDataEvents {

    private PlayerDataEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SpeciesTransitionManager.migrateLegacySpecies(player);
            if (BloodManager.getCustomVampire(player).isEmpty()) {
                SpeciesAttributeManager.clear(player);
            }
        }
    }

    @SubscribeEvent
    public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // The event precedes modifier replacement; consume this flag in the existing post tick.
            BloodManager.getCustomVampire(player)
                    .ifPresent(custom -> custom.requestEquipmentAttributeUpdate());
        }
    }
}
