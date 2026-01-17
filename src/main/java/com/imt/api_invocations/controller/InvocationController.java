package com.imt.api_invocations.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.imt.api_invocations.service.InvocationService;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;



@RestController
@RequestMapping("/api/invocation/")
@RequiredArgsConstructor
public class InvocationController {
    
    private final InvocationService invocationService;

    @GetMapping("invoque")
    public ResponseEntity<GlobalMonsterDto> invoque() {
        GlobalMonsterDto result = invocationService.invoke();
        return ResponseEntity.ok(result);
    }
}
