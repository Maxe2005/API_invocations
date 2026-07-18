package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.enums.Rank;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsService - Tests Unitaires")
class StatsServiceTest {

    @Mock
    private MonsterService monsterService;

    @InjectMocks
    private StatsService statsService;

    @Test
    @DisplayName("verifyCorrectRanksDropRate retourne true quand la somme vaut 100%")
    void should_ReturnTrue_When_DropRatesSumToOne() {
        boolean result = statsService.verifyCorrectRanksDropRate();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getTheoreticalLootRatesString formate correctement les taux")
    void should_FormatTheoreticalLootRates_When_Called() {
        stubMonsterCounts();

        String result = statsService.getTheoreticalLootRatesString();

        assertThat(result).contains("Theoretical Drop Rates:").contains("COMMON: 50.00%")
                .contains("RARE: 30.00%").contains("EPIC: 15.00%").contains("LEGENDARY: 5.00%");
    }

    @Test
    @DisplayName("getRealLootRatesString calcule les ratios réels")
    void should_ComputeRealLootRates_When_SomeRanksUnavailable() {
        stubMonsterCounts();
        // COMMON et EPIC disponibles, RARE et LEGENDARY indisponibles
        when(monsterService.hasAvailableData(Rank.COMMON)).thenReturn(true);
        when(monsterService.hasAvailableData(Rank.RARE)).thenReturn(false);
        when(monsterService.hasAvailableData(Rank.EPIC)).thenReturn(true);
        when(monsterService.hasAvailableData(Rank.LEGENDARY)).thenReturn(false);

        String result = statsService.getRealLootRatesString();

        // totalAvailableRate = 0.5 + 0.15 = 0.65
        assertThat(result).contains("Real Drop Rates").contains("COMMON: 76.92%")
                .contains("EPIC: 23.08%").contains("RARE: 0.00%").contains("LEGENDARY: 0.00%");
    }

    @Test
    @DisplayName("getLootRatesString combine les sorties théorique et réelle")
    void should_CombineTheoreticalAndRealSections_When_GetLootRatesCalled() {
        stubMonsterCounts();
        when(monsterService.hasAvailableData(Rank.COMMON)).thenReturn(true);
        when(monsterService.hasAvailableData(Rank.RARE)).thenReturn(true);
        when(monsterService.hasAvailableData(Rank.EPIC)).thenReturn(true);
        when(monsterService.hasAvailableData(Rank.LEGENDARY)).thenReturn(true);

        String result = statsService.getLootRatesString();

        assertThat(result).contains("Loot Rates by Rank:").contains("Theoretical Drop Rates:")
                .contains("Real Drop Rates").contains("-----------------------");
    }

    private void stubMonsterCounts() {
        when(monsterService.getAllMonsterIdByRank(Rank.COMMON)).thenReturn(List.of("c1", "c2"));
        when(monsterService.getAllMonsterIdByRank(Rank.RARE)).thenReturn(List.of("r1"));
        when(monsterService.getAllMonsterIdByRank(Rank.EPIC)).thenReturn(List.of("e1", "e2", "e3"));
        when(monsterService.getAllMonsterIdByRank(Rank.LEGENDARY)).thenReturn(List.of());
    }
}
