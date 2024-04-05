# sunbird-rc-esignet-integration-impl

## About
Implementation for Authenticator, VCIssuancePlugin interfaces defined in esignet-integration-api. This library is built as a integration for [sunbird-registry-system](https://github.com/Sunbird-RC) service.

This library should be added as a runtime dependency to [esignet-service](https://github.com/mosip/esignet)

## Below configurations should be updated to configure the Sunbird RC plugins in eSignet

````
mosip.esignet.integration.scan-base-package=io.mosip.esignet.sunbirdrc.integration
mosip.esignet.integration.authenticator=SunbirdRCAuthenticationService
mosip.esignet.integration.vci-plugin=SunbirdRCVCIssuancePlugin
mosip.esignet.integration.key-binder=MockKeyBindingWrapperService
mosip.esignet.integration.audit-plugin=LoggerAuditService
mosip.esignet.integration.captcha-validator=GoogleRecaptchaValidatorService

# Update the below sub properties inside mosip.esignet.ui.config.key-values property

'auth.factor.kba.individual-id-field' : '${mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.individual-id-field}',\
'auth.factor.kba.field-details':${mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.field-details}

````

## Below configuration need to be updated based on specific usecase (Sample values are based on Insurance usecase)

````
mosip.esignet.vci.key-values={\
 'v11' : {\
              'credential_issuer': '${mosip.esignet.vci.identifier}',   \
              'credential_endpoint': '${mosip.esignet.domain.url}${server.servlet.path}/vci/credential', \
              'display': {{'name': 'Insurance', 'locale': 'en'}},\
              'credentials_supported': {{\
                      'format': 'ldp_vc',\
                      'id': 'InsuranceCredential', \
                      'scope' : 'sunbird_rc_insurance_vc_ldp',\
                      'cryptographic_binding_methods_supported': {'did:jwk'},\
                      'cryptographic_suites_supported': {'Ed25519Signature2020'},\
                      'proof_types_supported': {'jwt'},\
                      'credential_definition': {\
                        'type': {'VerifiableCredential','InsuranceCredential'},\
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
                                    'logo': {'url': 'https://sunbird.org/images/sunbird-logo-new.png', 'alt_text': 'a square logo of a Sunbird'},\
                                    'background_color': '#FDFAF9',\
                                    'text_color': '#7C4616'}},\
                        'order' : {'fullName','policyName','policyExpiresOn','policyIssuedOn','policyNumber','mobile','dob','gender','benefits','email'}\
                  },\
                  {\
                      'format': 'ldp_vc',\
                      'id': 'LifeInsuranceCredential', \
                      'scope' : 'life_insurance_vc_ldp',\
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
                      'display': {{'name': 'Life Insurance Verifiable Credential', \
                                    'locale': 'en', \
                                    'logo': {'url': 'https://sunbird.org/images/sunbird-logo-new.png','alt_text': 'a square logo of a Sunbird'},\
                                    'background_color': '#FDFAF9',\
                                    'text_color': '#7C4616'}},\
                       'order' : {'fullName','policyName','policyExpiresOn','policyIssuedOn','policyNumber','mobile','dob','gender','benefits','email'}\
                  }}\
          },\
   'latest' : {\
              'credential_issuer': '${mosip.esignet.vci.identifier}',   \
              'credential_endpoint': '${mosip.esignet.domain.url}${server.servlet.path}/vci/credential', \
              'display': {{'name': 'Insurance', 'locale': 'en'}},\
              'credentials_supported' : { \
                 "InsuranceCredential" : {\
                    'format': 'ldp_vc',\
                    'scope' : 'sunbird_rc_insurance_vc_ldp',\
                    'cryptographic_binding_methods_supported': {'did:jwk'},\
                    'cryptographic_suites_supported': {'Ed25519Signature2020'},\
                    'proof_types_supported': {'jwt'},\
                    'credential_definition': {\
                      'type': {'VerifiableCredential','InsuranceCredential'},\
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
                                  'logo': {'url': 'https://sunbird.org/images/sunbird-logo-new.png','alt_text': 'a square logo of a Sunbird'},\
                                  'background_color': '#FDFAF9',\
                                  'text_color': '#7C4616'}},\
                    'order' : {'fullName','policyName','policyExpiresOn','policyIssuedOn','policyNumber','mobile','dob','gender','benefits','email'}\
                 },\
                  "LifeInsuranceCredential":{\
                      'format': 'ldp_vc',\
                      'scope' : 'life_insurance_vc_ldp',\
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
                      'display': {{'name': 'Life Insurance Verifiable Credential', \
                                    'locale': 'en', \
                                    'logo': {'url': 'https://sunbird.org/images/sunbird-logo-new.png','alt_text': 'a square logo of a Sunbird'},\
                                    'background_color': '#FDFAF9',\
                                    'text_color': '#7C4616'}},\
                       'order' : {'fullName','policyName','policyExpiresOn','policyIssuedOn','policyNumber','mobile','dob','gender','benefits','email'}\
              }}\
   }\
}


##---------------------------------Sunbird-RC Plugin Configurations------------------------------------------------------------##

# Base url of registry system 
mosip.esignet.sunbird-rc.base-url=http://localhost

# Mandatory fields details that are required for KBA based authentication
mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.field-details={{"id":"policyNumber", "type":"text", "format":""},{"id":"fullName", "type":"text", "format":""},{"id":"dob", "type":"date", "format":"dd/mm/yyyy"}}
# Field that should be considered as individual id field from above fields
mosip.esignet.authenticator.sunbird-rc.auth-factor.kba.individual-id-field=policyNumber

# Field that is considered as entity id in the registry object
mosip.esignet.authenticator.sunbird-rc.kba.entity-id-field=osid

# Enable / disable the PSUT based registry search. To be enabled if the access token is expected to have PSUT instead of osid in the sub claim
mosip.esignet.vciplugin.sunbird-rc.enable-psut-based-registry-search=false

# Sunbird Url for the verifiable credential generation
mosip.esignet.vciplugin.sunbird-rc.issue-credential-url=${mosip.esignet.sunbird-rc.base-url}/credential/credentials/issue
# Specifies the types of verifiable credentials supported by the system.
# Based on the supported credentiels type the subsequent properties are defined 
mosip.esignet.vciplugin.sunbird-rc.supported-credential-types=InsuranceCredential,LifeInsuranceCredential

# Properties for the Insurance Credential

# Url for object based registry search
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.registry-search-url=${mosip.esignet.sunbird-rc.base-url}/registry/api/v1/Insurance/search
# VC issuer DID, same will be replaced in subject id field before credential generation
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.static-value-map.issuerId=did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322
# template url which will point to the Velocity template configured for this credential type
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.template-url=${spring_config_url_env}/*/${active_profile_env}/${spring_config_label_env}/insurance-credential.json
# Sunbird Url to get registry object based on the entity id
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.registry-get-url=${mosip.esignet.sunbird-rc.base-url}/registry/api/v1/Insurance/
# schema id for this VC type that is configured in Sunbird C
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.cred-schema-id=did:schema:0d10a2cf-94de-4ffc-b32c-4f1a61ee05ba
# schema version for this VC type that is configured in Sunbird C
mosip.esignet.vciplugin.sunbird-rc.credential-type.InsuranceCredential.cred-schema-version=1.0.0

# Properies for the Life Insurance Credential

mosip.esignet.vciplugin.sunbird-rc.credential-type.LifeInsuranceCredential.static-value-map.issuerId=did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322
mosip.esignet.vciplugin.sunbird-rc.credential-type.LifeInsuranceCredential.template-url=${spring_config_url_env}/*/${active_profile_env}/${spring_config_label_env}/life-insurance-credential.json
mosip.esignet.vciplugin.sunbird-rc.credential-type.LifeInsuranceCredential.registry-get-url=http://10.3.148.107/registry/api/v1/Insurance/
mosip.esignet.vciplugin.sunbird-rc.credential-type.LifeInsuranceCredential.cred-schema-id=did:schema:0d10a2cf-94de-4ffc-b32c-4f1a61ee05bamos
mosip.esignet.vciplugin.sunbird-rc.credential-type.LifeInsuranceCredential.cred-schema-version=1.0.0
mosip.esignet.vciplugin.sunbird-rc.credential-type.LifeInsuranceCredential.registry-search-url=http://10.3.148.107/registry/api/v1/Insurance/search
````


## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
This integration plugin is compatible with [eSignet 1.4.0](https://github.com/mosip/esignet/tree/v1.4.0) and [Sunbird-RC 1.0.0](https://github.com/Sunbird-RC/sunbird-rc-core/tree/v1.0.0)


