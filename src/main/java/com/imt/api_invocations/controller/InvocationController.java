package com.imt.api_invocations.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithoutRankDto;
import com.imt.api_invocations.controller.dto.output.InvocationReplayResponse;
import com.imt.api_invocations.controller.mapper.DtoMapperInvocation;
import com.imt.api_invocations.service.InvocationService;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.dto.InvocationReplayReport;

@RestController
@RequestMapping("/api/invocation/")
@RequiredArgsConstructor
public class InvocationController {

    private final InvocationService invocationService;
    private final DtoMapperInvocation dtoMapperInvocation;

    @GetMapping("invoque")
    public ResponseEntity<GlobalMonsterDto> invoque() {
        GlobalMonsterDto result = invocationService.invoke();
        return ResponseEntity.ok(result);
    }

    @GetMapping("global-invoque/{playerId}")
    public ResponseEntity<GlobalMonsterWithoutRankDto> globalInvoque(@PathVariable String playerId) {
        GlobalMonsterDto result = invocationService.globalInvoke(playerId);
        return ResponseEntity.ok(dtoMapperInvocation.toGlobalMonsterWithoutRankDto(result));
    }

    @PostMapping("recreate")
    public ResponseEntity<InvocationReplayResponse> recreateInvocations() {
        InvocationReplayReport report = invocationService.replayBufferedInvocations();
        return ResponseEntity.ok(dtoMapperInvocation.toInvocationReplayResponse(report));
    }
}
