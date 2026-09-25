package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.blockentity.*;
import de.teamlapen.vampirism.blocks.CoffinBlock;
import de.teamlapen.vampirism.items.CrucifixItem;
import de.teamlapen.vampirism.util.DamageHandler;
import de.teamlapen.vampirism.entity.player.ModPlayerEventHandler;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import org.kuro.tvdvampirism.compat.VampireLevelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Show the custom level to callers, but keep the stock tick from repeating blood use and leveling. */
@Mixin(value = {AltarInspirationBlockEntity.class, AltarInfusionBlockEntity.class,
        GarlicDiffuserBlockEntity.class, SunscreenBeaconBlockEntity.class,
        CoffinBlock.class, CrucifixItem.class, DamageHandler.class,
        ModPlayerEventHandler.class}, remap = false)
public abstract class VampireLevelChecksMixin {
    @Redirect(method = "*", at = @At(value = "FIELD", opcode = 180,
            target = "Lde/teamlapen/vampirism/entity/player/VampirismPlayerAttributes;vampireLevel:I"))
    private static int tvd$effectiveLevel(VampirismPlayerAttributes attributes) {
        return ((VampireLevelAccess) attributes).tvd$vampireLevel();
    }
}
