package com.gamblemod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GambleMod implements ModInitializer {

    private static final int ROLL_INTERVAL_TICKS = 2 * 60 * 20;
    private static final int OFFER_WINDOW_TICKS = 100;

    private final Map<UUID, PlayerGambleState> states = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(DoubleOrNothingPayload.TYPE, DoubleOrNothingPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DoubleOrNothingPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            PlayerGambleState state = states.get(player.getUUID());
            if (state != null && state.hasPendingOffer()) {
                GambleEngine.resolveDoubleOrNothing(player, state);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickPlayer(player);
            }
        });
    }

    private void tickPlayer(ServerPlayer player) {
        PlayerGambleState state = states.computeIfAbsent(player.getUUID(), id -> {
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

    private boolean isDiamondItem(ItemStack stack) {
        return stack.is(Items.DIAMOND_SWORD) || stack.is(Items.DIAMOND_PICKAXE)
                || stack.is(Items.DIAMOND_AXE) || stack.is(Items.DIAMOND_HELMET)
                || stack.is(Items.DIAMOND_CHESTPLATE) || stack.is(Items.DIAMOND_LEGGINGS)
                || stack.is(Items.DIAMOND_BOOTS);
    }
}
