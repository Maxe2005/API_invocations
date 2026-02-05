package com.imt.api_invocations.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.imt.api_invocations.config.seeding.MonsterSeedDto;
import com.imt.api_invocations.config.seeding.MonsterSeedingService;
import com.imt.api_invocations.config.seeding.SkillSeedDto;
import com.imt.api_invocations.dto.RatioDto;
import com.imt.api_invocations.dto.StatsDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.dao.MonsterMongoDao;
import com.imt.api_invocations.persistence.dao.SkillsMongoDao;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

        private final MonsterMongoDao monsterRepository;
        private final SkillsMongoDao skillsRepository;
        private final MonsterSeedingService monsterSeedingService;

        @Override
        public void run(String... args) throws Exception {
                if (monsterRepository.count() == 0) {
                        logger.info("Seeding database from JSON configuration...");

                        try {
                                List<MonsterSeedDto> monsterSeeds = monsterSeedingService.loadAllMonsters();
                                seedMonsters(monsterSeeds);
                                logger.info("Database seeding completed successfully!");
                        } catch (Exception e) {
                                logger.error("Error during database seeding", e);
                                throw e;
                        }
                }
        }

        /**
         * Seed la base de données avec les monstres et compétences chargés depuis JSON
         */
        private void seedMonsters(List<MonsterSeedDto> monsterSeeds) {
                List<MonsterMongoDto> monsters = new ArrayList<>();
                List<SkillsMongoDto> allSkills = new ArrayList<>();

                // Convertir les DTOs de seed en entités de base de données
                for (MonsterSeedDto seedDto : monsterSeeds) {
                        MonsterMongoDto monster = convertToMonsterEntity(seedDto);
                        monsters.add(monster);
                }

                // Sauvegarder les monstres
                monsterRepository.saveAll(monsters);

                // Ajouter les compétences pour chaque monstre
                for (int i = 0; i < monsterSeeds.size(); i++) {
                        MonsterSeedDto seedDto = monsterSeeds.get(i);
                        MonsterMongoDto monster = monsters.get(i);

                        if (seedDto.getSkills() != null) {
                                for (SkillSeedDto skillSeed : seedDto.getSkills()) {
                                        SkillsMongoDto skill = convertToSkillEntity(monster.getId(), skillSeed);
                                        allSkills.add(skill);
                                }
                        }
                }

                // Sauvegarder les compétences
                if (!allSkills.isEmpty()) {
                        skillsRepository.saveAll(allSkills);
                }
        }

        /**
         * Convertit un MonsterSeedDto en MonsterMongoDto
         */
        private MonsterMongoDto convertToMonsterEntity(MonsterSeedDto seedDto) {
                StatsDto stats = StatsDto.builder()
                                .hp(seedDto.getStats().getHp())
                                .atk(seedDto.getStats().getAtk())
                                .def(seedDto.getStats().getDef())
                                .vit(seedDto.getStats().getVit())
                                .build();

                return MonsterMongoDto.builder()
                                .name(seedDto.getNom())
                                .element(Elementary.valueOf(seedDto.getElement()))
                                .stats(stats)
                                .rank(Rank.valueOf(seedDto.getRang()))
                                .visualDescription(seedDto.getDescriptionVisuelle())
                                .cardDescription(seedDto.getDescriptionCarte())
                                .imageUrl("")
                                .build();
        }

        /**
         * Convertit un SkillSeedDto en SkillsMongoDto
         */
        private SkillsMongoDto convertToSkillEntity(String monsterId, SkillSeedDto seedDto) {
                RatioDto ratio = RatioDto.builder()
                                .stat(Stat.valueOf(seedDto.getRatio().getStat()))
                                .percent(seedDto.getRatio().getPercent())
                                .build();

                return SkillsMongoDto.builder()
                                .monsterId(monsterId)
                                .name(seedDto.getName())
                                .description(seedDto.getDescription())
                                .damage(seedDto.getDamage())
                                .ratio(ratio)
                                .cooldown(seedDto.getCooldown())
                                .lvlMax(seedDto.getLvlMax())
                                .rank(Rank.valueOf(seedDto.getRank()))
                                .build();
        }
}
