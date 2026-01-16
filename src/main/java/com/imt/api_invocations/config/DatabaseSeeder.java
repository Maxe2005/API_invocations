package com.imt.api_invocations.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.dao.MonsterMongoDao;
import com.imt.api_invocations.persistence.dao.SkillsMongoDao;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.persistence.dto.RatioDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final MonsterMongoDao monsterRepository;
    private final SkillsMongoDao skillsRepository;

    @Override
    public void run(String... args) throws Exception {
        if (monsterRepository.count() == 0) {
            System.out.println("Seeding database...");

            // Initialize Monsters
            // Monster 1: Fire
            MonsterMongoDto m1 = new MonsterMongoDto(Elementary.FIRE, 1200.0, 450.0, 300.0, 85.0, Rank.COMMON);
            // Monster 2: Wind
            MonsterMongoDto m2 = new MonsterMongoDto(Elementary.WIND, 1500.0, 200.0, 450.0, 80.0, Rank.COMMON);
            // Monster 3: Water
            MonsterMongoDto m3 = new MonsterMongoDto(Elementary.WATER, 2500.0, 150.0, 200.0, 70.0, Rank.COMMON);
            // Monster 4: Water Dsp
            MonsterMongoDto m4 = new MonsterMongoDto(Elementary.WATER, 1200.0, 550.0, 350.0, 80.0, Rank.LEGENDARY);

            monsterRepository.saveAll(Arrays.asList(m1, m2, m3, m4));

            // Initialize Skills
            List<SkillsMongoDto> skills = new ArrayList<>();
            Rank defaultSkillLootRate = Rank.COMMON;

            // Monster 1 Skills
            skills.add(new SkillsMongoDto(m1.getId(), 125.0, new RatioDto(Stat.ATK, 0.25), 0.0, 5.0,
                    defaultSkillLootRate));
            skills.add(new SkillsMongoDto(m1.getId(), 250.0, new RatioDto(Stat.ATK, 0.275), 2.0, 7.0,
                    defaultSkillLootRate));
            skills.add(new SkillsMongoDto(m1.getId(), 425.0, new RatioDto(Stat.ATK, 0.40), 5.0, 5.0,
                    defaultSkillLootRate));

            // Monster 2 Skills
            skills.add(new SkillsMongoDto(m2.getId(), 200.0, new RatioDto(Stat.DEF, 0.10), 0.0, 4.0,
                    defaultSkillLootRate));
            skills.add(new SkillsMongoDto(m2.getId(), 315.0, new RatioDto(Stat.DEF, 0.175), 2.0, 5.0,
                    defaultSkillLootRate));
            skills.add(new SkillsMongoDto(m2.getId(), 525.0, new RatioDto(Stat.DEF, 0.20), 6.0, 7.0,
                    defaultSkillLootRate));

            // Monster 3 Skills
            skills.add(
                    new SkillsMongoDto(m3.getId(), 150.0, new RatioDto(Stat.HP, 0.05), 0.0, 7.0, defaultSkillLootRate));
            skills.add(
                    new SkillsMongoDto(m3.getId(), 350.0, new RatioDto(Stat.HP, 0.07), 2.0, 4.0, defaultSkillLootRate));
            skills.add(new SkillsMongoDto(m3.getId(), 250.0, new RatioDto(Stat.ATK, 0.12), 5.0, 5.0,
                    defaultSkillLootRate));

            // Monster 4 Skills
            skills.add(new SkillsMongoDto(m4.getId(), 150.0, new RatioDto(Stat.ATK, 0.275), 0.0, 6.0,
                    defaultSkillLootRate));
            skills.add(new SkillsMongoDto(m4.getId(), 285.0, new RatioDto(Stat.ATK, 0.275), 2.0, 9.0,
                    defaultSkillLootRate));
            skills.add(new SkillsMongoDto(m4.getId(), 550.0, new RatioDto(Stat.ATK, 0.60), 4.0, 4.0,
                    defaultSkillLootRate));

            skillsRepository.saveAll(skills);

            System.out.println("Base data inserted into MongoDB via DatabaseSeeder!");
        }
    }
}
