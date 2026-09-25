package org.kuro.tvdvampirism.faction.player;

import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import org.kuro.tvdvampirism.blood.BloodData;

public interface ICustomVampirePlayer {

    BloodData getBloodData();

    void addBloodExhaustion(float amount);

    void drinkBlood(int amount, float saturationModifier, IDrinkBloodContext context);

    boolean useBlood(int amount, boolean allowPartial);

    boolean wantsBlood();
}
