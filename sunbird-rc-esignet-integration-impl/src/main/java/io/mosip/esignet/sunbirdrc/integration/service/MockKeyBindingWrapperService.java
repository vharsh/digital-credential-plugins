package io.mosip.esignet.sunbirdrc.integration.service;

import io.mosip.esignet.api.dto.AuthChallenge;
import io.mosip.esignet.api.dto.KeyBindingResult;
import io.mosip.esignet.api.dto.SendOtpResult;
import io.mosip.esignet.api.exception.KeyBindingException;
import io.mosip.esignet.api.exception.SendOtpException;
import io.mosip.esignet.api.spi.KeyBinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@ConditionalOnProperty(value = "mosip.esignet.integration.key-binder", havingValue = "MockKeyBindingWrapperService")
@Component
public class MockKeyBindingWrapperService implements KeyBinder {


    @Override
    public SendOtpResult sendBindingOtp(String individualId, List<String> otpChannels, Map<String, String> requestHeaders) throws SendOtpException {
        return null;
    }

    @Override
    public KeyBindingResult doKeyBinding(String individualId, List<AuthChallenge> challengeList, Map<String, Object> publicKeyJWK, String bindAuthFactorType, Map<String, String> requestHeaders) throws KeyBindingException {
        return null;
    }

    @Override
    public List<String> getSupportedChallengeFormats(String authFactorType) {
        return null;
    }
}
