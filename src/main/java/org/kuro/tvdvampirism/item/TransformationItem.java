package org.kuro.tvdvampirism.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.kuro.tvdvampirism.player.TransformationManager;
import org.kuro.tvdvampirism.player.TransformationManager.Reagent;
import org.kuro.tvdvampirism.bite.WerewolfBiteManager;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class TransformationItem extends Item {
    private final Reagent reagent;

    public TransformationItem(Reagent reagent, Properties properties) {
        super(properties);
        this.reagent = reagent;
    }

    private boolean canConsume(Player player) {
        return TransformationManager.canStart(player, reagent)
                || reagent == Reagent.ORIGINAL_HYBRID_BLOOD && WerewolfBiteManager.canCureWithBlood(player);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!canConsume(player)) return InteractionResultHolder.fail(player.getItemInHand(hand));
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) {
        return reagent == Reagent.AUGUSTINE_SYRINGE ? UseAnim.BOW : UseAnim.DRINK;
    }

    @Override public boolean isFoil(ItemStack stack) {
        return reagent == Reagent.INVINCIBILITY_CURSE || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            java.util.List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(getDescriptionId() + ".desc")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !canConsume(player)) return stack;
        boolean cured = reagent == Reagent.ORIGINAL_HYBRID_BLOOD && WerewolfBiteManager.cureWithBlood(player);
        boolean started = TransformationManager.start(player, reagent);
        if (!started && !cured) return stack;
        if (reagent == Reagent.AUGUSTINE_SYRINGE) {
            stack.consume(1, player);
            return stack;
        }
        return ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
    }
}
