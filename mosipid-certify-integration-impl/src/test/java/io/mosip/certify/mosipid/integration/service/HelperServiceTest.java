package io.mosip.certify.mosipid.integration.service;


import io.mosip.kernel.signature.dto.JWTSignatureResponseDto;
import io.mosip.kernel.signature.service.SignatureService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HelperServiceTest {

    @InjectMocks
    private HelperService helperService;

    @Mock
    private SignatureService signatureService;

    @Test
    public void getRequestSignature_validation() {
        JWTSignatureResponseDto jwtSignatureResponseDto = new JWTSignatureResponseDto();
        jwtSignatureResponseDto.setJwtSignedData("test-jwt");
        Mockito.when(signatureService.jwtSign(Mockito.any())).thenReturn(jwtSignatureResponseDto);
        Assert.assertEquals("test-jwt", helperService.getRequestSignature("test-request-value"));
    }
}
