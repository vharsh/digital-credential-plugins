package io.mosip.esignet.sunbird.integration.service;

import java.util.Map;

import io.mosip.esignet.api.exception.VCIExchangeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import foundation.identity.jsonld.JsonLDObject;
import io.mosip.esignet.api.dto.VCRequestDto;
import io.mosip.esignet.api.dto.VCResult;
import io.mosip.esignet.api.spi.VCIssuancePlugin;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(value = "mosip.esignet.integration.vci-plugin", havingValue = "MockVCIssuancePlugin")
@Component
@Slf4j
public class MockVCIssuancePlugin implements VCIssuancePlugin {

	@Override
	public VCResult<JsonLDObject> getVerifiableCredentialWithLinkedDataProof(VCRequestDto vcRequestDto, String holderId, Map<String, Object> identityDetails) throws VCIExchangeException {
		return null;
	}

	@Override
	public VCResult<String> getVerifiableCredential(VCRequestDto vcRequestDto, String holderId, Map<String, Object> identityDetails) throws VCIExchangeException {
		return null;
	}
}
