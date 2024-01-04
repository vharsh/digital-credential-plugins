/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.esignet.sunbirdrc.integration.service;


import java.io.StringWriter;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.api.exception.VCIExchangeException;
import io.mosip.esignet.api.util.ErrorConstants;
import io.mosip.kernel.signature.service.SignatureService;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import foundation.identity.jsonld.JsonLDObject;
import io.mosip.esignet.api.dto.VCRequestDto;
import io.mosip.esignet.api.dto.VCResult;
import io.mosip.esignet.api.spi.VCIssuancePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;


@ConditionalOnProperty(value = "mosip.esignet.integration.vci-plugin", havingValue = "SunbirdRCVCIssuancePlugin")
@Component
@Slf4j
public class SunbirdRCVCIssuancePlugin implements VCIssuancePlugin {

    public static final String PROPERTY_CONSTANT="mosip.esignet.vciplugin.sunbird-rc.credential-type";

    public static final String FORMAT="ldp_vc";

    public static final String TEMPLATE_URL = "template-url";

    public static final String REGISTRY_GET_URL = "registry-get-url";

    public static final String CRED_SCHEMA_ID = "cred-schema-id";

    public static final String CRED_SCHEMA_VESRION = "cred-schema-version";

    public static final String STATIC_VALUE_MAP_ISSUER_ID = "static-value-map.issuerId";

    @Autowired
    Environment env;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mosip.esignet.vciplugin.sunbird-rc.issue-credential-url}")
    String issueCredentialUri;

    @Value("${mosip.esignet.vciplugin.sunbird-rc.supported-credential-types}")
    String supportedCredentialTypes;

    @Value("${mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.registry-get-url}")
    String registryUrl;



    private final Map<String, Template> credentialTypeTemplates = new HashMap<>();

    private final Map<String,Map<String,String>> credentialTypeConfigMap = new HashMap<>();

    private VelocityEngine vEngine;


    @PostConstruct
    public  void validateProperties() throws VCIExchangeException {

        vEngine = new VelocityEngine();
        vEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        vEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        vEngine.init();
        //Validate all the supported VC
        String[] credentialTypes = supportedCredentialTypes.split(",");
        for (String credentialType : credentialTypes) {
            validatePropertyForCredentialType(credentialType.trim());
        }
    }

    @Override
    public VCResult<JsonLDObject> getVerifiableCredentialWithLinkedDataProof(VCRequestDto vcRequestDto, String holderId, Map<String, Object> identityDetails) throws VCIExchangeException {
        if (vcRequestDto == null || vcRequestDto.getType() == null) {
            throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
        }
        List<String> types = vcRequestDto.getType();
        if (types.isEmpty() || !types.get(0).equals("VerifiableCredential")) {
            log.error("Invalid request: first item in type is not VerifiableCredential");
            throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
        }
        types.remove(0);
        String requestedCredentialType = String.join("-", types);
        //Check if the key is in the supported-credential-types
        List<String> supportedTypes = Arrays.asList(supportedCredentialTypes.split(","));
        if (!supportedTypes.contains(requestedCredentialType)) {
            log.error("Credential type is not supported");
            throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
        }
        //todo validate context of vcrequestdto with template
        List<String> contextList=vcRequestDto.getContext();
        for(String supportedType:supportedTypes){
            Template template=credentialTypeTemplates.get(supportedType);
            validateContextUrl(template,contextList);
        }
        String osid=(String)identityDetails.get("sub");
        RequestEntity requestEntity = RequestEntity
                .get(UriComponentsBuilder.fromUriString(registryUrl+osid).build().toUri()).build();
        ResponseEntity<Map<String,Object>> responseEntity = restTemplate.exchange(requestEntity,
                new ParameterizedTypeReference<Map<String,Object>>() {});
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            Map<String,Object> responseMap = responseEntity.getBody();
                Map<String,Object> credentialRequestMap = createCredentialIssueRequest(requestedCredentialType,responseMap,vcRequestDto,holderId);
                try{
                    String requestBody=mapper.writeValueAsString(credentialRequestMap);
                    RequestEntity requestEntity2 = RequestEntity
                            .post(UriComponentsBuilder.fromUriString(issueCredentialUri).build().toUri())
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .body(requestBody);
                    ResponseEntity<Map<String,Object>> responseEntity2 = restTemplate.exchange(requestEntity2,
                            new ParameterizedTypeReference<Map<String,Object>>(){});
                    if (responseEntity2.getStatusCode().is2xxSuccessful() && responseEntity2.getBody() != null){
                        //TODO  This need to be removed since it can contain PII
                        log.debug("getting response {}", responseEntity);
                        Map<String,Object> vcResponseMap =responseEntity2.getBody();
                        //casting it to JsonLD object
                        VCResult vcResult = new VCResult();
                        JsonLDObject vcJsonLdObject = JsonLDObject.fromJsonObject(vcResponseMap);
                        vcResult.setCredential(vcJsonLdObject);
                        vcResult.setFormat(FORMAT);
                        return vcResult;
                    }else{
                        log.error("Sunbird service is not running. Status Code: " ,responseEntity.getStatusCode());
                        throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
                    }
                }catch (Exception e){
                    log.error("Unable to parse the Registry Object :{}",credentialRequestMap);
                    throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
                }
        }else {
            log.error("Sunbird service is not running. Status Code: " ,responseEntity.getStatusCode());
            throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
        }
    }

    @Override
    public VCResult<String> getVerifiableCredential(VCRequestDto vcRequestDto, String holderId, Map<String, Object> identityDetails) throws VCIExchangeException {
        return null;
    }

    private Map<String,Object> createCredentialIssueRequest(String requestedCredentialType,Map<String,Object> responseMap,VCRequestDto vcRequestDto,String holderId) {

        Template template=credentialTypeTemplates.get(requestedCredentialType);
        Map<String,String> configMap=credentialTypeConfigMap.get(requestedCredentialType);
        StringWriter wrt = new StringWriter();
        VelocityContext context = new VelocityContext();
        Map<String,Object> requestMap=new HashMap<>();

        try{
            context.put("issuerId", configMap.get(STATIC_VALUE_MAP_ISSUER_ID));
            context.put("calNowPlus30Days", calculateNowPlus30Days());
            context.put("id",holderId);
            for (Map.Entry<String, Object> entry : responseMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof List) {
                    JSONArray jsonArray = new JSONArray((List<String>) value);
                    context.put(key, jsonArray);
                } else {
                    context.put(key, value);
                }
            }
        }catch (Exception e){}
        template.merge(context, wrt);
        try{
            Map<String,Object> tempMap=mapper.readValue(wrt.toString(),Map.class);
            requestMap.put("credential",tempMap);
            requestMap.put("credentialSchemaId",configMap.get(CRED_SCHEMA_ID));
            requestMap.put("credentialSchemaVersion",configMap.get(CRED_SCHEMA_VESRION));
            requestMap.put("tags",new ArrayList<>());
        }catch (Exception e){
        }
        //TODO  This need to be removed since it can contain PII
        log.info("VC requset is {}",requestMap);
        return requestMap;
    }

    private void validatePropertyForCredentialType(String credentialType) throws VCIExchangeException {
        Map<String,String> configMap=new HashMap<>();
        validateProperty(PROPERTY_CONSTANT + "." + credentialType + "." + TEMPLATE_URL,TEMPLATE_URL,configMap);
        validateProperty(PROPERTY_CONSTANT + "." + credentialType + "." + REGISTRY_GET_URL,REGISTRY_GET_URL,configMap);
        validateProperty(PROPERTY_CONSTANT + "." + credentialType + "." + CRED_SCHEMA_ID,CRED_SCHEMA_ID,configMap);
        validateProperty(PROPERTY_CONSTANT + "." + credentialType + "." + CRED_SCHEMA_VESRION,CRED_SCHEMA_VESRION,configMap);
        validateProperty(PROPERTY_CONSTANT + "." + credentialType + "." + STATIC_VALUE_MAP_ISSUER_ID,STATIC_VALUE_MAP_ISSUER_ID,configMap);

        String templateUrl = env.getProperty(PROPERTY_CONSTANT +"." + credentialType + "." + TEMPLATE_URL);
        validateAndCacheTemplate(templateUrl,credentialType);
        // cache configuration with their credential type
        credentialTypeConfigMap.put(credentialType,configMap);
    }

    private void validateProperty(String propertyName,String credentialProp,Map<String,String> configMap) throws VCIExchangeException {
        String propertyValue = env.getProperty(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            throw new VCIExchangeException("Property " + propertyName + " is not set Properly.");
        }
        configMap.put(credentialProp,propertyValue);
    }

    private void validateAndCacheTemplate(String templateUrl, String credentialType){
            Template t = vEngine.getTemplate(templateUrl);
            //Todo Validate if all the templates are valid JSON-LD documents
            credentialTypeTemplates.put(credentialType,t);
    }
    private void validateContextUrl(Template template,List<String> vcRequestContextList) throws VCIExchangeException {
        try{
            StringWriter wrt = new StringWriter();
            template.merge(new VelocityContext(),wrt);
            Map<String,Object> tempMap= mapper.readValue(wrt.toString(),Map.class);
            List<String> contextList=(List<String>)tempMap.get("@context");
            for(String contextUrl:contextList){
                if(!vcRequestContextList.contains(contextUrl)){
                    log.error("ContextUrl is not supported");
                    throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
                }
            }
        }catch ( JsonProcessingException e){
            log.error("Error while parsing the templete ",e);
            throw new VCIExchangeException(ErrorConstants.VCI_EXCHANGE_FAILED);
        }
    }

    private static Date calculateNowPlus30Days() {
        // Implement your logic to calculate current date + 30 days
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        return calendar.getTime();
    }
}
