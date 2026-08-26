package com.gamblemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class GambleModClient implements ClientModInitializer {

    private static KeyMapping doubleOrNothingKey;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("gamblemod", "category")
    );

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.serverboundPlay().register(DoubleOrNothingPayload.TYPE, DoubleOrNothingPayload.CODEC);

        doubleOrNothingKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.gamblemod.double_or_nothing",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (doubleOrNothingKey.consumeClick()) {
                ClientPlayNetworking.send(new DoubleOrNothingPayload());
            }
        });
    }
}
