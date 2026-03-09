package com.imt.api_invocations.utils;

import java.util.concurrent.ThreadLocalRandom;

import com.imt.api_invocations.enums.Rank;

public class Random {
    private Random() {
    }

    public static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    public static Rank getRandomRankBasedOnAvailableData(DataServiceInterface dataService) {
        // 1. Calculer la somme des taux des rangs encore DISPONIBLES
        float totalAvailableRate = 0f;
        for (Rank rank : Rank.values()) {
            if (dataService.hasAvailableData(rank)) { // La méthode de vérification de stock de monstres
                totalAvailableRate += rank.getDropRate();
            }
        }

        if (totalAvailableRate <= 0)
            return null; // Plus aucun monstre du tout !

        // 2. Tirage entre 0 et totalAvailableRate
        float randomValue = randomFloat() * totalAvailableRate;
        float cumulativeRate = 0f;

        for (Rank rank : Rank.values()) {
            if (dataService.hasAvailableData(rank)) {
                cumulativeRate += rank.getDropRate();
                if (randomValue <= cumulativeRate) {
                    return rank;
                }
            }
        }
        return Rank.LEGENDARY; // Fallback, should not reach here
    }

    public static float getRealDropRateForRank(Rank rank, DataServiceInterface dataService) {
        float totalAvailableRate = 0f;
        for (Rank r : Rank.values()) {
            if (dataService.hasAvailableData(r)) {
                totalAvailableRate += r.getDropRate();
            }
        }
        if (!dataService.hasAvailableData(rank) || totalAvailableRate <= 0)
            return 0f;
        return rank.getDropRate() / totalAvailableRate;
    }
}