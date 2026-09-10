package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.items.VampireBloodBottleItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.kuro.tvdvampirism.blood.AugustineFeedingPolicy;
import org.kuro.tvdvampirism.blood.BloodManager;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.spongepowered.asm.mixin.Mixin;

/** This stock item has no use methods; only Augustine gains a drinking action. */
@Mixin(VampireBloodBottleItem.class)
public abstract class VampireBloodBottleItemMixin extends Item {
    protected VampireBloodBottleItemMixin(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return AugustineFeedingPolicy.isAugustine(player)
                ? ItemUtils.startUsingInstantly(level, player, hand) : super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return entity instanceof Player player && AugustineFeedingPolicy.isAugustine(player)
                ? 32 : super.getUseDuration(stack, entity);
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player) || !AugustineFeedingPolicy.isAugustine(player))
            return super.finishUsingItem(stack, level, entity);
        if (player instanceof ServerPlayer) {
            BloodManager.drinkFood(player, stack, ServerConfig.AUGUSTINE_VAMPIRE_BOTTLE_GAIN.get(), 1.0F);
            return ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }
}
