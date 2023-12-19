package io.mosip.esignet.sunbird.integration.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SearchRequestDto {

    private int offset;
    private int limit;
    private Map<String, Map<String, String>> filters;
}
