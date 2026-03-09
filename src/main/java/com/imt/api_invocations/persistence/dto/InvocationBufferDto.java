package com.imt.api_invocations.persistence.dto;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.client.dto.player.PlayerAddMonsterRequest;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "invocation_buffer")
public class InvocationBufferDto {

    @MongoId
    private String id;
    private String playerId;
    private GlobalMonsterDto monsterSnapshot;
    private CreateMonsterRequest monsterRequest;
    private CreateMonsterResponse monsterResponse;
    private PlayerAddMonsterRequest playerRequest;
    private PlayerResponse playerResponse;
    private InvocationStatus status;
    private String failureReason;
    private int attemptCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastAttemptAt;
}
