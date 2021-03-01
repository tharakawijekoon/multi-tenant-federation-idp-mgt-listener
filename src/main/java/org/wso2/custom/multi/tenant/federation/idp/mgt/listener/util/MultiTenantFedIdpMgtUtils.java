package org.wso2.custom.multi.tenant.federation.idp.mgt.listener.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.custom.multi.tenant.federation.idp.mgt.listener.internal.MultiTenantFedIdpMgtDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.identity.oauth.common.OAuthConstants.GrantTypes.AUTHORIZATION_CODE;
import static org.wso2.carbon.identity.oauth.common.OAuthConstants.OAuthVersions.VERSION_2;
import static org.wso2.custom.multi.tenant.federation.idp.mgt.listener.util.MultiTenantFedIdpMgtConstants.*;

public class MultiTenantFedIdpMgtUtils {

    private static final Log log = LogFactory.getLog(MultiTenantFedIdpMgtUtils.class);

    private MultiTenantFedIdpMgtUtils() {

    }

    /**
     * append suffix to tenant domain to make unique sp name.
     *
     * @param tenantDomain  tenant domain.
     */
    public static String getServiceProviderName(String tenantDomain) {
        return tenantDomain + SP_NAME_SUFFIX;
    }

    /**
     * Create OAuth2 application.
     *
     * @param applicationName application name.
     * @param callbackPath      callback path.
     * @param consumerKey     consumer key.
     * @param consumerSecret  consumer secret.
     * @param appOwner        application owner.
     * @param tenantId        tenant id.
     * @param tenantDomain    tenant domain.
     * @param grantTypes      grant types.
     * @throws IdentityOAuthAdminException in case of failure.
     */
    public static void createOAuth2Application(String applicationName, String callbackPath, String consumerKey,
                                               String consumerSecret, String appOwner, int tenantId, String tenantDomain,
                                               List<String> grantTypes) throws IdentityOAuthAdminException {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        oAuthConsumerAppDTO.setApplicationName(applicationName);
        oAuthConsumerAppDTO.setOAuthVersion(VERSION_2);
        oAuthConsumerAppDTO.setOauthConsumerKey(consumerKey);
        oAuthConsumerAppDTO.setOauthConsumerSecret(consumerSecret);
        String callbackUrl = IdentityUtil.getServerURL(callbackPath, true, true);
        oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
        oAuthConsumerAppDTO.setBypassClientCredentials(false);
        if (grantTypes != null && !grantTypes.isEmpty()) {
            oAuthConsumerAppDTO.setGrantTypes(String.join(" ", grantTypes));
        }
        oAuthConsumerAppDTO.setPkceMandatory(false);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(tenantId);
            privilegedCarbonContext.setTenantDomain(tenantDomain);
            privilegedCarbonContext.setUsername(appOwner);
            MultiTenantFedIdpMgtDataHolder.getInstance().getOAuthAdminService().registerOAuthApplicationData(oAuthConsumerAppDTO);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    /**
     * Create portal application.
     *
     * @param appName        Application name.
     * @param appOwner       Application owner.
     * @param consumerKey    Consumer key.
     * @param consumerSecret Consumer secret.
     * @param tenantDomain Tenant domain
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException.
     */
    public static void createApplication(String appName, String appOwner, String consumerKey,
                                         String consumerSecret, String tenantDomain) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(appName);
        serviceProvider.setDescription(SP_APP_DESCRIPTION);

        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig
                = new InboundAuthenticationRequestConfig();
        inboundAuthenticationRequestConfig.setInboundAuthKey(consumerKey);
        inboundAuthenticationRequestConfig.setInboundAuthType(INBOUND_AUTH2_TYPE);
        Property property = new Property();
        property.setName("oauthConsumerSecret");
        property.setValue(consumerSecret);
        Property[] properties = { property };
        inboundAuthenticationRequestConfig.setProperties(properties);
        List<InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigs = Arrays
                .asList(inboundAuthenticationRequestConfig);
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                inboundAuthenticationRequestConfigs.toArray(new InboundAuthenticationRequestConfig[0]));
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig
                = new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setUseUserstoreDomainInLocalSubjectIdentifier(true);
        localAndOutboundAuthenticationConfig.setUseTenantDomainInLocalSubjectIdentifier(true);
        localAndOutboundAuthenticationConfig.setSkipConsent(true);
        localAndOutboundAuthenticationConfig.setSkipLogoutConsent(true);
        localAndOutboundAuthenticationConfig.setAuthenticationType("flow");

        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

        // Set requested claim mappings for the SP.
        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setClaimMappings(getRequestedClaimMappings());
        claimConfig.setLocalClaimDialect(true);
        serviceProvider.setClaimConfig(claimConfig);

        MultiTenantFedIdpMgtDataHolder.getInstance().getApplicationManagementService()
                .createApplication(serviceProvider, tenantDomain, appOwner);
    }

    /**
     * Get requested claim mappings.
     *
     * @return array of claim mappings.
     */
    private static ClaimMapping[] getRequestedClaimMappings() {

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);

        Claim firstNameClaim = new Claim();
        firstNameClaim.setClaimUri(FIRST_NAME_CLAIM_URI);
        ClaimMapping firstNameClaimMapping = new ClaimMapping();
        firstNameClaimMapping.setRequested(true);
        firstNameClaimMapping.setLocalClaim(firstNameClaim);
        firstNameClaimMapping.setRemoteClaim(firstNameClaim);

        // add more if required.

        return new ClaimMapping[] { emailClaimMapping, firstNameClaimMapping };
    }


    /**
     * creates or update service provider with the new IDP added to tenant.
     *
     * @param identityProvider identityProvider
     * @param tenantDomain tenantDomain
     */
    public static void createUpdateServiceProviderWithIDP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = identityProvider.getDefaultAuthenticatorConfig();

        if (federatedAuthenticatorConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("The IDP " + identityProvider.getIdentityProviderName()
                        + " has no federated IdP enabled, hence skipping the association.");
            }
            return;
        }

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            ApplicationManagementService applicationMgtService = MultiTenantFedIdpMgtDataHolder.getInstance()
                    .getApplicationManagementService();

            UserRealm userRealm = MultiTenantFedIdpMgtDataHolder.getInstance().getRegistryService().getUserRealm(tenantId);
            String adminUsername = userRealm.getRealmConfiguration().getAdminUserName();

            String serviceProviderName = getServiceProviderName(tenantDomain);

            ServiceProvider serviceProvider = applicationMgtService.
                    getApplicationExcludingFileBasedSPs(serviceProviderName, tenantDomain);
            String consumerKey;
            String consumerSecret;

            if (serviceProvider == null) {
                consumerKey = OAuthUtil.getRandomNumber();
                consumerSecret = OAuthUtil.getRandomNumber();
                List<String> grantTypes = Arrays.asList(AUTHORIZATION_CODE);
                MultiTenantFedIdpMgtUtils
                        .createOAuth2Application(serviceProviderName, SP_CALLBACK_PATH, consumerKey, consumerSecret,
                                adminUsername, tenantId, tenantDomain, grantTypes);
                MultiTenantFedIdpMgtUtils.createApplication(serviceProviderName, adminUsername, consumerKey,
                        consumerSecret, tenantDomain);

                serviceProvider = applicationMgtService.getApplicationExcludingFileBasedSPs(serviceProviderName, tenantDomain);

            }
            addFederatedIDPToServiceProvider(serviceProvider, identityProvider, tenantDomain, adminUsername);
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = Arrays.stream(serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs()).
                    filter(x -> x.getInboundAuthType().equals("oauth2")).findFirst().get();
            consumerKey = inboundAuthenticationRequestConfig.getInboundAuthKey();
            consumerSecret = inboundAuthenticationRequestConfig.getProperties()[0].getValue();
            createIDPSaasTenant(consumerKey, consumerSecret, tenantDomain);

        } catch (IdentityApplicationManagementException | IdentityOAuthAdminException | RegistryException |
                UserStoreException e) {
            throw new IdentityProviderManagementException("Error while creating or updating tenant SP application", e);
        }
    }

    /**
     * creates a IDP in the SaaS tenant.
     *
     * @param consumerKey consumerKey
     * @param consumerSecret consumerSecret
     * @param tenantDomain tenantDomain
     */
    private static void createIDPSaasTenant(String consumerKey, String consumerSecret, String tenantDomain) throws
            IdentityProviderManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(SAAS_TENANT_ID);
            privilegedCarbonContext.setTenantDomain(SAAS_TENANT_DOMAIN);
            IdentityProvider saasTenantIdp = MultiTenantFedIdpMgtDataHolder.getInstance()
                    .getIdpManager().getIdPByName(tenantDomain + IDP_NAME_SUFFIX, SAAS_TENANT_DOMAIN);
            if("default".equals(saasTenantIdp.getIdentityProviderName())) {
                IdentityProvider identityProvider = new IdentityProvider();
                identityProvider.setIdentityProviderName(tenantDomain + IDP_NAME_SUFFIX);
                identityProvider.setAlias(getServerURL("/oauth2/token"));

                FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();

                federatedAuthenticatorConfig.setEnabled(true);
                federatedAuthenticatorConfig.setName("OpenIDConnectAuthenticator");
                federatedAuthenticatorConfig.setDisplayName("openidconnect");

                Property propertyOne = new Property();
                propertyOne.setName("ClientSecret");
                propertyOne.setValue(consumerSecret);
                propertyOne.setConfidential(true);

                Property propertyTwo = new Property();
                propertyTwo.setName("OAuth2TokenEPUrl");
                propertyTwo.setValue(getServerURL("/oauth2/token"));
                propertyTwo.setConfidential(false);

                Property propertyThree = new Property();
                propertyThree.setName("IsBasicAuthEnabled");
                propertyThree.setValue("false");
                propertyThree.setConfidential(false);

                Property propertyFour = new Property();
                propertyFour.setName("OAuth2AuthzEPUrl");
                propertyFour.setValue(getServerURL("/oauth2/authorize"));
                propertyFour.setConfidential(false);

                Property propertyFive = new Property();
                propertyFive.setName("OIDCLogoutEPUrl");
                propertyFive.setValue(getServerURL("/oidc/logout"));
                propertyFive.setConfidential(false);

                Property propertySix = new Property();
                propertySix.setName("ClientId");
                propertySix.setValue(consumerKey);
                propertySix.setConfidential(false);

                Property propertySeven = new Property();
                propertySeven.setName("callbackUrl");
                propertySeven.setValue(getServerURL("/commonauth"));
                propertySeven.setConfidential(false);

                Property propertyEight = new Property();
                propertyEight.setName("IsUserIdInClaims");
                propertyEight.setValue("false");
                propertyEight.setConfidential(false);

                Property propertyNine = new Property();
                propertyNine.setName("commonAuthQueryParams");
                propertyNine.setValue("scope=openid");
                propertyNine.setConfidential(false);

                Property propertyTen = new Property();
                propertyTen.setName("UserInfoUrl");
                propertyTen.setValue(getServerURL("/oauth2/userinfo"));
                propertyTen.setConfidential(false);

                Property[] properties = { propertyOne, propertyTwo, propertyThree, propertyFour, propertyFive,
                        propertySix, propertySeven, propertyEight, propertyNine };

                federatedAuthenticatorConfig.setProperties(properties);
                identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{
                        federatedAuthenticatorConfig});
                identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);

                MultiTenantFedIdpMgtDataHolder.getInstance().getIdpManager().
                        addIdPWithResourceId(identityProvider, SAAS_TENANT_DOMAIN);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * get the full endpoint path with the server url added.
     *
     * @param endpoint endpoint path
     */
    private static String getServerURL(String endpoint) {

        return IdentityUtil.getServerURL(endpoint, true, true);
    }

    /**
     * Adds the given federated IDP to the given service provider.
     *
     * @param serviceProvider serviceProvider (service provider)
     * @param identityProvider federated IDP to add
     * @param tenantDomain tenantDomain
     * @param adminUsername adminUsername
     */
    public static void addFederatedIDPToServiceProvider(ServiceProvider serviceProvider, IdentityProvider identityProvider,
                                                        String tenantDomain, String adminUsername) {
        String applicationName = serviceProvider.getApplicationName();
        String idpName = identityProvider.getIdentityProviderName();
        FederatedAuthenticatorConfig federatedAuthenticatorConfig = identityProvider.getDefaultAuthenticatorConfig();
        try {
            LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                    serviceProvider.getLocalAndOutBoundAuthenticationConfig();
            AuthenticationStep[] authenticationSteps = localAndOutboundAuthenticationConfig.getAuthenticationSteps();
            if (authenticationSteps != null && authenticationSteps.length == 0) {
                AuthenticationStep authenticationStep = new AuthenticationStep();
                authenticationStep.setStepOrder(1);
                authenticationStep.setAttributeStep(true);
                authenticationStep.setSubjectStep(true);
                authenticationStep.setFederatedIdentityProviders(new IdentityProvider[]{identityProvider});
                serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(new
                        AuthenticationStep[]{authenticationStep});
                MultiTenantFedIdpMgtDataHolder.getInstance().
                        getApplicationManagementService().updateApplication(serviceProvider, tenantDomain, adminUsername);
            } else if (authenticationSteps != null && authenticationSteps.length == 1) {
                // Get federated auth step
                AuthenticationStep federatedAuthenticationStep = authenticationSteps[0];

                // Check whether the federated authenticator name matches with the existing authenticators.
                IdentityProvider[] existingIdps = federatedAuthenticationStep.getFederatedIdentityProviders();
                for (IdentityProvider idp : existingIdps) {
                    if (idp.getIdentityProviderName().equals(idpName)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Skipping the SP " + applicationName + " since the IDP " + idpName +
                                    " is already added to its first step.");
                        }
                        return;
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Starting to add the IDP " + idpName + " with " + federatedAuthenticatorConfig.getDisplayName()
                            + " into the first step of the SP " + applicationName);
                }

                ArrayList<IdentityProvider> existingIdpsList = new ArrayList<>(Arrays.asList(existingIdps));

                IdentityProvider idp = new IdentityProvider();
                idp.setIdentityProviderName(idpName);
                idp.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
                idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});
                existingIdpsList.add(idp);

                IdentityProvider[] idpArray = new IdentityProvider[existingIdpsList.size()];
                idpArray = existingIdpsList.toArray(idpArray);
                federatedAuthenticationStep.setFederatedIdentityProviders(idpArray);
                authenticationSteps[0] = federatedAuthenticationStep;
                serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(authenticationSteps);

                MultiTenantFedIdpMgtDataHolder.getInstance().
                        getApplicationManagementService().updateApplication(serviceProvider, tenantDomain, adminUsername);

                if (log.isDebugEnabled()) {
                    log.debug("Successfully added the IDP " + idpName + " with " + federatedAuthenticatorConfig.getDisplayName()
                            + " into the first step of the SP " + applicationName);
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping the SP " + applicationName + " since it does not have a " +
                            "first step or step count is greater than one");
                }
            }
        } catch (IdentityApplicationManagementException e) {
            log.error("Error on updating application " + applicationName + " info.", e);
            throw new RuntimeException("Error on updating application " + applicationName + " info.", e);
        }
    }

}
