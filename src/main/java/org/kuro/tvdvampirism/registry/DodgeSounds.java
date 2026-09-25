package org.kuro.tvdvampirism.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.kuro.tvdvampirism.Tvdvampirism;

public final class DodgeSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, Tvdvampirism.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> DODGE = SOUNDS.register("dodge", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Tvdvampirism.MODID, "dodge")));

    private DodgeSounds() {}

    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
