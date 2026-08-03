package com.imt.api_invocations.service;

import static com.imt.api_invocations.utils.Random.getRandomRankBasedOnAvailableData;

import com.imt.api_invocations.client.MonstersApiClient;
import com.imt.api_invocations.client.PlayerApiClient;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.client.dto.player.PlayerAddMonsterRequest;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.config.InvocationProperties;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import com.imt.api_invocations.dto.SkillBaseDto;
import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.exception.ExternalApiException;
import com.imt.api_invocations.persistence.InvocationBufferRepository;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
import com.imt.api_invocations.service.mapper.InvocationServiceMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InvocationService {

  private static final Logger logger = LoggerFactory.getLogger(InvocationService.class);

  private final MonsterService monsterService;
  private final SkillsService skillsService;
  private final MonstersApiClient monstersApiClient;
  private final PlayerApiClient playerApiClient;
  private final InvocationBufferRepository invocationBufferRepository;
  private final InvocationServiceMapper invocationServiceMapper;
  private final InvocationProperties invocationProperties;

  public InvocationService(
      MonsterService monsterService,
      SkillsService skillsService,
      MonstersApiClient monstersApiClient,
      PlayerApiClient playerApiClient,
      InvocationBufferRepository invocationBufferRepository,
      InvocationServiceMapper invocationServiceMapper,
      InvocationProperties invocationProperties) {
    this.monsterService = monsterService;
    this.skillsService = skillsService;
    this.monstersApiClient = monstersApiClient;
    this.playerApiClient = playerApiClient;
    this.invocationBufferRepository = invocationBufferRepository;
    this.invocationServiceMapper = invocationServiceMapper;
    this.invocationProperties = invocationProperties;
  }

  public GlobalMonsterDto invoke() {
    Rank rank = getRandomRankBasedOnAvailableData(monsterService);
    MonsterEntity monster = monsterService.getRandomMonsterByRank(rank);
    List<SkillBaseDto> skills = skillsService.getRandomSkillsForMonster(monster.getId(), 3);
    return invocationServiceMapper.toGlobalMonsterDto(monster, skills);
  }

  private InvocationBufferDto createBufferEntry(
      String playerId, GlobalMonsterDto monster, CreateMonsterRequest monsterRequest) {
    InvocationBufferDto bufferEntry =
        InvocationBufferDto.builder()
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

  private void markCompleted(
      InvocationBufferDto entry, PlayerAddMonsterRequest request, PlayerResponse response) {
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

  private void markAbandoned(InvocationBufferDto entry, String reason) {
    entry.setStatus(InvocationStatus.ABANDONED);
    entry.setFailureReason(reason);
    invocationBufferRepository.save(entry);
  }

  private String executeInvocation(
      GlobalMonsterDto monster, String playerId, InvocationBufferDto bufferEntry) {
    if (bufferEntry.getAttemptCount() >= invocationProperties.getMaxAttempts()) {
      String reason =
          "Nombre maximal de tentatives ("
              + invocationProperties.getMaxAttempts()
              + ") atteint pour l'invocation "
              + bufferEntry.getId()
              + ", abandon définitif";
      logger.warn(reason);
      markAbandoned(bufferEntry, reason);
      throw new ExternalApiException(reason);
    }

    if (bufferEntry.getMonsterRequest() == null) {
      bufferEntry.setMonsterRequest(
          invocationServiceMapper.toCreateMonsterRequest(monster, playerId));
      bufferEntry = invocationBufferRepository.save(bufferEntry);
    }

    bufferEntry = markAttempt(bufferEntry);

    String createdMonsterId = null;

    try {
      CreateMonsterResponse monsterResponse =
          monstersApiClient.createMonster(bufferEntry.getMonsterRequest());
      markMonsterCreated(bufferEntry, monsterResponse);
      createdMonsterId = monsterResponse.getMonsterId();

      PlayerAddMonsterRequest playerRequest = new PlayerAddMonsterRequest(createdMonsterId);
      bufferEntry.setPlayerRequest(playerRequest);
      invocationBufferRepository.save(bufferEntry);
      PlayerResponse playerResponse =
          playerApiClient.addMonsterToPlayer(playerId, createdMonsterId);

      markCompleted(bufferEntry, playerRequest, playerResponse);

      logger.info(
          "Invocation globale réussie. Monstre {} ajouté au joueur {}", createdMonsterId, playerId);
      return createdMonsterId;

    } catch (ExternalApiException e) {
      logger.error("Échec de l'invocation globale: {}", e.getMessage());

      markFailed(bufferEntry, e.getMessage());

      if (createdMonsterId != null) {
        compensate(bufferEntry, createdMonsterId, e.getMessage());
      }

      throw e;
    }
  }

  /**
   * Tente de supprimer le monstre orphelin créé avant l'échec, avec une deuxième tentative
   * immédiate en cas d'échec de la première. Si les deux tentatives échouent, la buffer entry est
   * marquée avec une raison explicite mentionnant le monstre orphelin, pour rester traçable même
   * sans retry différé ni alerting dédié.
   */
  private void compensate(
      InvocationBufferDto bufferEntry, String createdMonsterId, String originalFailureReason) {
    logger.warn("Déclenchement de la compensation: suppression du monstre {}", createdMonsterId);

    boolean deleted = monstersApiClient.deleteMonster(createdMonsterId);
    if (!deleted) {
      logger.warn("Nouvelle tentative de compensation pour le monstre {}", createdMonsterId);
      deleted = monstersApiClient.deleteMonster(createdMonsterId);
    }

    if (!deleted) {
      String reason =
          originalFailureReason
              + " | COMPENSATION ÉCHOUÉE après 2 tentatives : "
              + "le monstre "
              + createdMonsterId
              + " est probablement orphelin (créé mais non "
              + "supprimé et jamais ajouté au joueur) et nécessite une intervention manuelle.";
      logger.error(reason);
      markFailed(bufferEntry, reason);
    }
  }

  /**
   * Invoque un monstre et l'ajoute au joueur via les APIs externes. Utilise le pattern Saga avec
   * compensation en cas d'échec.
   *
   * @param playerId L'ID du joueur qui reçoit le monstre
   * @return Le monstre invoqué
   * @throws ExternalApiException En cas d'erreur de communication avec les APIs externes
   */
  public GlobalMonsterWithIdDto globalInvoke(String playerId) {
    logger.info("Début de l'invocation globale pour le joueur: {}", playerId);

    GlobalMonsterDto monster = invoke();
    CreateMonsterRequest monsterRequest =
        invocationServiceMapper.toCreateMonsterRequest(monster, playerId);

    InvocationBufferDto bufferEntry = createBufferEntry(playerId, monster, monsterRequest);

    String createdMonsterId = executeInvocation(monster, playerId, bufferEntry);
    return invocationServiceMapper.toGlobalMonsterWithIdDto(monster, createdMonsterId);
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
