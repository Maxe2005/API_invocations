package com.imt.api_invocations.utils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.enums.Rank;

@ExtendWith(MockitoExtension.class)
@DisplayName("Random - Tests Unitaires")
class RandomTest {

    @Mock
    private DataServiceInterface dataService;

    @Nested
    @DisplayName("Tests de la méthode random(min, max)")
    class RandomIntTests {

        @Test
        @DisplayName("Doit retourner une valeur dans la plage spécifiée")
        void should_ReturnValueInRange_When_RandomCalled() {
            // Arrange
            int min = 10;
            int max = 20;

            // Act
            int result = Random.random(min, max);

            // Assert
            assertThat(result).isBetween(min, max);
        }

        @Test
        @DisplayName("Doit retourner la même valeur quand min == max")
        void should_ReturnSameValue_When_MinEqualsMax() {
            // Arrange
            int value = 42;

            // Act
            int result = Random.random(value, value);

            // Assert
            assertThat(result).isEqualTo(value);
        }

        @Test
        @DisplayName("Doit avoir une distribution uniforme sur plusieurs appels")
        void should_HaveUniformDistribution_When_CalledMultipleTimes() {
            // Arrange
            int min = 1;
            int max = 5;
            int iterations = 10000;
            Map<Integer, Integer> distribution = new HashMap<>();

            // Act
            for (int i = 0; i < iterations; i++) {
                int value = Random.random(min, max);
                distribution.put(value, distribution.getOrDefault(value, 0) + 1);
            }

            // Assert
            // Chaque valeur devrait apparaître environ iterations/5 fois (20%)
            // On tolère une variation de +/- 30% pour éviter les faux négatifs
            int expectedCount = iterations / 5;
            int tolerance = (int) (expectedCount * 0.3);

            for (int i = min; i <= max; i++) {
                assertThat(distribution.get(i)).isCloseTo(expectedCount, within(tolerance));
            }
        }

        @Test
        @DisplayName("Doit prendre en charge les valeurs négatives")
        void should_SupportNegativeValues_When_RandomCalled() {
            // Arrange
            int min = -10;
            int max = -5;

            // Act
            int result = Random.random(min, max);

            // Assert
            assertThat(result).isBetween(min, max);
        }

        @Test
        @DisplayName("Doit prendre en charge les plages incluant zéro")
        void should_SupportZeroInRange_When_RandomCalled() {
            // Arrange
            int min = -5;
            int max = 5;

            // Act
            int result = Random.random(min, max);

            // Assert
            assertThat(result).isBetween(min, max);
        }
    }

    @Nested
    @DisplayName("Tests de la méthode randomFloat()")
    class RandomFloatTests {

        @Test
        @DisplayName("Doit retourner une valeur entre 0 et 1")
        void should_ReturnValueBetween0And1_When_RandomFloatCalled() {
            // Act
            float result = Random.randomFloat();

            // Assert
            assertThat(result).isBetween(0.0f, 1.0f);
        }

        @Test
        @DisplayName("Doit retourner des valeurs variées sur plusieurs appels")
        void should_ReturnVariedValues_When_CalledMultipleTimes() {
            // Arrange
            int iterations = 100;

            // Act
            long uniqueValues = java.util.stream.IntStream.range(0, iterations)
                    .mapToObj(i -> Random.randomFloat()).distinct().count();

            // Assert
            // Vérifier qu'il y a de la variété (au moins 50 valeurs différentes)
            assertThat(uniqueValues).isGreaterThan(50);
        }
    }

    @Nested
    @DisplayName("Tests de la méthode getRandomRankBasedOnAvailableData()")
    class GetRandomRankBasedOnAvailableDataTests {

        @Test
        @DisplayName("Doit respecter les probabilités configurées")
        void should_RespectConfiguredProbabilities_When_AllRanksAvailable() {
            // Arrange
            when(dataService.hasAvailableData(any(Rank.class))).thenReturn(true);

            int iterations = 100000;
            Map<Rank, Integer> distribution = new EnumMap<>(Rank.class);

            // Act
            for (int i = 0; i < iterations; i++) {
                Rank rank = Random.getRandomRankBasedOnAvailableData(dataService);
                distribution.put(rank, distribution.getOrDefault(rank, 0) + 1);
            }

            // Assert
            // Vérifier que chaque rank apparaît selon son taux de drop (avec tolérance de 2%)
            for (Rank rank : Rank.values()) {
                float expectedRate = rank.getDropRate();
                int expectedCount = (int) (iterations * expectedRate);
                int actualCount = distribution.getOrDefault(rank, 0);

                // Tolérance de 2% de la distribution totale
                int tolerance = (int) (iterations * 0.02);

                assertThat(actualCount).withFailMessage(
                        "Le rank %s devrait apparaître environ %d fois (taux %.2f%%), mais est apparu %d fois",
                        rank, expectedCount, expectedRate * 100, actualCount)
                        .isCloseTo(expectedCount, within(tolerance));
            }
        }

        @Test
        @DisplayName("Doit ignorer les ranks sans data disponible")
        void should_SkipRanksWithoutData_When_SomeRanksUnavailable() {
            // Arrange - Seuls COMMON et RARE sont disponibles
            when(dataService.hasAvailableData(Rank.COMMON)).thenReturn(true);
            when(dataService.hasAvailableData(Rank.RARE)).thenReturn(true);
            when(dataService.hasAvailableData(Rank.EPIC)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.LEGENDARY)).thenReturn(false);

            int iterations = 10000;
            Map<Rank, Integer> distribution = new EnumMap<>(Rank.class);

            // Act
            for (int i = 0; i < iterations; i++) {
                Rank rank = Random.getRandomRankBasedOnAvailableData(dataService);
                distribution.put(rank, distribution.getOrDefault(rank, 0) + 1);
            }

            // Assert
            assertThat(distribution).containsOnlyKeys(Rank.COMMON, Rank.RARE)
                    .doesNotContainKeys(Rank.EPIC, Rank.LEGENDARY);

            // Les probabilités sont recalculées dynamiquement
            // COMMON: 0.50, RARE: 0.30 → total = 0.80
            // Nouvelle distribution: COMMON = 0.50/0.80 = 62.5%, RARE = 0.30/0.80 = 37.5%
            float totalAvailableRate = Rank.COMMON.getDropRate() + Rank.RARE.getDropRate();

            int commonExpected =
                    (int) (iterations * (Rank.COMMON.getDropRate() / totalAvailableRate));
            int rareExpected = (int) (iterations * (Rank.RARE.getDropRate() / totalAvailableRate));

            int tolerance = (int) (iterations * 0.02);

            assertThat(distribution.get(Rank.COMMON)).isCloseTo(commonExpected, within(tolerance));
            assertThat(distribution.get(Rank.RARE)).isCloseTo(rareExpected, within(tolerance));
        }

        @Test
        @DisplayName("Doit retourner null si aucun rank disponible")
        void should_ReturnNull_When_NoRankAvailable() {
            // Arrange
            when(dataService.hasAvailableData(any(Rank.class))).thenReturn(false);

            // Act
            Rank result = Random.getRandomRankBasedOnAvailableData(dataService);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Doit retourner le seul rank disponible si un seul existe")
        void should_ReturnOnlyAvailableRank_When_OnlyOneRankAvailable() {
            // Arrange - Seul LEGENDARY est disponible
            when(dataService.hasAvailableData(Rank.COMMON)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.RARE)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.EPIC)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.LEGENDARY)).thenReturn(true);

            // Act
            Rank result = Random.getRandomRankBasedOnAvailableData(dataService);

            // Assert
            assertThat(result).isEqualTo(Rank.LEGENDARY);
        }

        @Test
        @DisplayName("Doit vérifier la disponibilité de chaque rank")
        void should_CheckAvailabilityForEachRank_When_GetRandomRankCalled() {
            // Arrange
            when(dataService.hasAvailableData(any(Rank.class))).thenReturn(true);

            // Act
            Random.getRandomRankBasedOnAvailableData(dataService);

            // Assert
            verify(dataService, atLeastOnce()).hasAvailableData(Rank.COMMON);
            verify(dataService, atLeastOnce()).hasAvailableData(Rank.RARE);
            verify(dataService, atLeastOnce()).hasAvailableData(Rank.EPIC);
            verify(dataService, atLeastOnce()).hasAvailableData(Rank.LEGENDARY);
        }
    }

    @Nested
    @DisplayName("Tests de la méthode getRealDropRateForRank()")
    class GetRealDropRateForRankTests {

        @Test
        @DisplayName("Doit retourner le taux réel quand tous les ranks sont disponibles")
        void should_ReturnTheoreticalRate_When_AllRanksAvailable() {
            // Arrange
            when(dataService.hasAvailableData(any(Rank.class))).thenReturn(true);

            // Act
            float commonRate = Random.getRealDropRateForRank(Rank.COMMON, dataService);
            float rareRate = Random.getRealDropRateForRank(Rank.RARE, dataService);
            float epicRate = Random.getRealDropRateForRank(Rank.EPIC, dataService);
            float legendaryRate = Random.getRealDropRateForRank(Rank.LEGENDARY, dataService);

            // Assert - Les taux devraient être identiques aux taux configurés
            assertThat(commonRate).isEqualTo(Rank.COMMON.getDropRate());
            assertThat(rareRate).isEqualTo(Rank.RARE.getDropRate());
            assertThat(epicRate).isEqualTo(Rank.EPIC.getDropRate());
            assertThat(legendaryRate).isEqualTo(Rank.LEGENDARY.getDropRate());
        }

        @Test
        @DisplayName("Doit recalculer les taux quand certains ranks ne sont pas disponibles")
        void should_RecalculateRates_When_SomeRanksUnavailable() {
            // Arrange - Seuls COMMON et RARE sont disponibles
            when(dataService.hasAvailableData(Rank.COMMON)).thenReturn(true);
            when(dataService.hasAvailableData(Rank.RARE)).thenReturn(true);
            when(dataService.hasAvailableData(Rank.EPIC)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.LEGENDARY)).thenReturn(false);

            // Act
            float commonRate = Random.getRealDropRateForRank(Rank.COMMON, dataService);
            float rareRate = Random.getRealDropRateForRank(Rank.RARE, dataService);

            // Assert
            // Total disponible = 0.50 (COMMON) + 0.30 (RARE) = 0.80
            // COMMON réel = 0.50 / 0.80 = 0.625
            // RARE réel = 0.30 / 0.80 = 0.375
            assertThat(commonRate).isCloseTo(0.625f, within(0.001f));
            assertThat(rareRate).isCloseTo(0.375f, within(0.001f));
        }

        @Test
        @DisplayName("Doit retourner 0 si le rank demandé n'est pas disponible")
        void should_ReturnZero_When_RequestedRankNotAvailable() {
            // Arrange
            when(dataService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(dataService.hasAvailableData(Rank.EPIC)).thenReturn(false);

            // Act
            float epicRate = Random.getRealDropRateForRank(Rank.EPIC, dataService);

            // Assert
            assertThat(epicRate).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("Doit retourner 0 si aucun rank n'est disponible")
        void should_ReturnZero_When_NoRankAvailable() {
            // Arrange
            when(dataService.hasAvailableData(any(Rank.class))).thenReturn(false);

            // Act
            float commonRate = Random.getRealDropRateForRank(Rank.COMMON, dataService);

            // Assert
            assertThat(commonRate).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("Doit retourner 1.0 si seul le rank demandé est disponible")
        void should_ReturnOne_When_OnlyRequestedRankAvailable() {
            // Arrange - Seul LEGENDARY est disponible
            when(dataService.hasAvailableData(Rank.COMMON)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.RARE)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.EPIC)).thenReturn(false);
            when(dataService.hasAvailableData(Rank.LEGENDARY)).thenReturn(true);

            // Act
            float legendaryRate = Random.getRealDropRateForRank(Rank.LEGENDARY, dataService);

            // Assert
            assertThat(legendaryRate).isEqualTo(1.0f);
        }
    }
}
