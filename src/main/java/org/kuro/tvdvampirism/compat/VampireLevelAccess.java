package org.kuro.tvdvampirism.compat;

import net.minecraft.world.entity.player.Player;

public interface VampireLevelAccess {
    void tvd$setPlayer(Player player);
    int tvd$vampireLevel();
}
