package com.kasaneta.fakesocialsimulator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SubscriptionBlock extends Block implements EntityBlock {
    public static final BooleanProperty MODE_DELETE = BooleanProperty.create("mode_delete");

    public SubscriptionBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MODE_DELETE, false)
                .setValue(BlockStateProperties.POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE_DELETE, BlockStateProperties.POWERED);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SocialBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof SocialBlockEntity sbe) {
            sbe.setOwnerUUID(player.getUUID());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            boolean isDelete = state.getValue(MODE_DELETE);
            level.setBlock(pos, state.setValue(MODE_DELETE, !isDelete), 3);
            player.displayClientMessage(Component.translatable(!isDelete ? "message.fakesocialsimulator.subscription_mode.unfollow" : "message.fakesocialsimulator.subscription_mode.follow"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            boolean isPowered = state.getValue(BlockStateProperties.POWERED);

            if (hasSignal && !isPowered) {
                level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, true), 3);
                if (level.getBlockEntity(pos) instanceof SocialBlockEntity sbe) {
                    if (sbe.canTrigger(System.currentTimeMillis())) {
                        triggerAction(level, pos, state, sbe);
                    }
                }
            } else if (!hasSignal && isPowered) {
                level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, false), 3);
            }
        }
    }

    private void triggerAction(Level level, BlockPos pos, BlockState state, SocialBlockEntity sbe) {
        if (sbe.getOwnerUUID() == null) return;

        UUID owner = sbe.getOwnerUUID();
        boolean isDelete = state.getValue(MODE_DELETE);
        SocialManager manager = SocialManager.get(level.getServer());
        PlayerStats stats = manager.getStats(owner);

        if (isDelete) {
            if (stats.getFollowingCount() > 0) {
                stats.decrementFollowing();

                double unfollowBackChance = Config.UNFOLLOW_BACK_CHANCE.get();
                if (level.random.nextDouble() < unfollowBackChance) {
                    stats.addPendingFollowers(-1);
                }

                if (Config.PUNISH_SPAMMERS.get()) {
                    stats.addDeletionSpam(0.2);
                }
            }
        } else {
            int maxFollowing = Config.MAX_FOLLOWING_COUNT.get();
            if (stats.getFollowingCount() < maxFollowing) {
                stats.incrementFollowing();
                SocialAlgorithm.onFollow(stats, System.currentTimeMillis());
            }
        }

        manager.setDirty();
    }
}
