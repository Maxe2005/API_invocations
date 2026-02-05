package com.imt.api_invocations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.input.MonsterHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperMonster;
import com.imt.api_invocations.service.MonsterService;

import java.util.List;
import java.util.Map;

import com.imt.api_invocations.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/invocation/monsters")
@RequiredArgsConstructor
public class MonsterController {

    private final MonsterService monsterService;
    private final DtoMapperMonster dtoMapper;

    /**
     * Create a new monster
     * 
     * @param monsterHttpDto the monster data
     * @return the ID of the created monster
     */
    @PostMapping("create")
    public ResponseEntity<String> createMonster(@Valid @RequestBody MonsterHttpDto monsterHttpDto) {
        String monsterId = monsterService.createMonster(dtoMapper.toMonsterMongoDto(monsterHttpDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(monsterId);
    }

    /**
     * Get a monster by ID
     * 
     * @param monsterId the monster ID
     * @return the monster details
     */
    @GetMapping("/{monsterId}")
    public ResponseEntity<GlobalMonsterWithIdDto> getMonsterById(@PathVariable String monsterId) {
        var monsterMongoDto = monsterService.getMonsterById(monsterId);
        if (monsterMongoDto == null) {
            throw new ResourceNotFoundException("Monster", monsterId);
        }
        return ResponseEntity.ok(dtoMapper.toGlobalMonsterWithIdDto(monsterMongoDto));
    }

    /**
     * Get all monsters
     * 
     * @return list of all monsters
     */
    @GetMapping("/all")
    public ResponseEntity<List<GlobalMonsterWithIdDto>> getAllMonsters() {
        var monsters = monsterService.getAllMonsters();
        List<GlobalMonsterWithIdDto> monsterDtos = monsters
                .stream()
                .map(dtoMapper::toGlobalMonsterWithIdDto)
                .toList();
        return ResponseEntity.ok(monsterDtos);
    }

    /**
     * Update a monster
     * 
     * @param monsterId        the monster ID
     * @param monsterUpdateDto the updated monster data
     * @return no content response
     */
    @PutMapping("/{monsterId}")
    public ResponseEntity<Void> updateMonster(
            @PathVariable String monsterId,
            @Valid @RequestBody MonsterHttpUpdateDto monsterUpdateDto) {
        var existingMonster = monsterService.getMonsterById(monsterId);
        if (existingMonster == null) {
            throw new ResourceNotFoundException("Monster", monsterId);
        }
        var updatedMonster = dtoMapper.updateMonsterMongoDto(existingMonster, monsterUpdateDto);
        monsterService.updateMonster(monsterId, updatedMonster);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a monster by ID
     * 
     * @param monsterId the monster ID
     * @return response with deletion status
     */
    @DeleteMapping("/{monsterId}")
    public ResponseEntity<Map<String, Boolean>> deleteMonster(@PathVariable @Valid String monsterId) {
        boolean isDeleted = monsterService.deleteMonsterById(monsterId);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("deleted", false));
        }
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
