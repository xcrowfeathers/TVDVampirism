package org.kuro.tvdvampirism.ability;

import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModDamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import org.kuro.tvdvampirism.skill.CustomSkills;
import org.kuro.tvdvampirism.config.ServerConfig;

@EventBusSubscriber(modid="tvdvampirism")
public final class ResistanceSkills {
    private ResistanceSkills() {}
    public static boolean garlic(LivingEntity entity) { return CustomSkills.has(entity,CustomSkills.Definition.GARLIC); }
    public static float holyWaterMultiplier(LivingEntity entity) {
        return CustomSkills.has(entity,CustomSkills.Definition.HOLY_WATER) ? 1-ServerConfig.HOLY_WATER_RESISTANCE.get().floatValue() : 1;
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Pre event) {
        if (event.getSource().is(ModDamageTypes.HOLY_WATER)) event.setNewDamage(event.getNewDamage()*holyWaterMultiplier(event.getEntity()));
    }
    @SubscribeEvent public static void effect(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().is(ModEffects.GARLIC) && garlic(event.getEntity()))
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }
}
