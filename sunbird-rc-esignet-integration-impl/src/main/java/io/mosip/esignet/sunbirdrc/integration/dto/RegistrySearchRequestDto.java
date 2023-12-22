package io.mosip.esignet.sunbirdrc.integration.dto;

import lombok.Data;

import java.util.Map;

@Data
public class RegistrySearchRequestDto {

    private int offset;
    private int limit;
    private Map<String, Map<String, String>> filters;
}
