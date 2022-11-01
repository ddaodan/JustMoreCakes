package einstein.jmc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BaseEntityCandleCakeBlock<T extends BlockEntity> extends BaseCandleCakeBlock implements EntityBlock {

    protected final BlockEntityType.BlockEntitySupplier<T> blockEntity;

    public BaseEntityCandleCakeBlock(BaseCakeBlock originalCake, Properties properties, BlockEntityType.BlockEntitySupplier<T> blockEntity) {
        super(originalCake, properties);
        this.blockEntity = blockEntity;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntity.create(pos, state);
    }
}