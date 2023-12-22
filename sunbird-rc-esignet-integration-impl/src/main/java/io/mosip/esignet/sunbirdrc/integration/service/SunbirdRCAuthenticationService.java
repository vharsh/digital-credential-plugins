/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.esignet.sunbirdrc.integration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.api.dto.*;
import io.mosip.esignet.api.exception.KycAuthException;
import io.mosip.esignet.api.exception.KycExchangeException;
import io.mosip.esignet.api.exception.SendOtpException;
import io.mosip.esignet.api.spi.Authenticator;
import io.mosip.esignet.api.util.ErrorConstants;
import io.mosip.esignet.sunbirdrc.integration.dto.FieldDetail;
import io.mosip.esignet.sunbirdrc.integration.dto.InsuranceResponseDto;
import io.mosip.esignet.sunbirdrc.integration.dto.RegistrySearchRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;


@ConditionalOnProperty(value = "mosip.esignet.integration.authenticator", havingValue = "SunbirdRCAuthenticationService")
@Component
@Slf4j
public class SunbirdRCAuthenticationService implements Authenticator {


    @Value("${mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.registry-search-url}")
    private String registrySearchUrl;

    @Value("${mosip.esignet.authenticator.default.auth-factor.kba.field.key:eq}")
    private String fieldKey;

    @Value("${mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.individual-id-fields}")
    private String idField;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;


    protected static LocalDateTime getUTCDateTime() {
        return ZonedDateTime
                .now(ZoneOffset.UTC).toLocalDateTime();
    }

    @PostConstruct
    public void initialize() {
        log.info("Started to setup Sunbird-RC");
    }

    @Validated
    @Override
    public KycAuthResult doKycAuth(@NotBlank String relyingPartyId, @NotBlank String clientId,
                                   @NotNull @Valid KycAuthDto kycAuthDto) throws KycAuthException {

        log.info("Started to build kyc-auth request with transactionId : {} && clientId : {}",
                kycAuthDto.getTransactionId(), clientId);
        try {
            for (AuthChallenge authChallenge : kycAuthDto.getChallengeList()) {
                if(Objects.equals(authChallenge.getAuthFactorType(),"KBA")){
                    return validateKnowledgeBasedAuth(kycAuthDto.getIndividualId(),authChallenge);
                }
                throw new KycAuthException("invalid_challenge_format");
            }
        } catch (KycAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("KYC-auth failed with transactionId : {} && clientId : {}", kycAuthDto.getTransactionId(),
                    clientId, e);
        }
        throw new KycAuthException(ErrorConstants.AUTH_FAILED);
    }

    @Override
    public KycExchangeResult doKycExchange(String relyingPartyId, String clientId, KycExchangeDto kycExchangeDto)
            throws KycExchangeException {
        throw new KycExchangeException(ErrorConstants.NOT_IMPLEMENTED);
    }

    @Override
    public SendOtpResult sendOtp(String relyingPartyId, String clientId, SendOtpDto sendOtpDto)
            throws SendOtpException {
        throw new SendOtpException(ErrorConstants.NOT_IMPLEMENTED);
        }

    @Override
    public boolean isSupportedOtpChannel(String channel) {
        return false;
    }

    @Override
    public List<KycSigningCertificateData> getAllKycSigningCertificates() {
        return new ArrayList<>();
    }

    private KycAuthResult validateKnowledgeBasedAuth(String individualId, AuthChallenge authChallenge) throws KycAuthException {

        KycAuthResult  kycAuthResult= new KycAuthResult();
        RegistrySearchRequestDto registrySearchRequestDto =new RegistrySearchRequestDto();
        registrySearchRequestDto.setLimit(2);
        registrySearchRequestDto.setOffset(0);
        Map<String,Map<String,String>> filter=new HashMap<>();
        String encodedChallenge=authChallenge.getChallenge();

        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedChallenge);
        String challenge = new String(decodedBytes, StandardCharsets.UTF_8);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(challenge);

            String fieldDetails =env.getProperty("mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.field-detailss");
            List<FieldDetail> fieldDetailList = objectMapper.readValue(fieldDetails, new TypeReference<List<FieldDetail>>(){});

            for(FieldDetail fieldDetail:fieldDetailList){
                Map<String,String> hashMap=new HashMap<>();

                if(!StringUtils.isEmpty(idField) && fieldDetail.getId().equals(idField)){
                    hashMap.put(fieldKey,individualId);

                }else{
                    hashMap.put(fieldKey,jsonNode.get(fieldDetail.getId()).asText());
                }
                filter.put(fieldDetail.getId(),hashMap);
            }
            registrySearchRequestDto.setFilters(filter);

            String requestBody = objectMapper.writeValueAsString(registrySearchRequestDto);
            RequestEntity requestEntity = RequestEntity
                    .post(UriComponentsBuilder.fromUriString(registrySearchUrl).build().toUri())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(requestBody);
            ResponseEntity<List<InsuranceResponseDto>> responseEntity = restTemplate.exchange(requestEntity,
                    new ParameterizedTypeReference<List<InsuranceResponseDto>>() {});
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                List<InsuranceResponseDto> responseList = responseEntity.getBody();
                if(responseList.size()==1){
                    log.info("getting response {}",responseEntity);
                    kycAuthResult.setKycToken(responseList.get(0).getOsid());
                    kycAuthResult.setPartnerSpecificUserToken(responseList.get(0).getPolicyNumber());
                    return kycAuthResult;
                }
                log.error("Error response received from Sunbird Registry, Errors: {}");
                throw new KycAuthException(ErrorConstants.AUTH_FAILED );
            }

        } catch (Exception e) {
            log.error("Failed to do the Authentication: {}",e);
            throw new KycAuthException(ErrorConstants.AUTH_FAILED );
        }
        throw new KycAuthException(ErrorConstants.AUTH_FAILED);
    }

}
