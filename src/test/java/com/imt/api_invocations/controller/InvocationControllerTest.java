package com.imt.api_invocations.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithoutRankDto;
import com.imt.api_invocations.controller.dto.output.InvocationReplayResponse;
import com.imt.api_invocations.controller.dto.output.SkillForMonsterWithoutRankDto;
import com.imt.api_invocations.controller.mapper.DtoMapperInvocation;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.service.InvocationService;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;
import com.imt.api_invocations.persistence.dto.RatioDto;
import com.imt.api_invocations.enums.Stat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvocationController Unit Tests")
class InvocationControllerTest {

        @Mock
        private InvocationService invocationService;

        @Mock
        private DtoMapperInvocation dtoMapper;

        private InvocationController invocationController;

        @BeforeEach
        void setUp() {
                invocationController = new InvocationController(invocationService, dtoMapper);
        }

        @Test
        @DisplayName("Should invoke and return a monster successfully")
        void testInvoqueSuccess() {
                // Arrange
                List<SkillForMonsterDto> skills = Arrays.asList(
                                new SkillForMonsterDto(1, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON),
                                new SkillForMonsterDto(2, 60.0, new RatioDto(Stat.DEF, 0.9), 6.0, 12.0, Rank.RARE));
                GlobalMonsterDto monsterDto = new GlobalMonsterDto(
                                Elementary.FIRE,
                                100.0,
                                50.0,
                                30.0,
                                40.0,
                                skills,
                                Rank.COMMON);

                List<SkillForMonsterWithoutRankDto> expectedSkills = Arrays.asList(
                                new SkillForMonsterWithoutRankDto(1, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0),
                                new SkillForMonsterWithoutRankDto(2, 60.0, new RatioDto(Stat.DEF, 0.9), 6.0, 12.0));
                GlobalMonsterWithoutRankDto expectedResponse = new GlobalMonsterWithoutRankDto(
                                Elementary.FIRE,
                                100.0,
                                50.0,
                                30.0,
                                40.0,
                                expectedSkills);

                when(invocationService.invoke()).thenReturn(monsterDto);
                when(dtoMapper.toGlobalMonsterWithoutRankDto(monsterDto)).thenReturn(expectedResponse);

                // Act
                ResponseEntity<GlobalMonsterWithoutRankDto> result = invocationController.invoque();

                // Assert
                assertEquals(HttpStatus.OK, result.getStatusCode());
                assertNotNull(result.getBody());
                assertEquals(Elementary.FIRE, result.getBody().getElement());
                assertEquals(100.0, result.getBody().getHp());
                verify(invocationService, times(1)).invoke();
                verify(dtoMapper, times(1)).toGlobalMonsterWithoutRankDto(monsterDto);
        }

        @Test
        @DisplayName("Should perform global invocation successfully")
        void testGlobalInvoqueSuccess() {
                // Arrange
                String playerId = "player123";
                List<SkillForMonsterDto> skills = Arrays.asList(
                                new SkillForMonsterDto(1, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON));
                GlobalMonsterDto monsterDto = new GlobalMonsterDto(
                                Elementary.WATER,
                                110.0,
                                45.0,
                                35.0,
                                45.0,
                                skills,
                                Rank.RARE);

                List<SkillForMonsterWithoutRankDto> expectedSkills2 = Arrays.asList(
                                new SkillForMonsterWithoutRankDto(1, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0));
                GlobalMonsterWithoutRankDto expectedResponse = new GlobalMonsterWithoutRankDto(
                                Elementary.WATER,
                                110.0,
                                45.0,
                                35.0,
                                45.0,
                                expectedSkills2);

                when(invocationService.globalInvoke(playerId)).thenReturn(monsterDto);
                when(dtoMapper.toGlobalMonsterWithoutRankDto(monsterDto)).thenReturn(expectedResponse);

                // Act
                ResponseEntity<GlobalMonsterWithoutRankDto> result = invocationController.globalInvoque(playerId);

                // Assert
                assertEquals(HttpStatus.OK, result.getStatusCode());
                assertNotNull(result.getBody());
                assertEquals(Elementary.WATER, result.getBody().getElement());
                verify(invocationService, times(1)).globalInvoke(playerId);
                verify(dtoMapper, times(1)).toGlobalMonsterWithoutRankDto(monsterDto);
        }

        @Test
        @DisplayName("Should recreate invocations successfully")
        void testRecreateInvocationsSuccess() {
                // Arrange
                InvocationReplayReport report = new InvocationReplayReport(
                                10,
                                8,
                                2,
                                Arrays.asList("buffer1", "buffer2"));

                InvocationReplayResponse expectedResponse = new InvocationReplayResponse(
                                10,
                                8,
                                2,
                                Arrays.asList("buffer1", "buffer2"));

                when(invocationService.replayBufferedInvocations()).thenReturn(report);
                when(dtoMapper.toInvocationReplayResponse(report)).thenReturn(expectedResponse);

                // Act
                ResponseEntity<InvocationReplayResponse> result = invocationController.recreateInvocations();

                // Assert
                assertEquals(HttpStatus.OK, result.getStatusCode());
                assertNotNull(result.getBody());
                assertEquals(10, result.getBody().getRetried());
                assertEquals(8, result.getBody().getSucceeded());
                assertEquals(2, result.getBody().getFailed());
                verify(invocationService, times(1)).replayBufferedInvocations();
                verify(dtoMapper, times(1)).toInvocationReplayResponse(report);
        }

        @Test
        @DisplayName("Should handle empty replay report")
        void testRecreateInvocationsEmpty() {
                // Arrange
                InvocationReplayReport report = new InvocationReplayReport(
                                0,
                                0,
                                0,
                                Collections.emptyList());

                InvocationReplayResponse expectedResponse = new InvocationReplayResponse(
                                0,
                                0,
                                0,
                                Collections.emptyList());

                when(invocationService.replayBufferedInvocations()).thenReturn(report);
                when(dtoMapper.toInvocationReplayResponse(report)).thenReturn(expectedResponse);

                // Act
                ResponseEntity<InvocationReplayResponse> result = invocationController.recreateInvocations();

                // Assert
                assertEquals(HttpStatus.OK, result.getStatusCode());
                assertNotNull(result.getBody());
                assertEquals(0, result.getBody().getRetried());
                verify(invocationService, times(1)).replayBufferedInvocations();
        }
}
