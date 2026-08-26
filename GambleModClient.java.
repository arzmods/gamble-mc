package com.gamblemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;

public class GambleModClient implements ClientModInitializer {

    private static KeyMapping doubleOrNothingKey;

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(DoubleOrNothingPayload.TYPE, DoubleOrNothingPayload.CODEC);

        doubleOrNothingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.gamblemod.double_or_nothing",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                "category.gamblemod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (doubleOrNothingKey.consumeClick()) {
                ClientPlayNetworking.send(new DoubleOrNothingPayload());
            }
        });
    }
}
