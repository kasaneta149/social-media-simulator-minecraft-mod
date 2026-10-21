package com.kasaneta.fakesocialsimulator;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, SocialStatsMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SocialStatsMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SocialStatsMod.MODID);

    private static final BlockBehaviour.Properties SOCIAL_PROPS = BlockBehaviour.Properties.of()
            .sound(SoundType.METAL)
            .strength(2.0f, 5.0f)
            .requiresCorrectToolForDrops();

    public static final Supplier<Block> SUBSCRIPTION_BLOCK = BLOCKS.register("subscription_manager", 
            () -> new SubscriptionBlock(SOCIAL_PROPS));
    public static final Supplier<Item> SUBSCRIPTION_BLOCK_ITEM = ITEMS.register("subscription_manager",
            () -> new BlockItem(SUBSCRIPTION_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Block> POST_BLOCK = BLOCKS.register("post_manager", 
            () -> new PostBlock(SOCIAL_PROPS));
    public static final Supplier<Item> POST_BLOCK_ITEM = ITEMS.register("post_manager",
            () -> new BlockItem(POST_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Block> PROFILE_BLOCK = BLOCKS.register("profile_block", 
            () -> new ProfileBlock(SOCIAL_PROPS));
    public static final Supplier<Item> PROFILE_BLOCK_ITEM = ITEMS.register("profile_block",
            () -> new BlockItem(PROFILE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<SocialBlockEntity>> SOCIAL_BLOCK_ENTITY = BLOCK_ENTITIES.register("social_block_entity",
            () -> BlockEntityType.Builder.of(SocialBlockEntity::new, SUBSCRIPTION_BLOCK.get(), POST_BLOCK.get()).build(null));
}