package com.imt.api_invocations.persistence.entity;

import com.imt.api_invocations.dto.SkillBaseDto;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "skills")
@AttributeOverride(name = "ratio.stat", column = @Column(name = "ratio_stat"))
@AttributeOverride(name = "ratio.percent", column = @Column(name = "ratio_percent"))
public class SkillEntity extends SkillBaseDto {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "monster_id", nullable = false, length = 36)
    private String monsterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monster_id", insertable = false, updatable = false)
    private MonsterEntity monster;
}
