package org.wso2.custom.multi.tenant.federation.idp.mgt.listener.internal;

import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.registry.core.service.RegistryService;

public class MultiTenantFedIdpMgtDataHolder {

    private static MultiTenantFedIdpMgtDataHolder instance = new MultiTenantFedIdpMgtDataHolder();

    private ApplicationManagementService applicationManagementService;

    private IdpManager idpManager;

    private OAuthAdminServiceImpl oAuthAdminService;

    private RegistryService registryService;

    private MultiTenantFedIdpMgtDataHolder() {

    }

    public static MultiTenantFedIdpMgtDataHolder getInstance() {

        return instance;
    }

    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }

    public IdpManager getIdpManager() {
        return idpManager;
    }

    public void setIdpManager(IdpManager idpManager) {
        this.idpManager = idpManager;
    }

    public RegistryService getRegistryService() {

        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    public OAuthAdminServiceImpl getOAuthAdminService() {

        return oAuthAdminService;
    }

    public void setOAuthAdminService(OAuthAdminServiceImpl oAuthAdminService) {

        this.oAuthAdminService = oAuthAdminService;
    }

}
