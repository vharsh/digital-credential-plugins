package io.mosip.certify.vcdatamodel.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.jsonld.JsonLDException;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.LdProof;
import info.weboftrust.ldsignatures.canonicalizer.URDNA2015Canonicalizer;
import io.mosip.certify.vcdatamodel.templating.VCDataModelFormatter;
import io.mosip.certify.vcdatamodel.templating.impl.VCDataModel2;
import okhttp3.*;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TempApp {
    static OkHttpClient client = new OkHttpClient();
    static ObjectMapper objectMapper = new ObjectMapper();
    public static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static void main(String[] args) {
        Map<String, Object> data = new HashMap<>();
        data.put("validFrom", getUTCDateTime(0));
        // data.put("validUntil", getUTCDateTime(1));
        data.put("context", "https://vharsh.github.io/DID/SchoolCredential.json");
        data.put("issuer", "https://vharsh.github.io/DID/mock-controller2.json");
        data.put("dob", "01/01/2000");
        data.put("city", "Bangalore");
        // new JSONArray((List<String>) value)
        // data.put("amenities", new JSONArray(Arrays.asList("classroom", "library", "swimming pool")));
        data.put("name", "MS Dhoni Intl");
        data.put("principalName", "XYZ");
        data.put("schoolType", "normal");
        data.put("country", "India");
        data.put("policyName", "12345");
        data.put("policyNumber", 123456);
        VCDataModelFormatter vc2 = new VCDataModel2();
        // template the data & get a VC 2.0
        vc2.format(data);
        System.out.println(vc2.format(data));
        String templatedData = vc2.format(data);
        Map<String, Object> o;
        try {
            o = objectMapper.readValue(templatedData.trim(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonLDObject vcJsonLdObject = JsonLDObject.fromJsonObject(o);
        vcJsonLdObject.setDocumentLoader(null);
        // canonicalize
        Date validFrom = Date
                .from(LocalDateTime
                        .parse((String) data.get("validFrom"),
                                DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN))
                        .atZone(ZoneId.systemDefault()).toInstant());
        LdProof vcLdProof = LdProof.builder().defaultContexts(false).defaultTypes(false).type("Ed25519Signature2018")
                .created(validFrom).proofPurpose("assertionMethod")
                .verificationMethod(URI.create("https://vharsh.github.io/DID/mock-public-key2.json"))
                // ^^ Why is this pointing to JWKS URL of eSignet??
                .build();
        // 1. Canonicalize
        URDNA2015Canonicalizer canonicalizer = new URDNA2015Canonicalizer();
        // VC Sign
        byte[] vcSignBytes = null;
        try {
            vcSignBytes = canonicalizer.canonicalize(vcLdProof, vcJsonLdObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (JsonLDException e) {
            throw new RuntimeException(e);
        }
        String vcEncodedData = Base64.getUrlEncoder().encodeToString(vcSignBytes);
        String jwtSignedData = jwtSign(vcEncodedData);
        LdProof ldProofWithJWS = LdProof.builder().base(vcLdProof).defaultContexts(false)
                .jws(jwtSignedData).build();
        ldProofWithJWS.addToJsonLDObject(vcJsonLdObject);
        System.out.println(vcJsonLdObject);
    }

    private static String jwtSign(String vcEncodedData) {
        // Make a call to the /jwsSign API in dev1
        Map<String, Object> req = new HashMap<>();
        req.put("id", "v1");
        req.put("version", "1.0");
        Map<String, Object> re = new HashMap<>();
        re.put("dataToSign", vcEncodedData);
        re.put("applicationId", "CERTIFY_MOCK_ED25519");
        re.put("referenceId", "ED25519_SIGN");
        re.put("includePayload", false);
        re.put("includeCertificate", false);
        re.put("includeCertHash", true);
        re.put("certificateUrl", "");
        re.put("validateJson", false);
        re.put("b64JWSHeaderParam", false);
        re.put("signAlgorithm", "EdDSA");
        req.put("request", re);

        String jsonBody = null;
        try {
            jsonBody = objectMapper.writeValueAsString(req);
            System.out.println(jsonBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String code = "[REDACTED]";
        Request r = new Request.Builder()
                .url("https://api-internal.dev.mosip.net/v1/keymanager/jwsSign")
                .addHeader("Authorization", code)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", "state=335e1ee7-6ec6-4c9a-9a84-5eec2c6be186; Authorization=[REDACTED]")
                .post(RequestBody.create(jsonBody.getBytes())).build();
        Call call = client.newCall(r);
        try {
            Response res = call.execute();
            System.out.println("Got Status code " + res.code());
            String resp = res.body().string();
            System.out.println();
            System.out.println(resp);
            System.out.println();
            Map<String, Object> body = objectMapper.readValue(resp, Map.class);
            System.out.println(body.entrySet());
            Map<String, Object> x = (Map<String, Object>) body.get("response");
            return (String) x.get("jwtSignedData");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String getUTCDateTime(int years) {
        ZonedDateTime z = ZonedDateTime.now(ZoneOffset.UTC);
        z.plusYears(years);
        return z.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
    }
}
