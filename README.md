# multi-tenant-federation-idp-mgt-listener
Identity provider management listener to automate the identity federation configurations between tenants

In a multi-tenant environement, if you want to expose IDP's in one tenant to another(say to the carbon.super tenant where the SaaS applications exist), the following process should be followed,

  - A service provider should be created in the tenant where the federated IDP's exists 
  - The federated IDP should be added in the outbound configurations of the service provider created
  - In the tenant(carbon.super) you want to access the IDP's add a federated IDP for the SP created in the previous steps.

When there are a large number of tenants and IDP's the above process gets complicated and messy. In order to automate the IDP sharing process between tenants, you can write a IdentityProviderMgtListener that listens to the IDP events such as doPostAddIdP, doPostUpdateIdP etc and automatically triggers the creation/updation of the SP which exposes the IDPs and creates the corresponding IDP for the SP created  in the tenant where the IDP's need to be exposed.

The following items need to be carried out when Adding/Updating an federated IDP in a tenant other than the SaaS SP tenant
  - Check if add/update federated IDP occurs on the SaaS SP tenant, if so ignore.
  - Check if SP for the tenant is created, 
      - If not, created create a new SP. This SP would be used for exposing the IDP's to other tenants.
  - Add the federated IDP to the service provider application's federated idp list in case it is not already added.
  - Create the corresponding IDP for the tenant in the SaaS SP tenant.
  
## Build

Execute the following command to build the project.

```
mvn clean install
```

## Deploy

Copy and place the built JAR artifact from the <PROJECT_HOME>/target/org.wso2.custom.multi.tenant.federation.idp.mgt.listener-1.0.0.jar to the <IS_HOME>/repository/components/dropins directory.

Restart/Start the Identity Server.

## Testing

  - Create a federated IDP in a tenant other than the SaaS SP tenant (carbon.super in this case)

  - A SP would get created in the tenant the federated IDP exists and a IDP corresponding to the SP would get created in the SaaS tenant (carbon.super).
  - The listener has successfully exposed the federated IDP/IDPs registered in another tenant to the SaaS application's tenant. In the SaaS app service provider configurations, you can add the IDP that represents the tenant.
