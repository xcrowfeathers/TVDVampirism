package org.kuro.tvdvampirism.faction.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.registry.ModAttachments;
import org.kuro.tvdvampirism.skill.SpeciesSkillProfile;

public final class OriginalHybridPlayer
        extends CustomFactionPlayer<IOriginalHybridPlayer>
        implements IOriginalHybridPlayer {

    public OriginalHybridPlayer(Player player) {
        super(player);
    }

    @Override
    public ResourceLocation getAttachedKey() {
        return ModAttachments.Keys.ORIGINAL_HYBRID_PLAYER;
    }

    @Override
    public SpeciesSkillProfile getSkillProfile() {
        return SpeciesSkillProfile.ORIGINAL_HYBRID;
    }
}
