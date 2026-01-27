package com.imt.api_invocations.service;

import org.springframework.stereotype.Service;
import java.util.List;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import static com.imt.api_invocations.utils.Random.random;
import com.imt.api_invocations.utils.DataServiceInterface;

@Service
public class MonsterService implements DataServiceInterface {

    private final MonsterRepository monsterRepository;

    public MonsterService(MonsterRepository monsterRepository) {
        this.monsterRepository = monsterRepository;
    }

    public String createMonster(MonsterMongoDto monsterMongoDto) {
        return monsterRepository.save(monsterMongoDto);
    }

    public MonsterMongoDto getMonsterById(String id) {
        return monsterRepository.findByID(id);
    }

    public List<MonsterMongoDto> getAllMonsters() {
        return monsterRepository.findAll();
    }

    public List<String> getAllMonsterIdByRank(Rank rank) {
        return monsterRepository.findAllMonsterIdByRank(rank).stream().map(MonsterMongoDto::getId).toList();
    }

    public void updateMonster(String monsterId, MonsterMongoDto monsterMongoDto) {
        MonsterMongoDto monsterToUpdate = new MonsterMongoDto(
                monsterId,
                monsterMongoDto.getName(),
                monsterMongoDto.getElement(),
                monsterMongoDto.getHp(),
                monsterMongoDto.getAtk(),
                monsterMongoDto.getDef(),
                monsterMongoDto.getVit(),
                monsterMongoDto.getRank(),
                monsterMongoDto.getVisualDescription(),
                monsterMongoDto.getCardDescription(),
                monsterMongoDto.getImageUrl());
        monsterRepository.update(monsterToUpdate);
    }

    public boolean deleteMonsterById(String id) {
        return monsterRepository.deleteByID(id);
    }

    public MonsterMongoDto getRandomMonsterByRank(Rank rank) {
        List<String> monsterIds = getAllMonsterIdByRank(rank);
        String selectedMonsterId = monsterIds.get(random(0, monsterIds.size() - 1));
        return getMonsterById(selectedMonsterId);
    }

    @Override
    public boolean hasAvailableData(Rank rank) {
        List<String> monsterIds = getAllMonsterIdByRank(rank);
        return !monsterIds.isEmpty();
    }

}
