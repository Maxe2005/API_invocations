package com.imt.api_invocations.client.dto.gatcha;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignedUrlRequest {
    private String id;
    private String url;
}
