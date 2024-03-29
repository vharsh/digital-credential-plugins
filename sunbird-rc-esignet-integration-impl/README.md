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

mosip.esignet.vci.key-values={ 'credential_issuer': '${mosip.esignet.vci.identifier}', 	\
  'credential_endpoint': '${mosipbox.public.url}${server.servlet.path}/vci/credential', \
  'credentials_supported': {{\
  'format': 'ldp_vc',\
  'id': 'InsuranceCredential', \
  'scope' : 'sunbird_rc_insurance_vc_ldp',\
  'cryptographic_binding_methods_supported': {'did:jwk'},\
  'cryptographic_suites_supported': {'Ed25519Signature2020'},\
  'proof_types_supported': {'jwt'},\
  'credential_definition': {\
  'type': {'VerifiableCredential'},\
  'credentialSubject': {\
  'fullName': {'display': {{'name': 'Name','locale': 'en'}}}, \
  'mobile': {'display': {{'name': 'Phone Number','locale': 'en'}}},\
  'dob': {'display': {{'name': 'Date of Birth','locale': 'en'}}},\
  'gender': {'display': {{'name': 'Gender','locale': 'en'}}},\
  'benefits': {'display': {{'name': 'Benefits','locale': 'en'}}},\
  'email': {'display': {{'name': 'Email Id','locale': 'en'}}},\
  'policyIssuedOn': {'display': {{'name': 'Policy Issued On','locale': 'en'}}},\
  'policyExpiresOn': {'display': {{'name': 'Policy Expires On','locale': 'en'}}},\
  'policyName': {'display': {{'name': 'Policy Name','locale': 'en'}}},\
  'policyNumber': {'display': {{'name': 'Policy Number','locale': 'en'}}}\
   }},\
  'display': {{'name': 'Sunbird RC Insurance Verifiable Credential', \
                'locale': 'en', \
                'logo': {'url': 'https://sunbird.org/images/sunbird-logo-new.png',\
                'alt_text': 'a square logo of a Sunbird'},\
                'background_color': '#FDFAF9',\
                'text_color': '#7C4616'}},\
  'order' : {'fullName','policyName','policyExpiresOn','policyIssuedOn','policyNumber','mobile','dob','gender','benefits','email'}\
  \ }},\
  'display': {{'name': 'Insurance', 'locale': 'en'}}\
  }

##---------------------------------Sunbird-RC Plugin Configurations------------------------------------------------------------##

# Base url of registry system 
mosip.esinet.sunbird-rc.base-url=http://localhost

# These two are the default fields required to generate the dynamic authentication form
mosip.esignet.authenticator.default.auth-factor.kba.field-details={{'id':'policyNumber', 'type':'text', 'format':''},{'id':'name', 'type':'text', 'format':''},{'id':'dob', 'type':'date', 'format':'dd/mm/yyyy'}}
mosip.esignet.authenticator.default.auth-factor.kba.individual-id-field=policyNumber

# Mandatory fields required for KBA are described here 
mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.field-details={{"id":"policyNumber", "type":"text", "format":""},{"id":"fullName", "type":"text", "format":""},{"id":"dob", "type":"date", "format":"dd/mm/yyyy"}}
# Amoung mandatory fields, which field is Indentity field is described here
mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.individual-id-field=policyNumber

# This is the field which is used to get the entity id from the registry object
mosip.esignet.authenticator.sunbird-rc.kba.entity-id-field=osid

# This is the field is used to enable the registry search based on the PSUT
mosip.esignet.vciplugin.sunbird-rc.enable-psut-based-registry-search=false

# Url for the verifiable credential issuance
mosip.esignet.vciplugin.sunbird-rc.issue-credential-url=${mosip.esinet.sunbird-rc.base-url}/credential/credentials/issue
# Specifies the types of verifiable credentials supported by the system.
# Based on the supported credentiels type the subsequent properties are defined 
mosip.esignet.vciplugin.sunbird-rc.supported-credential-types=InsuranceCredential,ExampleVerifiableCredential

# Properties for the Insurance Credential

# Url for object based registry search
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.registry-search-url=${mosip.esinet.sunbird-rc.base-url}/registry/api/v1/Insurance/search
# Here we define the verifiable credential issuer DID
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.static-value-map.issuerId=did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322
# Here we provide the template url for the credential
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.template-url=${spring_config_url_env}/*/${active_profile_env}/${spring_config_label_env}/insurance-credential.json
# Url to get registry object based on the entity id
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.registry-get-url={mosip.esinet.sunbird-rc.base-url}/registry/api/v1/Insurance/
# Here we define the schema id for the verfiable credential
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.cred-schema-id=did:schema:0d10a2cf-94de-4ffc-b32c-4f1a61ee05ba
# Here we define the schema version for the verfiable credential
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.cred-schema-version=1.0.0

# Properies for the Mosip Verifiable Credential

mosip.esignet.vciplugin.sunbird-rc.credential-type.ExampleVerifiableCredential.registry-search-url=${mosip.esinet.sunbird-rc.base-url}/registry/api/v1/Insurance/search
mosip.esignet.vciplugin.sunbird-rc.credential-type.ExampleVerifiableCredential.static-value-map.issuerId=did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322
mosip.esignet.vciplugin.sunbird-rc.credential-type.ExampleVerifiableCredential.template-url=${spring_config_url_env}/*/${active_profile_env}/${spring_config_label_env}/insurance-credential.json
mosip.esignet.vciplugin.sunbird-rc.credential-type.ExampleVerifiableCredential.registry-get-url={mosip.esinet.sunbird-rc.base-url}/registry/api/v1/Insurance/# Here we define the schema id for the verfiable credential
mosip.esignet.vciplugin.sunbird-rc.credential-type.ExampleVerifiableCredential.cred-schema-id=did:schema:0d10a2cf-94de-4ffc-b32c-4f1a61ee05ba# Here we define the schema version for the verfiable credential
mosip.esignet.vciplugin.sunbird-rc.credential-type.ExampleVerifiableCredential.cred-schema-version=1.0.0
````


Add "bindingtransaction" cache name in "mosip.esignet.cache.names" property.

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
This integration plugin is compatible with [eSignet 1.2.0](https://github.com/mosip/esignet/tree/v1.2.0) and [Sunbird-RC 1.0.0](https://github.com/Sunbird-RC/sunbird-rc-core/tree/v1.0.0)


