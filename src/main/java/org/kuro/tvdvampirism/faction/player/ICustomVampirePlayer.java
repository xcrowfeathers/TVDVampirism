package org.kuro.tvdvampirism.faction.player;

import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import org.kuro.tvdvampirism.blood.BloodData;

/** Common blood contract exposed by every custom vampire-derived faction. */
public interface ICustomVampirePlayer {

    BloodData getBloodData();

    void addBloodExhaustion(float amount);

    void drinkBlood(int amount, float saturationModifier, IDrinkBloodContext context);

    boolean useBlood(int amount, boolean allowPartial);

    boolean wantsBlood();
}
