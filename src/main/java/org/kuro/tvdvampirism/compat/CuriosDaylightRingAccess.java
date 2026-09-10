package org.kuro.tvdvampirism.compat;

import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.registry.DaylightRingContent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Curios-only implementation. No always-loaded class exposes a Curios type in
 * its fields or method signatures.
 */
final class CuriosDaylightRingAccess {
    private CuriosDaylightRingAccess() {
    }

    static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.getStacksHandler("ring"))
                .map(rings -> {
                    var stacks = rings.getStacks();
                    var activeStates = rings.getActiveStates();

                    for (int slot = 0; slot < stacks.getSlots(); slot++) {
                        if (activeStates.size() > slot && !activeStates.get(slot)) {
                            continue;
                        }
                        if (stacks.getStackInSlot(slot).is(
                                DaylightRingContent.DAYLIGHT_RING.get()
                        )) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }
}
