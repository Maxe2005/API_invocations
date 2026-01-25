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
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.InvocationBufferRepository;
import com.imt.api_invocations.persistence.dto.RatioDto;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(com.imt.api_invocations.config.TestContainersConfig.class)
@DisplayName("Invocation Integration Tests")
class InvocationIntegrationTest {

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
        @DisplayName("Should invoke a monster successfully")
        void testInvokeMonsterIntegration() throws Exception {
                // Setup: Create a monster and skills
                setupMonsterAndSkills();

                // Act & Assert
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
        @DisplayName("Should create a monster via API")
        void testCreateMonsterIntegration() throws Exception {
                // Arrange
                MonsterHttpDto monsterDto = new MonsterHttpDto(
                                Elementary.FIRE,
                                100.0,
                                50.0,
                                30.0,
                                40.0,
                                Rank.COMMON);

                // Act & Assert
                MvcResult result = mockMvc.perform(post("/api/invocation/monsters/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(monsterDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String monsterId = result.getResponse().getContentAsString();
                assert !monsterId.isEmpty();
        }

        @Test
        @DisplayName("Should get monster by ID")
        void testGetMonsterByIdIntegration() throws Exception {
                // Setup: Create a monster first
                String monsterId = createTestMonster();

                // Act & Assert
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", monsterId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(monsterId))
                                .andExpect(jsonPath("$.element").value("FIRE"))
                                .andExpect(jsonPath("$.hp").value(100));
        }

        @Test
        @DisplayName("Should return 404 for non-existent monster")
        void testGetNonExistentMonsterIntegration() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", "nonexistent")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should create a skill for a monster")
        void testCreateSkillIntegration() throws Exception {
                // Setup: Create a monster first
                String monsterId = createTestMonster();

                // Arrange
                SkillsHttpDto skillDto = new SkillsHttpDto(
                                monsterId,
                                50.0,
                                new RatioDto(Stat.ATK, 0.8),
                                5.0,
                                10.0,
                                Rank.COMMON);

                // Act & Assert
                MvcResult result = mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skillDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String skillId = result.getResponse().getContentAsString();
                assert !skillId.isEmpty();
        }

        @Test
        @DisplayName("Should fail to create skill for non-existent monster")
        void testCreateSkillForNonExistentMonsterIntegration() throws Exception {
                // Arrange
                SkillsHttpDto skillDto = new SkillsHttpDto(
                                "nonexistent",
                                50.0,
                                new RatioDto(Stat.ATK, 0.8),
                                5.0,
                                10.0,
                                Rank.COMMON);

                // Act & Assert
                mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skillDto)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should get skill by ID")
        void testGetSkillByIdIntegration() throws Exception {
                // Setup: Create monster and skill
                String monsterId = createTestMonster();
                String skillId = createTestSkill(monsterId);

                // Act & Assert
                mockMvc.perform(get("/api/invocation/skills/{skillId}", skillId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(skillId))
                                .andExpect(jsonPath("$.monsterId").value(monsterId))
                                .andExpect(jsonPath("$.damage").value(50));
        }

        @Test
        @DisplayName("Should recreate invocations")
        void testRecreateInvocationsIntegration() throws Exception {
                // Setup: Create monster and skills for testing
                setupMonsterAndSkills();

                // Act & Assert
                mockMvc.perform(post("/api/invocation/recreate")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.retried").isNumber())
                                .andExpect(jsonPath("$.succeeded").isNumber())
                                .andExpect(jsonPath("$.failed").isNumber())
                                .andExpect(jsonPath("$.failedInvocationIds").isArray());
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
