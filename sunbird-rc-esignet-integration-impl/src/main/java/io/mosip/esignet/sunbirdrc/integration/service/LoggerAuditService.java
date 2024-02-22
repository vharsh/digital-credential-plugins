package io.mosip.esignet.sunbirdrc.integration.service;

import io.mosip.esignet.api.dto.AuditDTO;
import io.mosip.esignet.api.spi.AuditPlugin;
import io.mosip.esignet.api.util.Action;
import io.mosip.esignet.api.util.ActionStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "mosip.esignet.integration.audit-plugin", havingValue = "LoggerAuditService")
@Component
public class LoggerAuditService implements AuditPlugin {

    @Override
    public void logAudit(Action action, ActionStatus status, AuditDTO audit, Throwable t) {

    }

    @Override
    public void logAudit(String username, Action action, ActionStatus status, AuditDTO audit, Throwable t) {

    }
}
