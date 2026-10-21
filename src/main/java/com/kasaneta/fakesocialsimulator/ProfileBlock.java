package com.kasaneta.fakesocialsimulator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ProfileBlock extends Block {

    public ProfileBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            SocialManager manager = SocialManager.get(level.getServer());
            PlayerStats stats = manager.getStats(player.getUUID());

            String name = player.getName().getString();

            serverPlayer.sendSystemMessage(Component.literal("------------------------------"));
            serverPlayer.sendSystemMessage(Component.translatable("message.fakesocialsimulator.profile.title", name));
            serverPlayer.sendSystemMessage(Component.translatable("message.fakesocialsimulator.profile.followers", SocialFormat.compact(stats.getFollowersCount())));
            serverPlayer.sendSystemMessage(Component.translatable("message.fakesocialsimulator.profile.posts", SocialFormat.compact(stats.getPostsCount())));
            serverPlayer.sendSystemMessage(Component.translatable("message.fakesocialsimulator.profile.views", SocialFormat.compact(stats.getViewCount())));
            serverPlayer.sendSystemMessage(Component.translatable("message.fakesocialsimulator.profile.likes", SocialFormat.compact(stats.getLikeCount())));
            serverPlayer.sendSystemMessage(Component.translatable("message.fakesocialsimulator.profile.comments", SocialFormat.compact(stats.getCommentCount())));
            serverPlayer.sendSystemMessage(Component.translatable("message.fakesocialsimulator.profile.following", SocialFormat.compact(stats.getFollowingCount())));
            serverPlayer.sendSystemMessage(Component.literal("------------------------------"));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
