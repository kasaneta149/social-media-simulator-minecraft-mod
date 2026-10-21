package com.kasaneta.fakesocialsimulator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class SocialBlockEntity extends BlockEntity {
    private UUID ownerUUID;
    private long lastTriggerTime = 0;

    public SocialBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOCIAL_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean canTrigger(long currentTime) {
        if (currentTime - lastTriggerTime >= 150) { 
            this.lastTriggerTime = currentTime;
            return true;
        }
        return false;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (ownerUUID != null) {
            tag.putUUID("owner", ownerUUID);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID("owner")) {
            this.ownerUUID = tag.getUUID("owner");
        }
    }
}