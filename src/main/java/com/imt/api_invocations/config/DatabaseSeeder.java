package com.imt.api_invocations.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

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
                long monsterCount = monsterRepository.count();
                long skillCount = skillsRepository.count();

                if (monsterCount == 0) {
                        logger.info("Seeding database from JSON configuration...");

                        try {
                                List<MonsterSeedDto> monsterSeeds = monsterSeedingService.loadAllMonsters();
                                seedMonsters(monsterSeeds);
                                logger.info("Database seeding completed successfully!");
                        } catch (Exception e) { // NOSONAR - Logging with context before rethrowing with wrapper
                                                // exception
                                logConstraintViolations(e);
                                String errorMsg = "Failed to seed database with initial data: " + e.getMessage();
                                logger.error(errorMsg, e);
                                throw new IllegalStateException(errorMsg, e);
                        }
                        return;
                }

                if (skillCount == 0) {
                        logger.info("Monsters already present; seeding skills from JSON configuration...");

                        try {
                                List<MonsterSeedDto> monsterSeeds = monsterSeedingService.loadAllMonsters();
                                seedSkillsOnly(monsterSeeds);
                                logger.info("Skill seeding completed successfully!");
                        } catch (Exception e) { // NOSONAR - Logging with context before rethrowing with wrapper
                                                // exception
                                logConstraintViolations(e);
                                String errorMsg = "Failed to seed skills with initial data: " + e.getMessage();
                                logger.error(errorMsg, e);
                                throw new IllegalStateException(errorMsg, e);
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

        private void seedSkillsOnly(List<MonsterSeedDto> monsterSeeds) {
                List<MonsterMongoDto> monsters = monsterRepository.findAll();
                Map<String, String> monsterIdByName = new HashMap<>();

                for (MonsterMongoDto monster : monsters) {
                        if (monster.getName() != null) {
                                monsterIdByName.put(monster.getName(), monster.getId());
                        }
                }

                List<SkillsMongoDto> allSkills = new ArrayList<>();

                for (MonsterSeedDto seedDto : monsterSeeds) {
                        String monsterId = monsterIdByName.get(seedDto.getNom());

                        if (monsterId == null) {
                                logger.warn("No monster found for seed name: {}", seedDto.getNom());
                                continue;
                        }

                        if (seedDto.getSkills() != null) {
                                for (SkillSeedDto skillSeed : seedDto.getSkills()) {
                                        SkillsMongoDto skill = convertToSkillEntity(monsterId, skillSeed);
                                        allSkills.add(skill);
                                }
                        }
                }

                if (!allSkills.isEmpty()) {
                        skillsRepository.saveAll(allSkills);
                }
        }

        /**
         * Convertit un MonsterSeedDto en MonsterMongoDto
         */
        private MonsterMongoDto convertToMonsterEntity(MonsterSeedDto seedDto) {
                StatsDto stats = StatsDto.builder()
                                .hp(seedDto.getStats().getHp().longValue())
                                .atk(seedDto.getStats().getAtk().longValue())
                                .def(seedDto.getStats().getDef().longValue())
                                .vit(seedDto.getStats().getVit().longValue())
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
                                .damage(seedDto.getDamage().longValue())
                                .ratio(ratio)
                                .cooldown(seedDto.getCooldown().longValue())
                                .lvlMax(seedDto.getLvlMax().longValue())
                                .rank(Rank.valueOf(seedDto.getRank()))
                                .build();
        }

        private void logConstraintViolations(Throwable error) {
                ConstraintViolationException violationException = findConstraintViolationException(error);

                if (violationException == null) {
                        return;
                }

                logger.error("Validation errors detected during seeding:");

                for (ConstraintViolation<?> violation : violationException.getConstraintViolations()) {
                        String bean = violation.getRootBeanClass() != null
                                        ? violation.getRootBeanClass().getSimpleName()
                                        : "Unknown";
                        logger.error("- {}.{}: {} (invalid value: {})",
                                        bean,
                                        violation.getPropertyPath(),
                                        violation.getMessage(),
                                        violation.getInvalidValue());
                }
        }

        private ConstraintViolationException findConstraintViolationException(Throwable error) {
                Throwable current = error;

                while (current != null) {
                        if (current instanceof ConstraintViolationException violationException) {
                                return violationException;
                        }
                        current = current.getCause();
                }

                return null;
        }
}
