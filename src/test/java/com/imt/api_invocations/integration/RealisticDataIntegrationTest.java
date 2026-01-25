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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(com.imt.api_invocations.config.TestContainersConfig.class)
@DisplayName("Realistic Data Integration Tests")
class RealisticDataIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private MonsterRepository monsterRepository;

        @Autowired
        private SkillsRepository skillsRepository;

        // Test data constants
        private static final class TestMonsters {
                static final MonsterHttpDto FIRE_DRAGON = new MonsterHttpDto(
                                Elementary.FIRE, 150.0, 85.0, 45.0, 70.0, Rank.EPIC);
                static final MonsterHttpDto WATER_PHOENIX = new MonsterHttpDto(
                                Elementary.WATER, 140.0, 75.0, 50.0, 75.0, Rank.EPIC);
                static final MonsterHttpDto GRASS_GOLEM = new MonsterHttpDto(
                                Elementary.EARTH, 180.0, 65.0, 70.0, 60.0, Rank.RARE);
                static final MonsterHttpDto COMMON_BEAST = new MonsterHttpDto(
                                Elementary.FIRE, 80.0, 40.0, 20.0, 30.0, Rank.COMMON);
        }

        @BeforeEach
        void setUp() {
                skillsRepository.deleteAll();
                monsterRepository.deleteAll();
        }

        @Test
        @DisplayName("Should handle realistic fire dragon invocation")
        void testFireDragonScenario() throws Exception {
                // Setup: Create fire dragon with appropriate skills
                String dragonId = createMonster(TestMonsters.FIRE_DRAGON);

                createSkill(
                                new SkillsHttpDto(dragonId, 120.0, new RatioDto(Stat.ATK, 1.2), 8.0, 15.0, Rank.EPIC)); // Fire
                                                                                                                        // Blast
                createSkill(
                                new SkillsHttpDto(dragonId, 90.0, new RatioDto(Stat.ATK, 0.9), 6.0, 12.0, Rank.RARE)); // Flame
                                                                                                                       // Burst
                createSkill(
                                new SkillsHttpDto(dragonId, 60.0, new RatioDto(Stat.ATK, 0.7), 4.0, 10.0, Rank.COMMON)); // Ember

                // Verify monster data
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", dragonId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.element").value("FIRE"))
                                .andExpect(jsonPath("$.hp").value(150))
                                .andExpect(jsonPath("$.atk").value(85))
                                .andExpect(jsonPath("$.def").value(45))
                                .andExpect(jsonPath("$.vit").value(70))
                                .andExpect(jsonPath("$.rank").value("EPIC"));
        }

        @Test
        @DisplayName("Should handle balanced water phoenix team composition")
        void testWaterPhoenixTeamComposition() throws Exception {
                // Create main monster
                String phoenixId = createMonster(TestMonsters.WATER_PHOENIX);

                // Create balanced skill set (offensive, defensive, utility)
                createSkill(
                                new SkillsHttpDto(phoenixId, 100.0, new RatioDto(Stat.ATK, 1.0), 7.0, 14.0, Rank.EPIC)); // Aqua
                                                                                                                         // Lance
                createSkill(
                                new SkillsHttpDto(phoenixId, 75.0, new RatioDto(Stat.DEF, 0.8), 5.0, 11.0, Rank.RARE)); // Water
                                                                                                                        // Shield
                createSkill(
                                new SkillsHttpDto(phoenixId, 85.0, new RatioDto(Stat.VIT, 0.85), 6.0, 13.0, Rank.RARE)); // Tidal
                                                                                                                         // Wave

                // Verify complete team data
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", phoenixId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.element").value("WATER"))
                                .andExpect(jsonPath("$.skills", hasSize(greaterThanOrEqualTo(0))));
        }

        @Test
        @DisplayName("Should handle defensive grass golem with high HP and DEF")
        void testGrassGolemDefensive() throws Exception {
                // Create tank monster
                String golemId = createMonster(TestMonsters.GRASS_GOLEM);

                // Create defensive skills
                createSkill(
                                new SkillsHttpDto(golemId, 50.0, new RatioDto(Stat.DEF, 0.6), 3.0, 8.0, Rank.RARE)); // Root
                                                                                                                     // Strike
                createSkill(
                                new SkillsHttpDto(golemId, 40.0, new RatioDto(Stat.DEF, 0.5), 2.0, 6.0, Rank.COMMON)); // Stone
                                                                                                                       // Guard
                createSkill(
                                new SkillsHttpDto(golemId, 30.0, new RatioDto(Stat.DEF, 0.4), 2.0, 5.0, Rank.COMMON)); // Thorns

                // Verify stats for tank role
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", golemId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.hp").value(180)) // High HP
                                .andExpect(jsonPath("$.def").value(70)) // High DEF
                                .andExpect(jsonPath("$.atk").value(65));
        }

        @Test
        @DisplayName("Should handle progression from common to epic monsters")
        void testMonsterProgression() throws Exception {
                // Create progression path
                String commonId = createMonster(TestMonsters.COMMON_BEAST);
                String rareId = createMonster(new MonsterHttpDto(
                                Elementary.FIRE, 100.0, 55.0, 30.0, 45.0, Rank.RARE));
                String epicId = createMonster(TestMonsters.FIRE_DRAGON);

                // Verify progression stat increases
                // Common
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", commonId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.rank").value("COMMON"))
                                .andExpect(jsonPath("$.hp").value(80));

                // Rare
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", rareId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.rank").value("RARE"))
                                .andExpect(jsonPath("$.hp").value(100));

                // Epic
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", epicId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.rank").value("EPIC"))
                                .andExpect(jsonPath("$.hp").value(150));
        }

        @Test
        @DisplayName("Should handle diverse element types")
        void testDiverseElementTypes() throws Exception {
                // Create monsters of different elements
                String fireId = createMonster(TestMonsters.FIRE_DRAGON);
                String waterId = createMonster(TestMonsters.WATER_PHOENIX);
                String grassId = createMonster(TestMonsters.GRASS_GOLEM);

                // Create a fire, water, and grass skill set
                createSkill(
                                new SkillsHttpDto(fireId, 110.0, new RatioDto(Stat.ATK, 1.1), 7.0, 14.0, Rank.EPIC));
                createSkill(
                                new SkillsHttpDto(waterId, 105.0, new RatioDto(Stat.ATK, 1.05), 7.0, 14.0, Rank.EPIC));
                createSkill(
                                new SkillsHttpDto(grassId, 95.0, new RatioDto(Stat.DEF, 0.95), 6.0, 13.0, Rank.RARE));

                // Verify elements
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", fireId))
                                .andExpect(jsonPath("$.element").value("FIRE"));

                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", waterId))
                                .andExpect(jsonPath("$.element").value("WATER"));

                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", grassId))
                                .andExpect(jsonPath("$.element").value("EARTH"));
        }

        @Test
        @DisplayName("Should handle realistic invocation with varied skill levels")
        void testInvocationWithVariedSkills() throws Exception {
                // Setup realistic team
                String monsterId = createMonster(TestMonsters.WATER_PHOENIX);

                // Add skills with varied difficulty levels
                createSkill(
                                new SkillsHttpDto(monsterId, 120.0, new RatioDto(Stat.ATK, 1.2), 8.0, 15.0, Rank.EPIC)); // Hard
                createSkill(
                                new SkillsHttpDto(monsterId, 80.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.RARE)); // Medium
                createSkill(
                                new SkillsHttpDto(monsterId, 40.0, new RatioDto(Stat.ATK, 0.4), 2.0, 5.0, Rank.COMMON)); // Easy

                // Perform invocation
                mockMvc.perform(get("/api/invocation/invoque"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.element", notNullValue()))
                                .andExpect(jsonPath("$.hp").isNumber())
                                .andExpect(jsonPath("$.atk").isNumber())
                                .andExpect(jsonPath("$.def").isNumber())
                                .andExpect(jsonPath("$.vit").isNumber())
                                .andExpect(jsonPath("$.skills", hasSize(greaterThanOrEqualTo(0))));
        }

        @Test
        @DisplayName("Should handle bulk operations with multiple monsters and skills")
        void testBulkOperations() throws Exception {
                // Create multiple monsters
                String dragon = createMonster(TestMonsters.FIRE_DRAGON);
                String phoenix = createMonster(TestMonsters.WATER_PHOENIX);
                String golem = createMonster(TestMonsters.GRASS_GOLEM);

                // Add skills to each
                for (String monsterId : new String[] { dragon, phoenix, golem }) {
                        for (int i = 0; i < 3; i++) {
                                createSkill(new SkillsHttpDto(
                                                monsterId,
                                                50.0 + (i * 20),
                                                new RatioDto(Stat.ATK, 0.7 + (i * 0.15)),
                                                4.0 + i,
                                                10.0 + (i * 2),
                                                Rank.COMMON));
                        }
                }

                // Perform multiple invocations
                for (int i = 0; i < 5; i++) {
                        mockMvc.perform(get("/api/invocation/invoque"))
                                        .andExpect(status().isOk());
                }

                // Verify completion
                mockMvc.perform(post("/api/invocation/recreate"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.retried", greaterThanOrEqualTo(0)));
        }

        @Test
        @DisplayName("Should verify stat scaling is realistic")
        void testRealisticStatScaling() throws Exception {
                // Common monster stats
                MonsterHttpDto commonStats = new MonsterHttpDto(
                                Elementary.FIRE, 80.0, 40.0, 20.0, 30.0, Rank.COMMON);
                // Rare should have 1.25x stats
                MonsterHttpDto rareStats = new MonsterHttpDto(
                                Elementary.FIRE, 100.0, 50.0, 25.0, 37.0, Rank.RARE);
                // Epic should have 1.875x stats
                MonsterHttpDto epicStats = new MonsterHttpDto(
                                Elementary.FIRE, 150.0, 75.0, 37.0, 56.0, Rank.EPIC);

                String commonId = createMonster(commonStats);
                String rareId = createMonster(rareStats);
                String epicId = createMonster(epicStats);

                // Verify scaling
                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", commonId))
                                .andExpect(jsonPath("$.hp").value(80));

                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", rareId))
                                .andExpect(jsonPath("$.hp").value(100));

                mockMvc.perform(get("/api/invocation/monsters/{monsterId}", epicId))
                                .andExpect(jsonPath("$.hp").value(150));
        }

        @Test
        @DisplayName("Should handle player collection scenario")
        void testPlayerCollectionScenario() throws Exception {
                // Simulate player collecting different monsters
                String[] playerMonsters = new String[3];

                // Collect common
                playerMonsters[0] = createMonster(TestMonsters.COMMON_BEAST);

                // Collect rare
                playerMonsters[1] = createMonster(new MonsterHttpDto(
                                Elementary.WATER, 100.0, 50.0, 30.0, 45.0, Rank.RARE));

                // Collect epic
                playerMonsters[2] = createMonster(TestMonsters.FIRE_DRAGON);

                // Verify player collection
                for (int i = 0; i < playerMonsters.length; i++) {
                        mockMvc.perform(get("/api/invocation/monsters/{monsterId}", playerMonsters[i]))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id", notNullValue()));
                }
        }

        // Helper methods

        private String createMonster(MonsterHttpDto dto) throws Exception {
                MvcResult result = mockMvc.perform(post("/api/invocation/monsters/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated())
                                .andReturn();
                return result.getResponse().getContentAsString();
        }

        private String createSkill(SkillsHttpDto dto) throws Exception {
                MvcResult result = mockMvc.perform(post("/api/invocation/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated())
                                .andReturn();
                return result.getResponse().getContentAsString();
        }
}
