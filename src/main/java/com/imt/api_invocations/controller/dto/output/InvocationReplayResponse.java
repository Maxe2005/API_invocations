package com.imt.api_invocations.controller.dto.output;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvocationReplayResponse {
    private final int retried;
    private final int succeeded;
    private final int failed;
    private final List<String> failedInvocationIds;
}
