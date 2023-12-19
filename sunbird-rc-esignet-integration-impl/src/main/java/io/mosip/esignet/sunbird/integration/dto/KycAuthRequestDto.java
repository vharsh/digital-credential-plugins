package io.mosip.esignet.sunbird.integration.dto;


import lombok.Data;

import java.util.List;

@Data
public class KycAuthRequestDto {

    private String transactionId;
    private String individualId;
    private String otp;
    private String pin;
    private String biometrics;
    private List<String> tokens;
}
