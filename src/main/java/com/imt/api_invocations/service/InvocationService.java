package com.imt.api_invocations.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.client.dto.player.PlayerAddMonsterRequest;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.client.MonstersApiClient;
import com.imt.api_invocations.client.PlayerApiClient;
import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.exception.ExternalApiException;
import com.imt.api_invocations.persistence.InvocationBufferRepository;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;
import static com.imt.api_invocations.utils.Random.*;

@Service
public class InvocationService {

    private static final Logger logger = LoggerFactory.getLogger(InvocationService.class);

    private final MonsterService monsterService;
    private final SkillsService skillsService;
    private final MonstersApiClient monstersApiClient;
    private final PlayerApiClient playerApiClient;
    private final InvocationBufferRepository invocationBufferRepository;

    public InvocationService(MonsterService monsterService,
            SkillsService skillsService,
            MonstersApiClient monstersApiClient,
            PlayerApiClient playerApiClient,
            InvocationBufferRepository invocationBufferRepository) {
        this.monsterService = monsterService;
        this.skillsService = skillsService;
        this.monstersApiClient = monstersApiClient;
        this.playerApiClient = playerApiClient;
        this.invocationBufferRepository = invocationBufferRepository;
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

    private CreateMonsterRequest toCreateMonsterRequest(GlobalMonsterDto monster) {
        return new CreateMonsterRequest(
                monster.getElement(),
                (int) monster.getHp(),
                (int) monster.getAtk(),
                (int) monster.getDef(),
                (int) monster.getVit(),
                monster.getSkills(),
                monster.getRank());
    }

    private InvocationBufferDto createBufferEntry(String playerId, GlobalMonsterDto monster,
            CreateMonsterRequest monsterRequest) {
        InvocationBufferDto bufferEntry = InvocationBufferDto.builder()
                .playerId(playerId)
                .monsterSnapshot(monster)
                .monsterRequest(monsterRequest)
                .status(InvocationStatus.PENDING)
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        return invocationBufferRepository.save(bufferEntry);
    }

    private InvocationBufferDto markAttempt(InvocationBufferDto entry) {
        entry.setAttemptCount(entry.getAttemptCount() + 1);
        entry.setLastAttemptAt(LocalDateTime.now());
        return invocationBufferRepository.save(entry);
    }

    private void markMonsterCreated(InvocationBufferDto entry, CreateMonsterResponse response) {
        entry.setMonsterResponse(response);
        entry.setStatus(InvocationStatus.MONSTER_CREATED);
        entry.setFailureReason(null);
        invocationBufferRepository.save(entry);
    }

    private void markCompleted(InvocationBufferDto entry, PlayerAddMonsterRequest request, PlayerResponse response) {
        entry.setPlayerRequest(request);
        entry.setPlayerResponse(response);
        entry.setStatus(InvocationStatus.COMPLETED);
        entry.setFailureReason(null);
        invocationBufferRepository.save(entry);
    }

    private void markFailed(InvocationBufferDto entry, String reason) {
        entry.setStatus(InvocationStatus.FAILED);
        entry.setFailureReason(reason);
        invocationBufferRepository.save(entry);
    }

    private GlobalMonsterDto executeInvocation(GlobalMonsterDto monster, String playerId,
            InvocationBufferDto bufferEntry) {
        if (bufferEntry.getMonsterRequest() == null) {
            bufferEntry.setMonsterRequest(toCreateMonsterRequest(monster));
            bufferEntry = invocationBufferRepository.save(bufferEntry);
        }

        bufferEntry = markAttempt(bufferEntry);

        String createdMonsterId = null;

        try {
            CreateMonsterResponse monsterResponse = monstersApiClient.createMonster(bufferEntry.getMonsterRequest());
            markMonsterCreated(bufferEntry, monsterResponse);
            createdMonsterId = monsterResponse.getMonsterId();

            PlayerAddMonsterRequest playerRequest = new PlayerAddMonsterRequest(createdMonsterId);
            bufferEntry.setPlayerRequest(playerRequest);
            invocationBufferRepository.save(bufferEntry);
            PlayerResponse playerResponse = playerApiClient.addMonsterToPlayer(playerId, createdMonsterId);

            markCompleted(bufferEntry, playerRequest, playerResponse);

            logger.info("Invocation globale réussie. Monstre {} ajouté au joueur {}", createdMonsterId, playerId);
            return monster;

        } catch (ExternalApiException e) {
            logger.error("Échec de l'invocation globale: {}", e.getMessage());

            if (bufferEntry != null) {
                markFailed(bufferEntry, e.getMessage());
            }

            if (createdMonsterId != null) {
                logger.warn("Déclenchement de la compensation: suppression du monstre {}", createdMonsterId);
                try {
                    monstersApiClient.deleteMonster(createdMonsterId);
                } catch (Exception compensationError) {
                    logger.error("Échec de la compensation pour le monstre {}", createdMonsterId, compensationError);
                }
            }

            throw e;
        }
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

        GlobalMonsterDto monster = invoke();
        CreateMonsterRequest monsterRequest = toCreateMonsterRequest(monster);

        InvocationBufferDto bufferEntry = createBufferEntry(playerId, monster, monsterRequest);

        return executeInvocation(monster, playerId, bufferEntry);
    }

    public InvocationReplayReport replayBufferedInvocations() {
        List<InvocationBufferDto> entries = invocationBufferRepository.findRecreatable();
        int successCount = 0;
        List<String> failedIds = new ArrayList<>();

        for (InvocationBufferDto entry : entries) {
            try {
                GlobalMonsterDto snapshot = entry.getMonsterSnapshot();
                if (snapshot == null) {
                    markFailed(entry, "No monster snapshot available for replay");
                    failedIds.add(entry.getId());
                    continue;
                }
                executeInvocation(snapshot, entry.getPlayerId(), entry);
                successCount++;
            } catch (ExternalApiException e) {
                failedIds.add(entry.getId());
            }
        }

        int total = entries.size();
        return new InvocationReplayReport(total, successCount, total - successCount, failedIds);
    }
}
