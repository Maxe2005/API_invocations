package com.imt.api_invocations.client.dto.monsters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CreateMonsterRequest extends GlobalMonsterDto {

    @Override
    @JsonIgnore
    public String getVisualDescription() {
        return super.getVisualDescription();
    }
}
