package com.imt.api_invocations.controller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.imt.api_invocations.controller.dto.output.InvocationReplayResponse;
import com.imt.api_invocations.service.dto.InvocationReplayReport;

@DisplayName("DtoMapperInvocation - Tests Unitaires")
class DtoMapperInvocationTest {

    private final DtoMapperInvocation mapper = new DtoMapperInvocation();

    @Test
    @DisplayName("toInvocationReplayResponse mappe tous les champs")
    void should_MapAllFields_When_ReportProvided() {
        InvocationReplayReport report =
                new InvocationReplayReport(5, 3, 2, List.of("inv-1", "inv-2"));

        InvocationReplayResponse result = mapper.toInvocationReplayResponse(report);

        assertThat(result).isNotNull();
        assertThat(result.getRetried()).isEqualTo(5);
        assertThat(result.getSucceeded()).isEqualTo(3);
        assertThat(result.getFailed()).isEqualTo(2);
        assertThat(result.getFailedInvocationIds()).containsExactly("inv-1", "inv-2");
    }

    @Test
    @DisplayName("toInvocationReplayResponse gère les collections vides")
    void should_HandleEmptyCollections_When_ReportHasNoFailures() {
        InvocationReplayReport report = new InvocationReplayReport(2, 2, 0, List.of());

        InvocationReplayResponse result = mapper.toInvocationReplayResponse(report);

        assertThat(result.getFailedInvocationIds()).isEmpty();
    }
}
