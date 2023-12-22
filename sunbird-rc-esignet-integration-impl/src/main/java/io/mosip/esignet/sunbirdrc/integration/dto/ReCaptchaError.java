package io.mosip.esignet.sunbirdrc.integration.dto;

import lombok.Data;

@Data
public class ReCaptchaError {

    private String errorCode;
    private String message;
}
