package com.imt.api_invocations.client.dto.gatcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignedUrlResponse {
    private String id;

    @JsonProperty("input_url")
    private String inputUrl;

    @JsonProperty("signed_url")
    private String signedUrl;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    private String error;
}
