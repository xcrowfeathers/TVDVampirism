package org.kuro.tvdvampirism.mixin;

import de.teamlapen.werewolves.items.LiverItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.kuro.tvdvampirism.blood.BloodManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiverItem.class)
public abstract class LiverItemMixin {

    @Inject(
            method = "finishUsingItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tvdvampirism$finishUsingLiver(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!(entity instanceof Player player)
                || BloodManager.getCustomVampire(player).isEmpty()) {
            return;
        }

        FoodProperties food = stack.getFoodProperties(player);

        if (food == null) {
            return;
        }

        BloodManager.drinkFood(
                player,
                stack,
                food.nutrition(),
                food.saturation()
        );

        stack.consume(1, player);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_BURP,
                SoundSource.PLAYERS,
                0.5F,
                level.random.nextFloat() * 0.1F + 0.9F
        );

        cir.setReturnValue(stack);
    }
}