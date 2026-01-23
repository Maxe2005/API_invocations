package com.imt.api_invocations.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvocationReplayReport {
    private final int retried;
    private final int succeeded;
    private final int failed;
    private final List<String> failedInvocationIds;
}
