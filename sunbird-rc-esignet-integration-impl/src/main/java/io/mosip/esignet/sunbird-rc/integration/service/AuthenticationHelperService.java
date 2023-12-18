package io.mosip.esignet.mock.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.api.dto.*;
import io.mosip.esignet.api.exception.KycAuthException;
import io.mosip.esignet.api.exception.SendOtpException;
import io.mosip.esignet.api.util.ErrorConstants;
import io.mosip.esignet.mock.integration.dto.InsurenceResponceDto;
import io.mosip.esignet.mock.integration.dto.KycAuthRequestDto;
import io.mosip.esignet.mock.integration.dto.SearchRequestDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.signature.dto.JWTSignatureRequestDto;
import io.mosip.kernel.signature.dto.JWTSignatureResponseDto;
import io.mosip.kernel.signature.service.SignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@Slf4j
public class AuthenticationHelperService {

    private final String Full_Name="fullName";
    private final String DOB="dob";
    private final String Policy_Number="policyNumber";
    public static final String OIDC_PARTNER_APP_ID = "OIDC_PARTNER";
    private static final Base64.Encoder urlSafeEncoder = Base64.getUrlEncoder().withoutPadding();
    public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    @Value("${mosip.esignet.mock.authenticator.send-otp}")
    private String sendOtpUrl;
    @Value("${mosip.esignet.mock.authenticator.kyc-auth-url}")
    private String kycAuthUrl;
    @Value("${mosip.esignet.mock.authenticator.ida.otp-channels}")
    private List<String> otpChannels;

    @Value("${mosip.esignet.authenticator.default.auth-factor.kba.field.key:eq}")
    private String fieldKey;

    @Autowired
    private SignatureService signatureService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static final Map<String, List<String>> supportedKycAuthFormats = new HashMap<>();

    static {
        supportedKycAuthFormats.put("OTP", List.of("alpha-numeric"));
        supportedKycAuthFormats.put("PIN", List.of("number"));
        supportedKycAuthFormats.put("BIO", List.of("encoded-json"));
        supportedKycAuthFormats.put("WLA", List.of("jwt"));
    }


    public static String b64Encode(String value) {
        return urlSafeEncoder.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static long getEpochSeconds() {
        return ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond();
    }

    protected static LocalDateTime getUTCDateTime() {
        return ZonedDateTime
                .now(ZoneOffset.UTC).toLocalDateTime();
    }

    protected String getRequestSignature(String request) {
        JWTSignatureRequestDto jwtSignatureRequestDto = new JWTSignatureRequestDto();
        jwtSignatureRequestDto.setApplicationId(OIDC_PARTNER_APP_ID);
        jwtSignatureRequestDto.setReferenceId("");
        jwtSignatureRequestDto.setIncludePayload(false);
        jwtSignatureRequestDto.setIncludeCertificate(true);
        jwtSignatureRequestDto.setDataToSign(AuthenticationHelperService.b64Encode(request));
        JWTSignatureResponseDto responseDto = signatureService.jwtSign(jwtSignatureRequestDto);
        log.debug("Request signature ---> {}", responseDto.getJwtSignedData());
        return responseDto.getJwtSignedData();
    }

    public boolean isSupportedOtpChannel(String channel) {
        return channel != null && otpChannels.contains(channel.toLowerCase());
    }

    public SendOtpResult sendOtpMock(String transactionId, String individualId, List<String> otpChannels, String relyingPartyId, String clientId)
            throws SendOtpException {
        try {
            var sendOtpDto = new SendOtpDto();
            sendOtpDto.setTransactionId(transactionId);
            sendOtpDto.setIndividualId(individualId);
            sendOtpDto.setOtpChannels(otpChannels);
            String requestBody = objectMapper.writeValueAsString(sendOtpDto);
            RequestEntity requestEntity = RequestEntity
                    .post(UriComponentsBuilder.fromUriString(sendOtpUrl).pathSegment(relyingPartyId,
                            clientId).build().toUri())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(requestBody);
            ResponseEntity<ResponseWrapper<SendOtpResult>> responseEntity = restTemplate.exchange(requestEntity,
                    new ParameterizedTypeReference<>() {
                    });

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                ResponseWrapper<SendOtpResult> responseWrapper = responseEntity.getBody();
                if (responseWrapper.getResponse() != null) {
                    return responseWrapper.getResponse();
                }
                log.error("Errors in response received from IDA send Otp: {}", responseWrapper.getErrors());
                if (!CollectionUtils.isEmpty(responseWrapper.getErrors())) {
                    throw new SendOtpException(responseWrapper.getErrors().get(0).getErrorCode());
                }
            }
            throw new SendOtpException(ErrorConstants.SEND_OTP_FAILED);
        } catch (SendOtpException e) {
            throw e;
        } catch (Exception e) {
            log.error("send otp failed", e);
            throw new SendOtpException("send_otp_failed: " + e.getMessage());
        }
    }

    public KycAuthResult doKycAuthImpl(String relyingPartyId, String clientId, KycAuthDto kycAuthDto)
            throws KycAuthException {
        try {
            KycAuthRequestDto kycAuthRequestDto = new KycAuthRequestDto();
            kycAuthRequestDto.setTransactionId(kycAuthDto.getTransactionId());
            kycAuthRequestDto.setIndividualId(kycAuthDto.getIndividualId());

            for (AuthChallenge authChallenge : kycAuthDto.getChallengeList()) {
                if(Objects.equals(authChallenge.getAuthFactorType(),"KBA")){
                    return knowledgeBasedAuthentcation(kycAuthDto.getIndividualId(),authChallenge);
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

    private KycAuthResult knowledgeBasedAuthentcation(String policyNumber, AuthChallenge authChallenge) throws KycAuthException {

        KycAuthResult  kycAuthResult= new KycAuthResult();
        SearchRequestDto searchRequestDto=new SearchRequestDto();
        searchRequestDto.setLimit(2);
        searchRequestDto.setOffset(0);
        Map<String,Map<String,String>> filter=new HashMap<>();
        Map<String,String> fullNameMap=new HashMap<>();
        Map<String,String> dobMap=new HashMap<>();
        Map<String,String> policyMap=new HashMap<>();
        String encodedChallenge=authChallenge.getChallenge();

        byte[] base64Bytes = encodedChallenge.getBytes(StandardCharsets.UTF_8);
        byte[] decodedBytes = Base64.getDecoder().decode(base64Bytes);
        String challenge = new String(decodedBytes, StandardCharsets.UTF_8);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(challenge);

            String fullName = jsonNode.get("name").asText();
            String dob = jsonNode.get("dob").asText();


            fullNameMap.put(fieldKey,fullName);
            dobMap.put(fieldKey,dob);
            policyMap.put(fieldKey,policyNumber);

            filter.put(Full_Name,fullNameMap);
            filter.put(DOB,dobMap);
            filter.put(Policy_Number,policyMap);

            searchRequestDto.setFilters(filter);

            String requestBody = objectMapper.writeValueAsString(searchRequestDto);
            RequestEntity requestEntity = RequestEntity
                    .post(UriComponentsBuilder.fromUriString(kycAuthUrl).build().toUri())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(requestBody);
            ResponseEntity<List<InsurenceResponceDto>> responseEntity = restTemplate.exchange(requestEntity,
                    new ParameterizedTypeReference<List<InsurenceResponceDto>>() {});
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                List<InsurenceResponceDto> responseList = responseEntity.getBody();
                if(responseList.size()==1){
                    log.info("getting response {}",responseList);
                    kycAuthResult.setKycToken(responseList.get(0).getOsid());
                    kycAuthResult.setPartnerSpecificUserToken(responseList.get(0).getPolicyNumber());
                    return kycAuthResult;
                }
                log.error("Error response received from IDA, Errors: {}");
                throw new KycAuthException(ErrorConstants.AUTH_FAILED );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new KycAuthException(ErrorConstants.AUTH_FAILED);
    }

//    private boolean isKycAuthFormatSupported(String authFactorType, String kycAuthFormat) {
//        var supportedFormat = supportedKycAuthFormats.get(authFactorType);
//        return supportedFormat != null && supportedFormat.contains(kycAuthFormat);
//    }
}
