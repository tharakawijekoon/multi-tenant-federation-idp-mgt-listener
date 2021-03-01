package org.wso2.custom.multi.tenant.federation.idp.mgt.listener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.custom.multi.tenant.federation.idp.mgt.listener.MultiTenantFederationIdpMgtListener;

@Component(
        name = "org.wso2.custom.multi.tenant.federation.idp.mgt.listener",
        immediate = true)
public class MultiTenantFedIdpMgtListenerServiceComponent {

    private static final Log log = LogFactory.getLog(MultiTenantFedIdpMgtListenerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        try {
            context.getBundleContext()
                    .registerService(IdentityProviderMgtListener.class, new MultiTenantFederationIdpMgtListener(), null);
            if (log.isDebugEnabled()) {
                log.debug("Identity Provider Management Listener is activated.");
            }
        } catch (Throwable e) {
            log.error("Error while activating Identity Provider Management Listener bundle.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("Identity Provider Management Listener bundle deactivated.");
    }

    @Reference(name = "registry.service",
            service = RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Registry Service.");
        }
        MultiTenantFedIdpMgtDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("Un-setting the Registry Service.");
        }
        MultiTenantFedIdpMgtDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(name = "application.mgt.service",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService")
    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Application Management Service.");
        }
        MultiTenantFedIdpMgtDataHolder.getInstance().setApplicationManagementService(applicationManagementService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("Un-setting the Application Management Service.");
        }
        MultiTenantFedIdpMgtDataHolder.getInstance().setApplicationManagementService(null);
    }

    protected void unsetIdpManager(IdpManager idpManager) {

        MultiTenantFedIdpMgtDataHolder.getInstance().setIdpManager(null);
    }

    @Reference(
            name = "IdentityProviderManager",
            service = org.wso2.carbon.idp.mgt.IdpManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdpManager")
    protected void setIdpManager(IdpManager idpManager) {

        MultiTenantFedIdpMgtDataHolder.getInstance().setIdpManager(idpManager);
    }

    @Reference(name = "oauth.admin.service",
            service = OAuthAdminServiceImpl.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuthAdminService")
    protected void setOAuthAdminService(OAuthAdminServiceImpl oAuthAdminService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the OAuth Admin Service.");
        }
        MultiTenantFedIdpMgtDataHolder.getInstance().setOAuthAdminService(oAuthAdminService);
    }

    protected void unsetOAuthAdminService(OAuthAdminServiceImpl oAuthAdminService) {

        if (log.isDebugEnabled()) {
            log.debug("Un-setting the OAuth Admin Service.");
        }
        MultiTenantFedIdpMgtDataHolder.getInstance().setOAuthAdminService(null);
    }
}
