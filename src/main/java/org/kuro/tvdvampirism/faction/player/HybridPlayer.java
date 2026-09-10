package org.kuro.tvdvampirism.faction.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.registry.ModAttachments;
import org.kuro.tvdvampirism.skill.SpeciesSkillProfile;

public final class HybridPlayer
        extends CustomFactionPlayer<IHybridPlayer>
        implements IHybridPlayer {

    public HybridPlayer(Player player) {
        super(player);
    }

    @Override
    public ResourceLocation getAttachedKey() {
        return ModAttachments.Keys.HYBRID_PLAYER;
    }

    @Override
    public SpeciesSkillProfile getSkillProfile() {
        return SpeciesSkillProfile.HYBRID;
    }
}
