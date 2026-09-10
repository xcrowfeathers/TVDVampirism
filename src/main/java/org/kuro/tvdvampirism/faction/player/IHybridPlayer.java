package org.kuro.tvdvampirism.faction.player;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import org.kuro.tvdvampirism.faction.SpeciesFactions;

public interface IHybridPlayer extends IFactionPlayer<IHybridPlayer>, ICustomVampirePlayer {

    @Override
    default IPlayableFaction<IHybridPlayer> getFaction() {
        return SpeciesFactions.hybrid();
    }
}
