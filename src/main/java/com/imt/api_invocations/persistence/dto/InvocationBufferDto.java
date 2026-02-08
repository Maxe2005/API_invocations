package com.imt.api_invocations.persistence.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.client.dto.player.PlayerAddMonsterRequest;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import com.imt.api_invocations.enums.InvocationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invocation_buffer")
public class InvocationBufferDto {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;
    private String playerId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "monster_snapshot", columnDefinition = "jsonb")
    private GlobalMonsterDto monsterSnapshot;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "monster_request", columnDefinition = "jsonb")
    private CreateMonsterRequest monsterRequest;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "monster_response", columnDefinition = "jsonb")
    private CreateMonsterResponse monsterResponse;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "player_request", columnDefinition = "jsonb")
    private PlayerAddMonsterRequest playerRequest;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "player_response", columnDefinition = "jsonb")
    private PlayerResponse playerResponse;
    @Enumerated(EnumType.STRING)
    private InvocationStatus status;
    private String failureReason;
    private int attemptCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastAttemptAt;
}
