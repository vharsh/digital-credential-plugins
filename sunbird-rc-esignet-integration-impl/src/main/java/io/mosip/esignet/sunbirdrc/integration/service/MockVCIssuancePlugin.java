package io.mosip.esignet.sunbirdrc.integration.service;


import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import io.mosip.esignet.api.exception.VCIExchangeException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import foundation.identity.jsonld.JsonLDObject;
import io.mosip.esignet.api.dto.VCRequestDto;
import io.mosip.esignet.api.dto.VCResult;
import io.mosip.esignet.api.spi.VCIssuancePlugin;
import lombok.extern.slf4j.Slf4j;


@ConditionalOnProperty(value = "mosip.esignet.integration.vci-plugin", havingValue = "SunbirdRCVCIssuancePlugin")
@Component
@Slf4j
public class MockVCIssuancePlugin implements VCIssuancePlugin {

    @Autowired
    Environment env;

    private final Map<String, Template> credentialTypeTemplates = new HashMap<>();
    private VelocityEngine vEngine;

    public  void validateProperties() {

        vEngine = new VelocityEngine();
        vEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        vEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        vEngine.init();

        //Validate all the supported VC
        String supportedCredentialTypes =env.getProperty("mosip.esignet.vciplugin.sunbird-rc.supported-credential-types");
        if (supportedCredentialTypes == null || supportedCredentialTypes.isEmpty()) {
            throw new IllegalArgumentException("Property mosip.esignet.vciplugin.sunbird-rc.supported-credential-types is not set.");
        }
        String[] credentialTypes = supportedCredentialTypes.split(",");
        for (String credentialType : credentialTypes) {
            validatePropertyForCredentialType(credentialType.trim());
        }
    }

    @Override
    public VCResult<JsonLDObject> getVerifiableCredentialWithLinkedDataProof(VCRequestDto vcRequestDto, String holderId, Map<String, Object> identityDetails) throws VCIExchangeException {
        validateProperties();
        return null;
    }

    @Override
    public VCResult<String> getVerifiableCredential(VCRequestDto vcRequestDto, String holderId, Map<String, Object> identityDetails) throws VCIExchangeException {
        return null;
    }



    private void validatePropertyForCredentialType(String credentialType) {
        validateProperty("mosip.esignet.vciplugin.sunbird-rc.credential-type." + credentialType + ".template-url");
        validateProperty("mosip.esignet.vciplugin.sunbird-rc.credential-type." + credentialType + ".registry-get-url");
        validateProperty("mosip.esignet.vciplugin.sunbird-rc.credential-type." + credentialType + ".cred-schema-id");
        validateProperty("mosip.esignet.vciplugin.sunbird-rc.credential-type." + credentialType + ".cred-schema-version");
        validateProperty("mosip.esignet.vciplugin.sunbird-rc.credential-type." + credentialType + ".static-value-map.issuerId");
        //TODO download  the templete
        //TODO validate  the templete as JSON-ld
        //Todo use Map of Map to cache configuration

        String templateUrl = env.getProperty("mosip.esignet.vciplugin.sunbird-rc.credential-type." + credentialType + ".template-url");
        downloadAndValideateTemplate(templateUrl,credentialType);
        //validateJsonLd(template);
        //credentialTypeTemplates.put(credentialType, template);

    }

    private void validateProperty(String propertyName) {
        String propertyValue = env.getProperty(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
           throw new IllegalArgumentException("Property " + propertyName + " is not set.");
        }
    }

    private void downloadAndValideateTemplate(String templateUrl,String credentialType){

        //String templete=wrt.toString();
        try{
            Template t = vEngine.getTemplate(templateUrl);
            VelocityContext context = new VelocityContext();
            StringWriter wrt = new StringWriter();
            t.merge(context,wrt);

            System.out.println(wrt.toString());
            JSONObject jsonObject = new JSONObject(wrt.toString());
            boolean hasContext=jsonObject.has("@context");
            boolean hasType=jsonObject.has("type");
            boolean hasCredential=jsonObject.has("credentialSubject");
            if(!hasCredential || !hasContext || !hasType){
                throw new IllegalArgumentException("Not a valid JSON-ld format");
            }
            credentialTypeTemplates.put(credentialType,t);
        }catch (Exception e){

        }

    }

    private void validateJsonLd(String jsonLd)  {

    }


















































//	@Autowired
//	private SignatureService signatureService;
//
//	private ConfigurableDocumentLoader confDocumentLoader = null;
//
//	@Value("${mosip.esignet.mock.vciplugin.verification-method}")
//	private String verificationMethod;
//
//	public static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
//
//	public static final String OIDC_SERVICE_APP_ID = "OIDC_SERVICE";
//
//	@Override
//	public VCResult<JsonLDObject> getVerifiableCredentialWithLinkedDataProof(VCRequestDto vcRequestDto, String holderId,
//																			 Map<String, Object> identityDetails) throws VCIExchangeException {
//
//		//VelocityEngine vEngine = new VelocityEngine();
//		JsonLDObject vcJsonLdObject = null;
//		try {
//			VCResult vcResult = new VCResult();
//			vcJsonLdObject = buildDummyJsonLDWithLDProof(holderId);
//			vcResult.setCredential(vcJsonLdObject);
//			vcResult.setFormat("ldp_vc");
//			return vcResult;
//		} catch (Exception e) {
//			log.error("Failed to build mock VC", e);
//		}
//		throw new VCIExchangeException();
//	}
//
//	private JsonLDObject buildDummyJsonLDWithLDProof(String holderId)
//			throws IOException, GeneralSecurityException, JsonLDException, URISyntaxException {
//		Map<String, Object> formattedMap = new HashMap<>();
//		formattedMap.put("id", holderId);
//		formattedMap.put("name", "John Doe");
//		formattedMap.put("email", "john.doe@mail.com");
//		formattedMap.put("gender", "Male");
//
//		Map<String, Object> verCredJsonObject = new HashMap<>();
//		verCredJsonObject.put("@context", Arrays.asList("https://www.w3.org/2018/credentials/v1", "https://schema.org/"));
//		verCredJsonObject.put("type", Arrays.asList("VerifiableCredential", "Person"));
//		verCredJsonObject.put("id", "urn:uuid:3978344f-8596-4c3a-a978-8fcaba3903c5");
//		verCredJsonObject.put("issuer", "did:example:123456789");
//		verCredJsonObject.put("issuanceDate", getUTCDateTime());
//		verCredJsonObject.put("credentialSubject", formattedMap);
//
//		JsonLDObject vcJsonLdObject = JsonLDObject.fromJsonObject(verCredJsonObject);
//		vcJsonLdObject.setDocumentLoader(confDocumentLoader);
//		// vc proof
//		Date created = Date
//				.from(LocalDateTime
//						.parse((String) verCredJsonObject.get("issuanceDate"),
//								DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN))
//						.atZone(ZoneId.systemDefault()).toInstant());
//		LdProof vcLdProof = LdProof.builder().defaultContexts(false).defaultTypes(false).type("RsaSignature2018")
//				.created(created).proofPurpose("assertionMethod")
//				.verificationMethod(URI.create(verificationMethod))
//				.build();
//
//		URDNA2015Canonicalizer canonicalizer = new URDNA2015Canonicalizer();
//		byte[] vcSignBytes = canonicalizer.canonicalize(vcLdProof, vcJsonLdObject);
//		String vcEncodedData = CryptoUtil.encodeToURLSafeBase64(vcSignBytes);
//
//		JWTSignatureRequestDto jwtSignatureRequestDto = new JWTSignatureRequestDto();
//		jwtSignatureRequestDto.setApplicationId(OIDC_SERVICE_APP_ID);
//		jwtSignatureRequestDto.setReferenceId("");
//		jwtSignatureRequestDto.setIncludePayload(false);
//		jwtSignatureRequestDto.setIncludeCertificate(true);
//		jwtSignatureRequestDto.setIncludeCertHash(true);
//		jwtSignatureRequestDto.setDataToSign(vcEncodedData);
//		JWTSignatureResponseDto responseDto = signatureService.jwtSign(jwtSignatureRequestDto);
//		LdProof ldProofWithJWS = LdProof.builder().base(vcLdProof).defaultContexts(false)
//				.jws(responseDto.getJwtSignedData()).build();
//		ldProofWithJWS.addToJsonLDObject(vcJsonLdObject);
//		return vcJsonLdObject;
//	}
//
//	private static String getUTCDateTime() {
//		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
//	}
//
//	@Override
//	public VCResult<String> getVerifiableCredential(VCRequestDto vcRequestDto, String holderId,
//													Map<String, Object> identityDetails) throws VCIExchangeException {
//		throw new VCIExchangeException(ErrorConstants.NOT_IMPLEMENTED);
//	}

}
