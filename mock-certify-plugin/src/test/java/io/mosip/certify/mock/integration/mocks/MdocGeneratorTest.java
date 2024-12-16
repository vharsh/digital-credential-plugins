package io.mosip.certify.mock.integration.mocks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


public class MdocGeneratorTest {
    @Test
    void shouldGenerateMdocDataSuccessfully() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("family_name", "Mock");
        data.put("given_name", "User");
        data.put("birth_date", "2001-11-06");
        data.put("issuing_country", "MK");
        data.put("document_number", 1233);
        data.put("driving_privileges", new HashMap<>() {{
            put("vehicle_category_code", "A");
        }});
        String generatedMdoc = new MdocGenerator().generate(data,
                "did:jwk:eyJrdHkiOiJFQyIsInVzZSI6InNpZyIsImNydiI6IlAtMjU2Iiwia2lkIjoiZEp2UzIzOXkyQk5Jc3VJdzZ4dG12M0QtLVotbC1yaDlZZ1NQYmhhOGRVVSIsIngiOiI0eDJtTUFHRHRMV3drRUtCMExnUHBYYmZzbVQtZ2FFeVg4c2lkZlFJV2NZIiwieSI6IlBkeU5LVlhyU1piNENZUW1vSzZsWDdadXgwRElCY25oSjktX2E3WmxZdGMiLCJhbGciOiJFUzI1NiJ9",
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg2U+wq/9uXZdMqJgjpaZ0SIkXLlVzZzVkhFKZIxD8adyhRANCAASO1Fw5eAjJH/GoYH4zp3SqSuNBY4EYZ2U+B8h9vbFbR6BkBUTP/nkhcKh9ZqmtEbFfupBOU0BnIHKeJrLaPOFH||LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUJ3RENDQVdXZ0F3SUJBZ0lVZnFVZ0pPd0NHUXlLOGkwNVAyL29sNW01dkNZd0NnWUlLb1pJemowRUF3SXcKVGpFTE1Ba0dBMVVFQmhNQ1RVc3hEakFNQmdOVkJBZ01CVTFMTFV0Qk1SRXdEd1lEVlFRSERBaE5iMk5yUTJsMAplVEVOTUFzR0ExVUVDZ3dFVFc5amF6RU5NQXNHQTFVRUN3d0VUVzlqYXpBZUZ3MHlOREV3TWpJd056QXlOVEJhCkZ3MHlOVEV3TWpJd056QXlOVEJhTUU0eEN6QUpCZ05WQkFZVEFrMUxNUTR3REFZRFZRUUlEQVZOU3kxTFFURVIKTUE4R0ExVUVCd3dJVFc5amEwTnBkSGt4RFRBTEJnTlZCQW9NQkUxdlkyc3hEVEFMQmdOVkJBc01CRTF2WTJzdwpXVEFUQmdjcWhrak9QUUlCQmdncWhrak9QUU1CQndOQ0FBU08xRnc1ZUFqSkgvR29ZSDR6cDNTcVN1TkJZNEVZCloyVStCOGg5dmJGYlI2QmtCVVRQL25raGNLaDlacW10RWJGZnVwQk9VMEJuSUhLZUpyTGFQT0ZIb3lFd0h6QWQKQmdOVkhRNEVGZ1FVMm9BZktsQmh6QmFoNVIrWXkvaEp2TzJpWVc4d0NnWUlLb1pJemowRUF3SURTUUF3UmdJaApBT3lDelAzQUpybnE2U2wvSytyM2Z2VnZrYUdSSmx2ZndidkVXaDlhQVcwbkFpRUFyRUFLV1VnNjF5VjRvUk1NClVacmRxdG9BT01wM2FLeHFGQzFkbkJDWSt2VT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=");

        Assertions.assertNotNull(generatedMdoc);
    }

}

