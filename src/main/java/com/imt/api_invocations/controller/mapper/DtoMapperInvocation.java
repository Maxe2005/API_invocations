package com.imt.api_invocations.controller.mapper;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.controller.dto.output.InvocationReplayResponse;
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

    public InvocationReplayResponse toInvocationReplayResponse(InvocationReplayReport report) {
        return new InvocationReplayResponse(
                report.getRetried(),
                report.getSucceeded(),
                report.getFailed(),
                report.getFailedInvocationIds());
    }
}
