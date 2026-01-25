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
import com.imt.api_invocations.persistence.dto.RatioDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(com.imt.api_invocations.config.TestContainersConfig.class)
@DisplayName("Monster & Skills Integration Tests")
class MonsterSkillsIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private MonsterRepository monsterRepository;

        @Autowired
        private SkillsRepository skillsRepository;

        @BeforeEach
        void setUp() {
                // Clean up before each test
                skillsRepository.deleteAll();
                monsterRepository.deleteAll();
        }

        @Test
        @DisplayName("Should create and retrieve a monster")
        void testMonsterLifecycle() throws Exception {
                // Arrange
                MonsterHttpDto monsterDto = new MonsterHttpDto(
                                Elementary.FIRE,
                                150.0,
                                60.0,
                                40.0,
                                50.0,
                                Rank.RARE);

                // Act: Create monster
                MvcResult createResult = mockMvc.perform(post("/api/invocation/monsters/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(monsterDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String monsterId = createResult.getResponse().getContentAsString();

                // Assert: Get the created monster
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", monsterId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(monsterId))
                                .andExpect(jsonPath("$.element").value("FIRE"))
                                .andExpect(jsonPath("$.hp").value(150))
                                .andExpect(jsonPath("$.rank").value("RARE"));
        }

        @Test
        @DisplayName("Should create monster and add multiple skills")
        void testMonsterWithMultipleSkills() throws Exception {
                // Arrange: Create a monster
                MonsterHttpDto monsterDto = new MonsterHttpDto(
                                Elementary.WATER,
                                120.0,
                                55.0,
                                45.0,
                                60.0,
                                Rank.RARE);

                MvcResult createMonsterResult = mockMvc.perform(post("/api/invocation/monsters/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(monsterDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String monsterId = createMonsterResult.getResponse().getContentAsString();

                // Act: Add multiple skills
                SkillsHttpDto skill1 = new SkillsHttpDto(monsterId, 40.0, new RatioDto(Stat.ATK, 0.7), 4.0, 8.0,
                                Rank.COMMON);
                SkillsHttpDto skill2 = new SkillsHttpDto(monsterId, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0,
                                Rank.COMMON);
                SkillsHttpDto skill3 = new SkillsHttpDto(monsterId, 70.0, new RatioDto(Stat.DEF, 1.0), 7.0, 15.0,
                                Rank.RARE);

                MvcResult skill1Result = mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skill1)))
                                .andExpect(status().isCreated())
                                .andReturn();

                MvcResult skill2Result = mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skill2)))
                                .andExpect(status().isCreated())
                                .andReturn();

                MvcResult skill3Result = mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skill3)))
                                .andExpect(status().isCreated())
                                .andReturn();

                // Assert: Verify all skills were created
                assert !skill1Result.getResponse().getContentAsString().isEmpty();
                assert !skill2Result.getResponse().getContentAsString().isEmpty();
                assert !skill3Result.getResponse().getContentAsString().isEmpty();
        }

        @Test
        @DisplayName("Should validate monster creation with invalid data")
        void testMonsterCreationValidation() throws Exception {
                // Arrange: Invalid monster DTO (missing required fields)
                String invalidJson = "{}";

                // Act & Assert
                mockMvc.perform(post("/api/invocation/monsters/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle skill creation with invalid monster ID")
        void testSkillCreationWithInvalidMonsterId() throws Exception {
                // Arrange
                SkillsHttpDto skillDto = new SkillsHttpDto(
                                "invalid-monster-id",
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
        @DisplayName("Should list all monsters")
        void testGetAllMonsters() throws Exception {
                // Arrange: Create multiple monsters
                createTestMonster(Elementary.FIRE, Rank.COMMON);
                createTestMonster(Elementary.WATER, Rank.RARE);
                createTestMonster(Elementary.WIND, Rank.EPIC);

                // Act & Assert
                mockMvc.perform(get("/api/invocation/monsters/all")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should update monster data")
        void testUpdateMonster() throws Exception {
                // Arrange: Create a monster
                String monsterId = createTestMonster(Elementary.FIRE, Rank.COMMON);

                MonsterHttpDto updateDto = new MonsterHttpDto(
                                Elementary.WIND,
                                200.0,
                                70.0,
                                50.0,
                                60.0,
                                Rank.EPIC);

                // Act & Assert
                mockMvc.perform(put("/api/invocation/monsters/{monsterId}", monsterId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should delete monster")
        void testDeleteMonster() throws Exception {
                // Arrange: Create a monster
                String monsterId = createTestMonster(Elementary.FIRE, Rank.COMMON);

                // Act: Delete the monster
                mockMvc.perform(delete("/api/invocation/monsters/{monsterId}", monsterId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // Assert: Verify it's deleted
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", monsterId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should delete skill")
        void testDeleteSkill() throws Exception {
                // Arrange: Create monster and skill
                String monsterId = createTestMonster(Elementary.FIRE, Rank.COMMON);
                String skillId = createTestSkill(monsterId);

                // Act: Delete the skill
                mockMvc.perform(delete("/api/invocation/skills/{skillId}", skillId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // Assert: Verify it's deleted
                mockMvc.perform(get("/api/invocation/skills/{skillId}", skillId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        // Helper methods

        private String createTestMonster(Elementary element, Rank rank) throws Exception {
                MonsterHttpDto monsterDto = new MonsterHttpDto(element, 100.0, 50.0, 30.0, 40.0, rank);
                MvcResult result = mockMvc.perform(post("/api/invocation/monsters/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(monsterDto)))
                                .andExpect(status().isCreated())
                                .andReturn();
                return result.getResponse().getContentAsString();
        }

        private String createTestSkill(String monsterId) throws Exception {
                SkillsHttpDto skillDto = new SkillsHttpDto(monsterId, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0,
                                Rank.COMMON);
                MvcResult result = mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skillDto)))
                                .andExpect(status().isCreated())
                                .andReturn();
                return result.getResponse().getContentAsString();
        }
}
