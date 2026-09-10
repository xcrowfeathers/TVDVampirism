package org.kuro.tvdvampirism.faction.player;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import org.kuro.tvdvampirism.faction.SpeciesFactions;

public interface IOriginalHybridPlayer
        extends IFactionPlayer<IOriginalHybridPlayer>, ICustomVampirePlayer {

    @Override
    default IPlayableFaction<IOriginalHybridPlayer> getFaction() {
        return SpeciesFactions.originalHybrid();
    }
}
