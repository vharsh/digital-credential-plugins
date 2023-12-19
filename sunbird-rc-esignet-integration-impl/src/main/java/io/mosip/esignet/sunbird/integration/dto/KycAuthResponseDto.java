package io.mosip.esignet.sunbird.integration.dto;


import lombok.Data;

@Data
public class KycAuthResponseDto {

    private boolean authStatus;
    private String kycToken;
    private String partnerSpecificUserToken;
}