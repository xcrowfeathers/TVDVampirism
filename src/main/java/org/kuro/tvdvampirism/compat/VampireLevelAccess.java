package org.kuro.tvdvampirism.compat;

import net.minecraft.world.entity.player.Player;

/** Associates the stock attribute cache with its player without changing its fields. */
public interface VampireLevelAccess {
    void tvd$setPlayer(Player player);
    int tvd$vampireLevel();
}
