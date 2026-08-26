package com.gamblemod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.boss.wither.Wither;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Random;

public final class GambleEngine {

    private static final Random RANDOM = new Random();

    private GambleEngine() {}

    public static ItemStack rollAndApply(ServerPlayer player, GambleTier[] outTier) {
        GambleTier tier = GambleTier.roll(RANDOM);
        outTier[0] = tier;
        switch (tier) {
            case DEATH -> {
                broadcast(player, player.getGameProfile().getName() + " rolled a 0.1% and lost the ultimate gamble.");
                instantKill(player);
                return ItemStack.EMPTY;
            }
            case HORRIBLE -> {
                sendTitle(player, "HORRIBLE LUCK!", 0xAA0000);
                applyHorribleEffect(player);
                return ItemStack.EMPTY;
            }
            case JACKPOT -> {
                sendTitle(player, "JACKPOT!", 0xFFD700);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
                ItemStack jackpotItem = randomJackpotItem();
                giveItem(player, jackpotItem);
                return jackpotItem;
            }
            case BAD -> {
                applyBadEffect(player);
                return ItemStack.EMPTY;
            }
            case GOOD -> {
                boolean rare = RANDOM.nextDouble() < 0.15;
                ItemStack goodItem = randomGearItem(rare);
                giveItem(player, goodItem);
                return goodItem;
            }
            case NOTHING -> {
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void resolveDoubleOrNothing(ServerPlayer player, PlayerGambleState state) {
        boolean win = RANDOM.nextBoolean();
        ItemStack item = state.pendingItem;
        GambleTier tier = state.pendingItemTier;
        boolean rare = state.pendingIsRare;
        state.clearPending();

        if (win) {
            giveItem(player, item.copy());
            sendTitle(player, "DOUBLED!", 0x55FF55);
            return;
        }

        if (tier == GambleTier.JACKPOT) {
            broadcast(player, player.getGameProfile().getName()
                    + " got greedy trying to double a Jackpot item and lost everything.");
            instantKill(player);
            return;
        }

        sendTitle(player, "BUSTED!", 0xAA0000);
        if (rare) {
            applyHorribleEffect(player);
        } else {
            applyBadEffect(player);
        }
    }

    private static void instantKill(ServerPlayer player) {
        player.kill((ServerLevel) player.level());
    }

    private static void applyHorribleEffect(ServerPlayer player) {
        int pick = RANDOM.nextInt(3);
        switch (pick) {
            case 0 -> spawnWitherOnHead(player);
            case 1 -> deleteHalfInventory(player);
            default -> lavaTrap(player);
        }
    }

    private static void applyBadEffect(ServerPlayer player) {
        int pick = RANDOM.nextInt(3);
        switch (pick) {
            case 0 -> spawnZombieHorde(player);
            case 1 -> zapHealth(player, 6.0f);
            default -> tntExplosion(player);
        }
    }

    private static void spawnWitherOnHead(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        Wither wither = new Wither(EntityType.WITHER, level);
        wither.moveTo(player.getX(), player.getY() + 2.0, player.getZ(), 0, 0);
        level.addFreshEntity(wither);
    }

    private static void deleteHalfInventory(ServerPlayer player) {
        var inventory = player.getInventory();
        List<Integer> occupied = new java.util.ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                occupied.add(i);
            }
        }
        java.util.Collections.shuffle(occupied, RANDOM);
        int toRemove = occupied.size() / 2;
        for (int i = 0; i < toRemove; i++) {
            inventory.setItem(occupied.get(i), ItemStack.EMPTY);
        }
    }

    private static void lavaTrap(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos center = player.blockPosition().below();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlockAndUpdate(center.offset(dx, 0, dz), Blocks.LAVA.defaultBlockState());
            }
        }
    }

    private static void spawnZombieHorde(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        for (int i = 0; i < 5; i++) {
            Zombie zombie = new Zombie(EntityType.ZOMBIE, level);
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = 2.0 + RANDOM.nextDouble() * 2.0;
            zombie.moveTo(player.getX() + Math.cos(angle) * dist, player.getY(),
                    player.getZ() + Math.sin(angle) * dist, 0, 0);
            level.addFreshEntity(zombie);
        }
    }

    private static void zapHealth(ServerPlayer player, float amount) {
        ServerLevel level = (ServerLevel) player.level();
        player.hurtServer(level, player.damageSources().magic(), amount);
    }

    private static void tntExplosion(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        level.explode(player, player.getX(), player.getY(), player.getZ(), 3.0f, Level.ExplosionInteraction.TNT);
    }

    private static ItemStack randomGearItem(boolean rare) {
        ItemStack[] iron = new ItemStack[] {
                new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_HELMET),
                new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_LEGGINGS),
                new ItemStack(Items.IRON_BOOTS)
        };
        ItemStack[] diamond = new ItemStack[] {
                new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.DIAMOND_PICKAXE),
                new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS)
        };
        ItemStack[] pool = rare ? diamond : iron;
        return pool[RANDOM.nextInt(pool.length)].copy();
    }

    private static ItemStack randomJackpotItem() {
        ItemStack[] pool = new ItemStack[] {
                new ItemStack(Items.MACE),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),
                new ItemStack(Items.TOTEM_OF_UNDYING)
        };
        return pool[RANDOM.nextInt(pool.length)].copy();
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack.copy())) {
            player.drop(stack.copy(), false);
        }
    }

    private static void broadcast(ServerPlayer player, String message) {
        player.server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    static void sendTitle(ServerPlayer player, String title, int color) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal(title).withStyle(style -> style.withColor(color))));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("")));
    }
}
