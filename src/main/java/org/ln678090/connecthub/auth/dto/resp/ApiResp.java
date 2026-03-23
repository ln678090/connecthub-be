package org.ln678090.connecthub.auth.dto.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResp<T> (
    String message,
    T data,
    String timestamp
) {}
