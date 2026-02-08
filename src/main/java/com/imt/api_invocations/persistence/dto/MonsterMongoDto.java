package com.imt.api_invocations.persistence.dto;

import com.imt.api_invocations.dto.MonsterBaseDto;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "monsters")
@AttributeOverride(name = "stats.hp", column = @Column(name = "stats_hp"))
@AttributeOverride(name = "stats.atk", column = @Column(name = "stats_atk"))
@AttributeOverride(name = "stats.def", column = @Column(name = "stats_def"))
@AttributeOverride(name = "stats.vit", column = @Column(name = "stats_vit"))
public class MonsterMongoDto extends MonsterBaseDto {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false, length = 36)
  private String id;

  @OneToMany(mappedBy = "monster", fetch = FetchType.LAZY)
  private List<SkillsMongoDto> skills;
}
