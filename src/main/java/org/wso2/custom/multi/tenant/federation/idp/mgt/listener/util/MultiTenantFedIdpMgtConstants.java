package org.wso2.custom.multi.tenant.federation.idp.mgt.listener.util;

/**
 * This class holds the constants related to Tenant SP which would get created.
 */
public class MultiTenantFedIdpMgtConstants {

    public static final String INBOUND_AUTH2_TYPE = "oauth2";

    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static final String FIRST_NAME_CLAIM_URI ="http://wso2.org/claims/givenname";


    public static final String SP_NAME_SUFFIX = "-tenant-sp";
    public static final String SP_CALLBACK_PATH = "/commonauth";
    public static final String SP_APP_DESCRIPTION = "This is the application which exposes IDPs to SaaS SP tenant";

    public static final String IDP_NAME_SUFFIX = "-IDPs";
    public static final String SAAS_TENANT_DOMAIN = "carbon.super";
    public static final int SAAS_TENANT_ID = -1234;

    private MultiTenantFedIdpMgtConstants() {

    }


}
