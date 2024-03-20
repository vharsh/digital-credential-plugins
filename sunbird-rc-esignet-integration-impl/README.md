# sunbird-rc-esignet-integration-impl

## About
Implementation for Authenticator, VCIssuancePlugin interfaces defined in esignet-integration-api. This libaray is built as a wrapper for [sunbird-registry-system](https://github.com/Sunbird-RC) service.

This library should be added as a runtime dependency to [esignet-service](https://github.com/mosip/esignet)

## Configurations required to added in esignet-default.properties

````
mosip.esignet.integration.scan-base-package=io.mosip.esignet.sunbirdrc.integration
mosip.esignet.integration.authenticator=SunbirdRCAuthenticationService
mosip.esignet.integration.vci-plugin=SunbirdRCVCIssuancePlugin
mosip.esignet.integration.key-binder=MockKeyBindingWrapperService
mosip.esignet.integration.audit-plugin=LoggerAuditService
mosip.esignet.integration.captcha-validator=GoogleRecaptchaValidatorService

##---------------------------------Sunbird-RC Plugin Configurations------------------------------------------------------------##
mosip.esinet.sunbird-rc.base-url=http://localhost
mosip.esignet.authenticator.default.auth-factor.kba.field-details={{'id':'policyNumber', 'type':'text', 'format':''},{'id':'name', 'type':'text', 'format':''},{'id':'dob', 'type':'date', 'format':'dd/mm/yyyy'}}
mosip.esignet.authenticator.default.auth-factor.kba.individual-id-field=policyNumber

mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.individual-id-field=policyNumber
mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.field-details={{"id":"policyNumber", "type":"text", "format":""},{"id":"fullName", "type":"text", "format":""},{"id":"dob", "type":"date", "format":"dd/mm/yyyy"}}
mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.registry-search-url=http://10.3.148.107/registry/api/v1/Insurance/search
mosip.esignet.authenticator.sunbird-rc.kba.entity-id-field=osid

mosip.esignet.vciplugin.sunbird-rc.enable-psut-based-registry-search=false
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.registry-search-url=http://10.3.148.107/registry/api/v1/Insurance/search
mosip.esignet.vciplugin.sunbird-rc.issue-credential-url=${mosip.esinet.sunbird-rc.base-url}/credential/credentials/issue
mosip.esignet.vciplugin.sunbird-rc.supported-credential-types=InsuranceCredential
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.static-value-map.issuerId=did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.template-url=${spring_config_url_env}/*/${active_profile_env}/${spring_config_label_env}/insurance-credential.json
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.registry-get-url={mosip.esinet.sunbird-rc.base-url}/registry/api/v1/Insurance/
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.cred-schema-id=did:schema:0d10a2cf-94de-4ffc-b32c-4f1a61ee05ba
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.cred-schema-version=1.0.0
````


Add "bindingtransaction" cache name in "mosip.esignet.cache.names" property.

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
This integration plugin is compatible with [eSignet 1.2.0](https://github.com/mosip/esignet/tree/v1.2.0) and [Sunbird-RC 1.0.0](https://github.com/Sunbird-RC/sunbird-rc-core/tree/v1.0.0)


