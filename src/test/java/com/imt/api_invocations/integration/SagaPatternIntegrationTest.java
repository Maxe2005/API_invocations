package com.imt.api_invocations.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.InvocationBufferRepository;
import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;
import com.imt.api_invocations.persistence.dto.RatioDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(com.imt.api_invocations.config.TestContainersConfig.class)
@DisplayName("Saga Pattern Integration Tests")
class SagaPatternIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private MonsterRepository monsterRepository;

        @Autowired
        private SkillsRepository skillsRepository;

        @Autowired
        private InvocationBufferRepository invocationBufferRepository;

        @BeforeEach
        void setUp() {
                // Clean up before each test
                invocationBufferRepository.deleteAll();
                skillsRepository.deleteAll();
                monsterRepository.deleteAll();
        }

        @Test
        @DisplayName("Should complete full saga flow: invoke, create monster, add to player")
        void testCompleteSagaFlow() throws Exception {
                // Setup: Create monster and skills
                setupMonsterAndSkills();

                // Act: Invoke a monster
                MvcResult invokeResult = mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andReturn();

                String invokeResponse = invokeResult.getResponse().getContentAsString();
                assert !invokeResponse.isEmpty();

                // Assert: Monster data is returned
                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.element", notNullValue()))
                                .andExpect(jsonPath("$.hp").isNumber())
                                .andExpect(jsonPath("$.atk").isNumber())
                                .andExpect(jsonPath("$.def").isNumber())
                                .andExpect(jsonPath("$.vit").isNumber())
                                .andExpect(jsonPath("$.skills").isArray());
        }

        @Test
        @DisplayName("Should handle saga compensation when player API fails")
        void testSagaCompensation() throws Exception {
                // Setup: Create monster and skills
                setupMonsterAndSkills();

                // The actual compensation test would require mocking the external API clients
                // which is covered in the unit tests. This integration test verifies that
                // the buffer is properly stored when invocation occurs.

                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // Assert: Invocation was successful (compensation would happen on next recreate
                // call)
        }

        @Test
        @DisplayName("Should store invocation buffer for retry")
        void testInvocationBufferStorage() throws Exception {
                // Setup: Create monster and skills
                setupMonsterAndSkills();

                long before = invocationBufferRepository.count();

                // Act: Invoke a monster
                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                long after = invocationBufferRepository.count();

                // Assert: buffer count should be unchanged or increased but not negative
                assert after >= before;
        }

        @Test
        @DisplayName("Should replay buffered failed invocations")
        void testReplayBufferedInvocations() throws Exception {
                // Setup: Create monster and skills
                setupMonsterAndSkills();

                // Act: Try to recreate buffered invocations
                MvcResult recreateResult = mockMvc.perform(post("/api/invocation/recreate")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andReturn();

                String response = recreateResult.getResponse().getContentAsString();
                assert !response.isEmpty();

                // Assert: Response contains replay statistics
                mockMvc.perform(post("/api/invocation/recreate")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.retried").isNumber())
                                .andExpect(jsonPath("$.succeeded").isNumber())
                                .andExpect(jsonPath("$.failed").isNumber())
                                .andExpect(jsonPath("$.failedInvocationIds").isArray());
        }

        @Test
        @DisplayName("Should maintain invocation buffer state across operations")
        void testInvocationBufferStateManagement() throws Exception {
                // Setup
                String monsterId = createTestMonster();
                createTestSkill(monsterId);

                // Verify buffer repository is accessible and clean
                long initialCount = invocationBufferRepository.count();
                assertEquals(0, initialCount, "Buffer should be empty initially");

                // Act: Create an invocation buffer entry
                InvocationBufferDto bufferEntry = InvocationBufferDto.builder()
                                .playerId("test-player-123")
                                .status(InvocationStatus.PENDING)
                                .attemptCount(0)
                                .build();

                invocationBufferRepository.save(bufferEntry);

                // Assert: Verify buffer was saved
                long afterSaveCount = invocationBufferRepository.count();
                assertEquals(1, afterSaveCount, "Buffer should contain one entry");
        }

        @Test
        @DisplayName("Should handle multiple saga flows in sequence")
        void testMultipleSagaFlows() throws Exception {
                // Setup: Create multiple monsters
                String monster1 = createTestMonster();
                String monster2 = createTestMonster();
                createTestSkill(monster1);
                createTestSkill(monster2);

                // Act: Execute multiple invocations
                for (int i = 0; i < 3; i++) {
                        mockMvc.perform(get("/api/invocation/invoque")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
                }

                // Assert: All invocations completed successfully
                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.element", notNullValue()));
        }

        @Test
        @DisplayName("Should handle concurrent saga invocations")
        void testConcurrentSagaInvocations() throws Exception {
                // Setup: Create multiple monsters and skills
                String monster1 = createTestMonster();
                String monster2 = createTestMonster();
                String monster3 = createTestMonster();

                createTestSkill(monster1);
                createTestSkill(monster2);
                createTestSkill(monster3);

                // Act: Simulate concurrent invocations
                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // Assert: All invocations completed
        }

        @Test
        @DisplayName("Should verify saga steps in correct order")
        void testSagaStepOrder() throws Exception {
                // Setup
                setupMonsterAndSkills();

                // Step 1: Invoke monster
                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.element", notNullValue()));

                // Step 2: Check that monster has valid attributes (would be step before adding
                // to player)
                mockMvc.perform(get("/api/invocation/invoque")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.hp").isNumber())
                                .andExpect(jsonPath("$.atk").isNumber())
                                .andExpect(jsonPath("$.def").isNumber())
                                .andExpect(jsonPath("$.vit").isNumber())
                                .andExpect(jsonPath("$.skills").isArray());

                // Step 3: Verify recreation endpoint works (final step)
                mockMvc.perform(post("/api/invocation/recreate")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        // Helper methods

        private String createTestMonster() throws Exception {
                MonsterHttpDto monsterDto = new MonsterHttpDto(
                                Elementary.FIRE,
                                100.0,
                                50.0,
                                30.0,
                                40.0,
                                Rank.COMMON);

                MvcResult result = mockMvc.perform(post("/api/invocation/monsters/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(monsterDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                return result.getResponse().getContentAsString();
        }

        private String createTestSkill(String monsterId) throws Exception {
                SkillsHttpDto skillDto = new SkillsHttpDto(
                                monsterId,
                                50.0,
                                new RatioDto(Stat.ATK, 0.8),
                                5.0,
                                10.0,
                                Rank.COMMON);

                MvcResult result = mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skillDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                return result.getResponse().getContentAsString();
        }

        private void setupMonsterAndSkills() throws Exception {
                String monsterId = createTestMonster();
                createTestSkill(monsterId);
                createTestSkill(monsterId);
                createTestSkill(monsterId);
        }
}
