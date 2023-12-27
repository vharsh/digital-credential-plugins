/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.esignet.sunbirdrc.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.api.dto.*;
import io.mosip.esignet.api.exception.KycAuthException;
import io.mosip.esignet.api.exception.KycExchangeException;
import io.mosip.esignet.api.exception.SendOtpException;
import io.mosip.esignet.api.spi.Authenticator;
import io.mosip.esignet.api.util.ErrorConstants;
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
import java.util.*;


@ConditionalOnProperty(value = "mosip.esignet.integration.authenticator", havingValue = "SunbirdRCAuthenticationService")
@Component
@Slf4j
public class SunbirdRCAuthenticationService implements Authenticator {

    private final String Filter_Operator="eq";

    private final String Search_Field_Id="id";

    @Value("#{${mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.field-details}}")
    private List<Map<String,String>> fieldDetailList;

    @Value("${mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.registry-search-url}")
    private String registrySearchUrl;

    @Value("${mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.individual-id-field}")
    private String idField;

    @Value("${mosip.esignet.authenticator.sunbird-rc.kba.entity-id-field}")
    private String entityIdField;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    Environment env;

    @Autowired
    private ObjectMapper objectMapper;


    @PostConstruct
    public void initialize() throws KycAuthException {
        log.info("Started to setup Sunbird-RC Authenticator");
        validateProperty("mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.individual-id-field");
        validateProperty("mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.registry-search-url");
        validateProperty("mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.field-details");

        if(fieldDetailList==null || fieldDetailList.isEmpty()){
            log.error("Invalid configuration for field-detaisl");
            throw new KycAuthException("sunbird-rc authenticator field is not configured properly");
        }
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
        String encodedChallenge=authChallenge.getChallenge();

        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedChallenge);
        String challenge = new String(decodedBytes, StandardCharsets.UTF_8);

        try {
            registrySearchRequestDto =createRegistrySearchRequestDto(challenge,individualId);
            String requestBody = objectMapper.writeValueAsString(registrySearchRequestDto);
            RequestEntity requestEntity = RequestEntity
                    .post(UriComponentsBuilder.fromUriString(registrySearchUrl).build().toUri())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(requestBody);
            ResponseEntity<List<Map<String,Object>>> responseEntity = restTemplate.exchange(requestEntity,
                    new ParameterizedTypeReference<List<Map<String,Object>>>() {});
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                List<Map<String,Object>> responseList = responseEntity.getBody();
                if(responseList.size()==1){
                    //TODO  This need to be removed since it can contain PII
                    log.debug("getting response {}", responseEntity);
                    kycAuthResult.setKycToken((String)responseList.get(0).get(entityIdField));
                    kycAuthResult.setPartnerSpecificUserToken((String)responseList.get(0).get(entityIdField));
                    return kycAuthResult;
                }else{
                    log.error("Registry search returns more than one match, so authentication is considered as failed. Result size: " + responseList.size());
                    throw new KycAuthException(ErrorConstants.AUTH_FAILED );
                }
            }else {
                log.error("Sunbird service is not running. Status Code: " ,responseEntity.getStatusCode());
                throw new KycAuthException(ErrorConstants.AUTH_FAILED);
            }

        } catch (Exception e) {
            log.error("Failed to do the Authentication: {}",e);
            throw new KycAuthException(ErrorConstants.AUTH_FAILED );
        }
    }

    private RegistrySearchRequestDto createRegistrySearchRequestDto(String challenge, String individualId) throws KycAuthException, JsonProcessingException {
        RegistrySearchRequestDto registrySearchRequestDto =new RegistrySearchRequestDto();
        registrySearchRequestDto.setLimit(2);
        registrySearchRequestDto.setOffset(0);
        Map<String,Map<String,String>> filter=new HashMap<>();

        Map<String, String> challengeMap = objectMapper.readValue(challenge, new TypeReference<Map<String, String>>() {});


        for(Map<String,String> fieldDetailMap: fieldDetailList) {
            Map<String,String> hashMap=new HashMap<>();
            if(!StringUtils.isEmpty(idField) && fieldDetailMap.get(Search_Field_Id).equals(idField)){
                hashMap.put(Filter_Operator,individualId);
            }else{
                if(!challengeMap.containsKey(fieldDetailMap.get(Search_Field_Id)))
                {
                    log.error("Field '{}' is missing in the challenge.", fieldDetailMap.get(""));
                    throw new KycAuthException(ErrorConstants.AUTH_FAILED );
                }
                hashMap.put(Filter_Operator,challengeMap.get(fieldDetailMap.get(Search_Field_Id)));
            }
            filter.put(fieldDetailMap.get(Search_Field_Id),hashMap);
        }
        registrySearchRequestDto.setFilters(filter);
        return registrySearchRequestDto;
    }

    private void validateProperty(String propertyName) throws KycAuthException {
        String propertyValue = env.getProperty(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            log.error("Field not configured: {}",propertyName);
            throw new KycAuthException("sunbird-rc authenticator field is not configured properly");
        }
    }

}
