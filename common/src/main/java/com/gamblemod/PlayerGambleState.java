package com.gamblemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class PlayerGambleState {

    public BlockPos lastPos = null;
    public boolean wasOnGround = true;
    public int cooldownTicks = 0;

    public ItemStack pendingItem = ItemStack.EMPTY;
    public GambleTier pendingItemTier = null;
    public boolean pendingIsRare = false;
    public int pendingExpiryTicks = 0;

    public boolean hasPendingOffer() {
        return !pendingItem.isEmpty() && pendingExpiryTicks > 0;
    }

    public void clearPending() {
        pendingItem = ItemStack.EMPTY;
        pendingItemTier = null;
        pendingIsRare = false;
        pendingExpiryTicks = 0;
    }
}
