package com.imt.api_invocations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.exception.ResourceNotFoundException;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperSkills;
import com.imt.api_invocations.service.SkillsService;

@RestController
@RequestMapping("/api/invocation/skills")
@RequiredArgsConstructor
public class SkillsController {

    private final SkillsService skillsService;
    private final DtoMapperSkills dtoMapper;

    /**
     * Create a new skill
     * 
     * @param skillsHttpDto the skill data
     * @return the ID of the created skill
     */
    @PostMapping
    public ResponseEntity<String> createSkill(@Valid @RequestBody SkillsHttpDto skillsHttpDto) {
        String skillId = skillsService.createSkill(dtoMapper.toSkillsMongoDto(skillsHttpDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(skillId);
    }

    /**
     * Get a skill by ID
     * 
     * @param skillId the skill ID
     * @return the skill details
     */
    @GetMapping("/{skillId}")
    public ResponseEntity<SkillsWithIdDto> getSkillById(@PathVariable String skillId) {
        var skillMongoDto = skillsService.getSkillById(skillId);
        if (skillMongoDto == null) {
            throw new ResourceNotFoundException("Skill", skillId);
        }
        return ResponseEntity.ok(dtoMapper.toSkillsDto(skillMongoDto));
    }

    /**
     * Get skill by monster ID
     * 
     * @param monsterId the monster ID
     * @return the skill details
     */
    @GetMapping("/monster/{monsterId}")
    public ResponseEntity<List<SkillsWithIdDto>> getSkillByMonsterId(@PathVariable String monsterId) {
        var skills = skillsService.getSkillByMonsterId(monsterId);
        if (skills.isEmpty()) {
            throw new ResourceNotFoundException("No skills found for monster with ID " + monsterId);
        }
        return ResponseEntity.ok(skills.stream().map(dtoMapper::toSkillsDto).toList());
    }

    /**
     * Update a skill
     * 
     * @param skillId       the skill ID
     * @param skillsHttpDto the updated skill data
     * @return no content response
     */
    @PutMapping("/{skillId}")
    public ResponseEntity<Void> updateSkill(
            @PathVariable String skillId,
            @Valid @RequestBody SkillsHttpDto skillsHttpDto) {
        var existingSkill = skillsService.getSkillById(skillId);
        if (existingSkill == null) {
            throw new ResourceNotFoundException("Skill", skillId);
        }
        var updatedSkill = dtoMapper.updateSkillsMongoDto(existingSkill, skillsHttpDto);
        skillsService.updateSkill(skillId, updatedSkill);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a skill by ID
     * 
     * @param skillId the skill ID
     * @return response with deletion status
     */
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Map<String, Boolean>> deleteSkillById(@PathVariable String skillId) {
        boolean isDeleted = skillsService.deleteSkillById(skillId);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("deleted", false));
        }
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    /**
     * Delete skill by monster ID
     * 
     * @param monsterId the monster ID
     * @return response with deleted count
     */
    @DeleteMapping("/monster/{monsterId}")
    public ResponseEntity<Map<String, Long>> deleteSkillByMonsterId(@PathVariable String monsterId) {
        long deletedCount = skillsService.deleteSkillByMonsterId(monsterId);
        return ResponseEntity.ok(Map.of("deletedCount", deletedCount));
    }

}
