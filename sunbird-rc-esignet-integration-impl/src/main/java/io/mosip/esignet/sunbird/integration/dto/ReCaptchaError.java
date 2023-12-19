package io.mosip.esignet.sunbird.integration.dto;

import lombok.Data;

@Data
public class ReCaptchaError {

    private String errorCode;
    private String message;
}
