package com.gamblemod.neoforge;

import com.gamblemod.DoubleOrNothingPayload;
import com.gamblemod.GambleCore;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(GambleModNeoForge.MOD_ID)
public class GambleModNeoForge {

    public static final String MOD_ID = "gamblemod";

    public GambleModNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                DoubleOrNothingPayload.TYPE,
                DoubleOrNothingPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        GambleCore.handleDoubleOrNothing(serverPlayer);
                    }
                })
        );
    }

    private void onServerTick(ServerTickEvent.Post event) {
        GambleCore.tickServer(event.getServer());
    }
}
