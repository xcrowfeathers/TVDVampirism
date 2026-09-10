package org.kuro.tvdvampirism.faction.player;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import org.kuro.tvdvampirism.faction.SpeciesFactions;

public interface IAugustineVampirePlayer
        extends IFactionPlayer<IAugustineVampirePlayer>, ICustomVampirePlayer {

    @Override
    default IPlayableFaction<IAugustineVampirePlayer> getFaction() {
        return SpeciesFactions.augustineVampire();
    }
}
