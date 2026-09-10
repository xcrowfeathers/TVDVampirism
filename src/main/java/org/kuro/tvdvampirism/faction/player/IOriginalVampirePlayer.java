package org.kuro.tvdvampirism.faction.player;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import org.kuro.tvdvampirism.faction.SpeciesFactions;

public interface IOriginalVampirePlayer
        extends IFactionPlayer<IOriginalVampirePlayer>, ICustomVampirePlayer {

    @Override
    default IPlayableFaction<IOriginalVampirePlayer> getFaction() {
        return SpeciesFactions.originalVampire();
    }
}
