package com.gamblemod;
public enum GambleTier {
    DEATH(0.1),
    HORRIBLE(5.0),
    JACKPOT(5.0),
    BAD(25.0),
    GOOD(25.0),
    NOTHING(39.9);

    public final double chance;

    GambleTier(double chance) {
        this.chance = chance;
    }

    public static GambleTier roll(java.util.Random random) {
        double r = random.nextDouble() * 100.0;
        double cumulative = 0.0;
        for (GambleTier tier : values()) {
            cumulative += tier.chance;
            if (r < cumulative) {
                return tier;
            }
        }
        return NOTHING;
    }
}
