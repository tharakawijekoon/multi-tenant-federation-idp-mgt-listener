package org.wso2.custom.multi.tenant.federation.idp.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.listener.AbstractIdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.custom.multi.tenant.federation.idp.mgt.listener.util.MultiTenantFedIdpMgtUtils;

import static org.wso2.custom.multi.tenant.federation.idp.mgt.listener.util.MultiTenantFedIdpMgtConstants.SAAS_TENANT_DOMAIN;

public class MultiTenantFederationIdpMgtListener extends AbstractIdentityProviderMgtListener {

    private static final Log log = LogFactory.getLog(MultiTenantFederationIdpMgtListener.class);

    @Override
    public int getDefaultOrderId() {
        // Execute as the last event listener
        return 9999;
    }

    @Override
    public boolean doPostAddIdP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (SAAS_TENANT_DOMAIN.equals(tenantDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping doPostAddIdP on SaaS SP tenant");
            }
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("doPostAddIdP started for the IDP " + identityProvider.getIdentityProviderName());
        }

        MultiTenantFedIdpMgtUtils.createUpdateServiceProviderWithIDP(identityProvider, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("doPostAddIdP completed for the IDP " + identityProvider.getIdentityProviderName());
        }
        return true;
    }

    @Override
    public boolean doPostUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (SAAS_TENANT_DOMAIN.equals(tenantDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping doPostUpdateIdP on SaaS SP tenant");
            }
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("doPostUpdateIdP started for the IDP " + identityProvider.getIdentityProviderName());
        }
        MultiTenantFedIdpMgtUtils.createUpdateServiceProviderWithIDP(identityProvider, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("doPostUpdateIdP completed for the IDP " + identityProvider.getIdentityProviderName());
        }
        return true;
    }

}
