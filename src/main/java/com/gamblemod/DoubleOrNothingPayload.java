package com.gamblemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DoubleOrNothingPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DoubleOrNothingPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("gamblemod", "double_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DoubleOrNothingPayload> CODEC =
            StreamCodec.unit(new DoubleOrNothingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
