package einstein.jmc.blocks;

import java.util.Random;

import einstein.einsteins_library.blocks.CakeBlockBase;
import einstein.jmc.init.ModConfigs.ModClientConfigs;
import einstein.jmc.init.ModConfigs.ModServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LavaCakeBlock extends CakeBlockBase
{
    public LavaCakeBlock(final Block.Properties properties) {
        super(properties);
        this.setDefaultState((this.stateContainer.getBaseState()).with(LavaCakeBlock.BITES, 0));
    }
    
    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            final ItemStack itemstack = player.getHeldItem(handIn);
            if (this.eatSlice(worldIn, pos, state, player) == ActionResultType.SUCCESS) {
                return ActionResultType.SUCCESS;
            }
            if (itemstack.isEmpty()) {
                return ActionResultType.CONSUME;
            }
        }
        return this.eatSlice(worldIn, pos, state, player);
    }
    
    private ActionResultType eatSlice(final IWorld worldIn, final BlockPos pos, final BlockState state, final PlayerEntity playerIn) {
        if (!playerIn.canEat(false)) {
            return ActionResultType.PASS;
        }
        playerIn.addStat(Stats.EAT_CAKE_SLICE);
        playerIn.getFoodStats().addStats(2, 0.1f);
        playerIn.addPotionEffect(new EffectInstance(Effects.REGENERATION, ModServerConfigs.LAVA_CAKE_REGEN_DUR.get(), ModServerConfigs.LAVA_CAKE_REGEN_STRENGTH.get()));
        playerIn.addPotionEffect(new EffectInstance(Effects.STRENGTH, ModServerConfigs.LAVA_CAKE_STRENGTH_DUR.get(), ModServerConfigs.LAVA_CAKE_STRENGTH_STRENGTH.get()));
        final int i = state.get(LavaCakeBlock.BITES);
        if (i < 6) { // Number must be same as BITES
            worldIn.setBlockState(pos, state.with(LavaCakeBlock.BITES, (i + 1)), 3);
        }
        else {
            worldIn.removeBlock(pos, false);
        }
        return ActionResultType.SUCCESS;
    }
    
    public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn) {
        if (!entityIn.isImmuneToFire() && entityIn instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entityIn)) {
            entityIn.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0f);
        }
        super.onEntityWalk(worldIn, pos, entityIn);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
    	if (ModClientConfigs.LAVA_CAKE_PARTICLES.get()) {
        	if (rand.nextInt(10) == 0) {
    	        double d0 = pos.getX() + rand.nextDouble();
    	        double d1 = pos.getY() + 1.0D;
    	        double d2 = pos.getZ() + rand.nextDouble();
    	    	worldIn.addParticle(ParticleTypes.LAVA, d0, d1, d2, 0, 0, 0);
        	}
    	}
    }
}
