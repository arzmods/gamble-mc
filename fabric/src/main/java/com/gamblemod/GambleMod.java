package com.gamblemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class GambleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(DoubleOrNothingPayload.TYPE, DoubleOrNothingPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DoubleOrNothingPayload.TYPE, (payload, context) ->
                GambleCore.handleDoubleOrNothing(context.player()));

        ServerTickEvents.END_SERVER_TICK.register(GambleCore::tickServer);
    }
}
