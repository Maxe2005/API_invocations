package com.imt.api_invocations.controller.mapper;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithoutRankDto;
import com.imt.api_invocations.controller.dto.output.InvocationReplayResponse;
import com.imt.api_invocations.controller.dto.output.SkillForMonsterWithoutRankDto;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;

import java.util.List;

/**
 * Mapper for converting between DTOs and entities.
 * Applies DRY principle by centralizing all DTO conversions.
 */
@Component
public class DtoMapperInvocation {

    public SkillForMonsterWithoutRankDto toSkillForMonsterWithoutRankDto(SkillForMonsterDto skillForMonsterDto) {
        return new SkillForMonsterWithoutRankDto(
                skillForMonsterDto.getNumber(),
                skillForMonsterDto.getDamage(),
                skillForMonsterDto.getRatio(),
                skillForMonsterDto.getCooldown(),
                skillForMonsterDto.getLvlMax());
    }

    private List<SkillForMonsterWithoutRankDto> mapSkills(GlobalMonsterDto globalMonsterDto) {
        return globalMonsterDto.getSkills().stream()
                .map(this::toSkillForMonsterWithoutRankDto)
                .toList();
    }

    public GlobalMonsterWithoutRankDto toGlobalMonsterWithoutRankDto(GlobalMonsterDto globalMonsterDto) {
        return new GlobalMonsterWithoutRankDto(
                globalMonsterDto.getElement(),
                globalMonsterDto.getHp(),
                globalMonsterDto.getAtk(),
                globalMonsterDto.getDef(),
                globalMonsterDto.getVit(),
                globalMonsterDto.getImageUrl(),
                mapSkills(globalMonsterDto));
    }

    public InvocationReplayResponse toInvocationReplayResponse(InvocationReplayReport report) {
        return new InvocationReplayResponse(
                report.getRetried(),
                report.getSucceeded(),
                report.getFailed(),
                report.getFailedInvocationIds());
    }
}
