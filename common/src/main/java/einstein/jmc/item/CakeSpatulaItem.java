package einstein.jmc.item;

import einstein.jmc.block.cake.BaseCakeBlock;
import einstein.jmc.block.cake.ThreeTieredCakeBlock;
import einstein.jmc.data.packs.ModBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CakeSpatulaItem extends Item {

    public CakeSpatulaItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            ItemStack stack = context.getItemInHand();
            Block block = state.getBlock();
            if (state.is(ModBlockTags.CAKE_SPATULA_USABLE)) {
                if (!BaseCakeBlock.isUneaten(state, pos, level)) {
                    return InteractionResult.PASS;
                }

                if (!level.isClientSide) {
                    level.destroyBlock(pos, false, player);

                    // TODO look into swapping with ThreeTieredCakeBlock.destroyOppositeHalf()
                    if (block instanceof ThreeTieredCakeBlock) {
                        boolean isLower = state.getValue(ThreeTieredCakeBlock.HALF) == DoubleBlockHalf.LOWER;
                        BlockPos otherPos = isLower ? pos.above() : pos.below();
                        BlockState otherState = level.getBlockState(otherPos);

                        if (otherState.getValue(ThreeTieredCakeBlock.HALF) == (isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)) {
                            level.destroyBlock(isLower ? pos.above() : pos.below(), false, player);
                        }
                    }

                    stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(context.getHand()));
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);

                    if (!player.isCreative()) {
                        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                        Block.dropResources(state, level, pos, blockEntity, player, stack);
                    }
                }

                player.awardStat(Stats.BLOCK_MINED.get(block));
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.jmc.cake_spatula.desc").withStyle(ChatFormatting.GRAY));
    }
}
