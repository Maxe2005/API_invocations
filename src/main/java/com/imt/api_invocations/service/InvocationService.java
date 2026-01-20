package com.imt.api_invocations.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.imt.api_invocations.client.MonstersApiClient;
import com.imt.api_invocations.client.PlayerApiClient;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.exception.ExternalApiException;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;
import static com.imt.api_invocations.utils.Random.*;

@Service
public class InvocationService {

    private static final Logger logger = LoggerFactory.getLogger(InvocationService.class);

    private final MonsterService monsterService;
    private final SkillsService skillsService;
    private final MonstersApiClient monstersApiClient;
    private final PlayerApiClient playerApiClient;

    public InvocationService(MonsterService monsterService,
            SkillsService skillsService,
            MonstersApiClient monstersApiClient,
            PlayerApiClient playerApiClient) {
        this.monsterService = monsterService;
        this.skillsService = skillsService;
        this.monstersApiClient = monstersApiClient;
        this.playerApiClient = playerApiClient;
    }

    private GlobalMonsterDto mapToGlobalMonsterDto(MonsterMongoDto monsterMongoDto, List<SkillForMonsterDto> skills) {
        return new GlobalMonsterDto(
                monsterMongoDto.getElement(),
                monsterMongoDto.getHp(),
                monsterMongoDto.getAtk(),
                monsterMongoDto.getDef(),
                monsterMongoDto.getVit(),
                skills,
                monsterMongoDto.getRank());
    }

    public GlobalMonsterDto invoke() {
        Rank rank = getRandomRankBasedOnAvailableData(monsterService);
        MonsterMongoDto monster = monsterService.getRandomMonsterByRank(rank);
        List<SkillForMonsterDto> skills = skillsService.getRandomSkillsForMonster(monster.getId(), 3);
        return mapToGlobalMonsterDto(monster, skills);
    }

    /**
     * Invoque un monstre et l'ajoute au joueur via les APIs externes.
     * Utilise le pattern Saga avec compensation en cas d'échec.
     * 
     * @param playerId L'ID du joueur qui reçoit le monstre
     * @return Le monstre invoqué
     * @throws ExternalApiException En cas d'erreur de communication avec les APIs
     *                              externes
     */
    public GlobalMonsterDto globalInvoke(String playerId) {
        logger.info("Début de l'invocation globale pour le joueur: {}", playerId);

        String createdMonsterId = null;

        try {
            // Étape 1: Invoquer un monstre localement
            logger.info("Étape 1: Invocation du monstre");
            GlobalMonsterDto monster = invoke();

            // Étape 2: Créer le monstre dans l'API Monsters
            logger.info("Étape 2: Création du monstre dans l'API externe");
            createdMonsterId = monstersApiClient.createMonster(monster);

            // Étape 3: Ajouter le monstre au joueur
            logger.info("Étape 3: Ajout du monstre au joueur");
            playerApiClient.addMonsterToPlayer(playerId, createdMonsterId);

            logger.info("Invocation globale réussie. Monstre {} ajouté au joueur {}", createdMonsterId, playerId);
            return monster;

        } catch (ExternalApiException e) {
            logger.error("Échec de l'invocation globale: {}", e.getMessage());

            // Compensation: Si le monstre a été créé mais l'ajout au joueur a échoué, on
            // supprime le monstre
            if (createdMonsterId != null) {
                logger.warn("Déclenchement de la compensation: suppression du monstre {}", createdMonsterId);
                try {
                    monstersApiClient.deleteMonster(createdMonsterId);
                } catch (Exception compensationError) {
                    logger.error("Échec de la compensation pour le monstre {}", createdMonsterId, compensationError);
                }
            }

            // Relancer l'exception pour que le contrôleur puisse la gérer
            throw e;
        }
    }
}
