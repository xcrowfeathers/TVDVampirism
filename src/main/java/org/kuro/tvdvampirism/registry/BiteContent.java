package org.kuro.tvdvampirism.registry;

import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.bite.WerewolfBiteEffect;
import org.kuro.tvdvampirism.action.FillBloodBottleAction;

public final class BiteContent {
    private BiteContent() {}
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Tvdvampirism.MODID);
    private static final DeferredRegister<IAction<?>> ACTIONS = DeferredRegister.create(VampirismRegistries.Keys.ACTION, Tvdvampirism.MODID);
    public static final DeferredHolder<MobEffect, WerewolfBiteEffect> WEREWOLF_BITE = EFFECTS.register("werewolf_bite", WerewolfBiteEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> CURE = EFFECTS.register("cure", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xB53242) {});
    public static final DeferredHolder<IAction<?>, FillBloodBottleAction> FILL_BLOOD_BOTTLE = ACTIONS.register("fill_bottle_with_blood", FillBloodBottleAction::new);
    public static void register(IEventBus bus) { EFFECTS.register(bus); ACTIONS.register(bus); }
}
