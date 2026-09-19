package com.gamblemod.neoforge;

import com.gamblemod.DoubleOrNothingPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = GambleModNeoForge.MOD_ID, dist = Dist.CLIENT)
public class GambleModNeoForgeClient {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("gamblemod", "gamble"));

    private static KeyMapping doubleOrNothingKey;

    public GambleModNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        doubleOrNothingKey = new KeyMapping(
                "key.gamblemod.double_or_nothing",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                CATEGORY
        );
        event.register(doubleOrNothingKey);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        while (doubleOrNothingKey.consumeClick()) {
            PacketDistributor.sendToServer(new DoubleOrNothingPayload());
        }
    }
}
