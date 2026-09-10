package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.util.Helper;
import de.teamlapen.vampirism.blocks.GarlicBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.kuro.tvdvampirism.ability.ResistanceSkills;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=Helper.class,remap=false)
public abstract class GarlicHandlingMixin {
    @Inject(method="handleHeldNonVampireItem",at=@At(value="INVOKE",
            target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"),cancellable=true)
    private static void tvd$allowResistantGarlic(ItemStack stack,Entity entity,boolean held,CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof GarlicBlock.GarlicItem && entity instanceof LivingEntity living && ResistanceSkills.garlic(living)) cir.setReturnValue(false);
    }
}
