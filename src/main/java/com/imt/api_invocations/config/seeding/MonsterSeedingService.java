package com.imt.api_invocations.config.seeding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonsterSeedingService {

    private static final Logger logger = LoggerFactory.getLogger(MonsterSeedingService.class);
    private static final String MONSTERS_PATTERN = "classpath:monsters/*.json";

    private final ObjectMapper objectMapper;

    public MonsterSeedingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Charge tous les monstres depuis le répertoire monsters/
     */
    public List<MonsterSeedDto> loadAllMonsters() throws IOException {
        return loadMonstersFromPattern(MONSTERS_PATTERN);
    }

    /**
     * Charge les monstres depuis un pattern de fichiers spécifique
     * 
     * @param pattern le pattern de fichiers (ex: classpath:monsters/*.json)
     */
    public List<MonsterSeedDto> loadMonstersFromPattern(String pattern) throws IOException {
        List<MonsterSeedDto> allMonsters = new ArrayList<>();

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pattern);

            logger.info("Found {} monster files matching pattern: {}", resources.length, pattern);

            for (Resource resource : resources) {
                try {
                    MonsterSeedDto monster = objectMapper.readValue(
                            resource.getInputStream(),
                            MonsterSeedDto.class
                    );
                    allMonsters.add(monster);
                    logger.debug("Loaded monster: {}", monster.getNom());
                } catch (IOException e) {
                    logger.warn("Failed to load monster from file: {}", resource.getFilename(), e);
                }
            }

            logger.info("Successfully loaded {} monsters", allMonsters.size());
            return allMonsters;

        } catch (IOException e) {
            logger.error("Failed to load monsters from pattern: {}", pattern, e);
            throw e;
        }
    }

    /**
     * Charge un monstre spécifique depuis un fichier
     * 
     * @param fileName le nom du fichier dans le répertoire monsters/
     */
    public MonsterSeedDto loadMonster(String fileName) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("monsters/" + fileName);
            MonsterSeedDto monster = objectMapper.readValue(
                    resource.getInputStream(),
                    MonsterSeedDto.class
            );
            logger.info("Loaded monster from file: {}", fileName);
            return monster;
        } catch (IOException e) {
            logger.error("Failed to load monster from file: {}", fileName, e);
            throw e;
        }
    }
}
