package com.gamblemod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Loader-independent gambling logic. Fabric and NeoForge both hook their own
 * server tick / networking events into this class so the mod behaves identically
 * on either loader.
 */
public final class GambleCore {

    private static final int ROLL_INTERVAL_TICKS = 2 * 60 * 20;
    private static final int OFFER_WINDOW_TICKS = 100;

    private static final Map<UUID, PlayerGambleState> STATES = new HashMap<>();

    private GambleCore() {}

    /** Called once per server tick by the loader-specific entrypoint. */
    public static void tickServer(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    /** Called when a client sends the "double or nothing" packet. */
    public static void handleDoubleOrNothing(ServerPlayer player) {
        PlayerGambleState state = STATES.get(player.getUUID());
        if (state != null && state.hasPendingOffer()) {
            GambleEngine.resolveDoubleOrNothing(player, state);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        PlayerGambleState state = STATES.computeIfAbsent(player.getUUID(), id -> {
            PlayerGambleState fresh = new PlayerGambleState();
            fresh.cooldownTicks = ROLL_INTERVAL_TICKS;
            return fresh;
        });

        if (state.hasPendingOffer()) {
            state.pendingExpiryTicks--;
            if (state.pendingExpiryTicks <= 0) {
                state.clearPending();
            }
        }

        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
            return;
        }

        state.cooldownTicks = ROLL_INTERVAL_TICKS;

        GambleTier[] outTier = new GambleTier[1];
        ItemStack awarded = GambleEngine.rollAndApply(player, outTier);

        if (!awarded.isEmpty() && (outTier[0] == GambleTier.GOOD || outTier[0] == GambleTier.JACKPOT)) {
            boolean rare = outTier[0] == GambleTier.GOOD && isDiamondItem(awarded);
            state.pendingItem = awarded.copy();
            state.pendingItemTier = outTier[0];
            state.pendingIsRare = rare;
            state.pendingExpiryTicks = OFFER_WINDOW_TICKS;
            player.sendSystemMessage(Component.literal("Press [G] to Double or Nothing!"));
        }
    }

    private static boolean isDiamondItem(ItemStack stack) {
        return stack.is(Items.DIAMOND_SWORD) || stack.is(Items.DIAMOND_PICKAXE)
                || stack.is(Items.DIAMOND_AXE) || stack.is(Items.DIAMOND_HELMET)
                || stack.is(Items.DIAMOND_CHESTPLATE) || stack.is(Items.DIAMOND_LEGGINGS)
                || stack.is(Items.DIAMOND_BOOTS);
    }
}
