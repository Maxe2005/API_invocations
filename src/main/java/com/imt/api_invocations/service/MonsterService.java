package com.imt.api_invocations.service;

import org.springframework.stereotype.Service;
import java.util.List;

import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

@Service
public class MonsterService {

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

    public List<String> getAllMonsterIds() {
        return monsterRepository.findAllIds().stream().map(MonsterMongoDto::getId).toList();
    }

    public void updateMonster(String monsterId, MonsterMongoDto monsterMongoDto) {
        MonsterMongoDto monsterToUpdate = new MonsterMongoDto(
                monsterId,
                monsterMongoDto.getElement(),
                monsterMongoDto.getHp(),
                monsterMongoDto.getAtk(),
                monsterMongoDto.getDef(),
                monsterMongoDto.getVit(),
                monsterMongoDto.getLootRate());
        monsterRepository.update(monsterToUpdate);
    }

    public boolean deleteMonsterById(String id) {
        return monsterRepository.deleteByID(id);
    }

}
