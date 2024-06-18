/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.certify.mosipid.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.certify.api.dto.AuditDTO;
import io.mosip.certify.api.spi.AuditPlugin;
import io.mosip.certify.api.util.Action;
import io.mosip.certify.api.util.ActionStatus;
import io.mosip.certify.mosipid.integration.helper.AuthTransactionHelper;
import io.mosip.certify.mosipid.integration.dto.AuditRequest;
import io.mosip.certify.mosipid.integration.dto.AuditResponse;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@ConditionalOnProperty(value = "mosip.certify.integration.audit-plugin", havingValue = "IdaAuditPluginImpl")
@Component
@Slf4j
public class IdaAuditPluginImpl implements AuditPlugin {

	private static final String CERTIFY = "certify";


	@Autowired
	private AuthTransactionHelper authTransactionHelper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.certify.authenticator.ida.audit-manager-url}")
	private String auditManagerUrl;

	@Value("${mosip.audit.description.max-length:2048}")
	private Integer auditDescriptionMaxLength;

	@Override
	public void logAudit(Action action, ActionStatus status, AuditDTO audit, Throwable t) {
		audit(null, action, status, audit,t);
	}

	@Override
	public void logAudit(String username, Action action, ActionStatus status, AuditDTO audit, Throwable t) {
		audit(username, action, status, audit,t);
	}

	private void audit(String username, Action action, ActionStatus status, AuditDTO audit, Throwable t) {
		try {
			String authToken = authTransactionHelper.getAuthToken();

			RequestWrapper<AuditRequest> request = new RequestWrapper<>();

			AuditRequest auditRequest = new AuditRequest();
			auditRequest.setEventId(action.name());
			auditRequest.setEventName(action.name());
			auditRequest.setEventType(status.name());
			auditRequest.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
			auditRequest.setHostName("localhost");
			auditRequest.setHostIp("localhost");
			auditRequest.setApplicationId(CERTIFY);
			auditRequest.setApplicationName(CERTIFY);
			auditRequest.setSessionUserId(StringUtils.isEmpty(username)?"no-user":username);
			auditRequest.setSessionUserName(StringUtils.isEmpty(username)?"no-user":username);
			auditRequest.setIdType(audit.getIdType());
			auditRequest.setCreatedBy(this.getClass().getSimpleName());
			auditRequest.setModuleName(action.getModule());
			auditRequest.setModuleId(action.getModule());
			String auditDescription = t != null ? ExceptionUtils.getStackTrace(t) : getAuditDescription(audit);
			if (auditDescription != null && auditDescription.length() > auditDescriptionMaxLength) {
				auditDescription = auditDescription.substring(0, auditDescriptionMaxLength);
			}
			auditRequest.setDescription(auditDescription);
			auditRequest.setId(audit.getTransactionId());

			request.setRequest(auditRequest);
			request.setId("ida");
			request.setRequesttime(DateUtils.getUTCCurrentDateTime());

			String requestBody = objectMapper.writeValueAsString(request);
			RequestEntity requestEntity = RequestEntity
					.post(UriComponentsBuilder.fromUriString(auditManagerUrl).build().toUri())
					.contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.COOKIE, "Authorization=" + authToken)
					.body(requestBody);
			ResponseEntity<ResponseWrapper> responseEntity = restTemplate.exchange(requestEntity,
					new ParameterizedTypeReference<ResponseWrapper>() {
					});

			if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
				ResponseWrapper<AuditResponse> responseWrapper = responseEntity.getBody();
				if (responseWrapper != null && responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty())  {
					log.error("Error response received from audit service with errors: {}",
							responseWrapper.getErrors());
				}
			}

			if(responseEntity.getStatusCode() == HttpStatus.FORBIDDEN ||
					responseEntity.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				log.error("Audit call failed with error: {}, issue with auth-token hence purging the auth-token-cache",
						responseEntity.getStatusCode());
				authTransactionHelper.purgeAuthTokenCache();
			}
		} catch (Exception e) {
			log.error("LogAudit failed with error : {}", e);
		}
	}

	private String getAuditDescription(AuditDTO audit) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("transactionId", audit.getTransactionId());
		json.put("idType", audit.getIdType());
		return json.toString();
	}

}
