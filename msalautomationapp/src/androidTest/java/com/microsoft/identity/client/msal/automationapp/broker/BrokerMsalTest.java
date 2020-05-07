package com.microsoft.identity.client.msal.automationapp.broker;

import com.microsoft.identity.client.msal.automationapp.AcquireTokenNetworkTest;

import org.junit.Before;

public abstract class BrokerMsalTest extends AcquireTokenNetworkTest {

    private ITestBroker mBroker; // null or Authenticator or CP

    @Before
    public void setup() {
        super.setup();
        mBroker.install();
    }

    public abstract static class AuthenticatorTest extends BrokerMsalTest {

        public AuthenticatorTest() {
            super.mBroker = new BrokerAuthenticator();
        }
    }

    public abstract static class CompanyPortalTest extends BrokerMsalTest {

        public CompanyPortalTest() {
            super.mBroker = new BrokerCompanyPortal();
        }
    }

}
