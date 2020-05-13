// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package com.microsoft.identity.client.msal.automationapp;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.ui.automation.app.IApp;
import com.microsoft.identity.client.ui.automation.broker.ITestBroker;
import com.microsoft.identity.client.ui.automation.utils.CommonUtils;
import com.microsoft.identity.common.internal.util.StringUtil;
import com.microsoft.identity.internal.testutils.TestUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;

import static com.microsoft.identity.common.adal.internal.AuthenticationConstants.Broker.AZURE_AUTHENTICATOR_APP_PACKAGE_NAME;
import static com.microsoft.identity.common.adal.internal.AuthenticationConstants.Broker.COMPANY_PORTAL_APP_PACKAGE_NAME;
import static junit.framework.Assert.fail;

public abstract class AcquireTokenAbstractTest extends PublicClientApplicationAbstractTest implements IAcquireTokenTest {

    private static final String TAG = AcquireTokenAbstractTest.class.getSimpleName();

    protected String[] mScopes;
    protected ITestBroker mBroker;
    protected IAccount mAccount;
    protected IApp mBrowser;

    public IAccount getAccount() {
        return mAccount;
    }

    @Before
    public void setup() {
        // remove existing authenticator and company portal apps - the test app is removed
        // by the Android Test Orchestrator. See build.gradle
        CommonUtils.removeApp(AZURE_AUTHENTICATOR_APP_PACKAGE_NAME);
        CommonUtils.removeApp(COMPANY_PORTAL_APP_PACKAGE_NAME);

        mScopes = getScopes();
        mBroker = getBroker();
        mBrowser = getBrowser();

        mBrowser.clear();

        super.setup();
    }

    @After
    public void cleanup() {
        super.cleanup();
        AcquireTokenTestHelper.setAccount(null);
        // remove everything from cache after test ends
        TestUtils.clearCache(SHARED_PREFERENCES_NAME);
        // this is not needed as test app is removed by orchestrator
        //CommonUtils.clearApp(mContext.getPackageName());
    }

    public AuthenticationCallback successfulInteractiveCallback(final CountDownLatch latch, final Context context) {
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                showMessageWithToast(authenticationResult.getAccessToken());
                Assert.assertFalse(StringUtil.isEmpty(authenticationResult.getAccessToken()));
                mAccount = authenticationResult.getAccount();
                latch.countDown();
            }

            @Override
            public void onError(MsalException exception) {
                fail(exception.getMessage());
                latch.countDown();
            }

            @Override
            public void onCancel() {
                fail("User cancelled flow");
                latch.countDown();
            }
        };

        return callback;
    }


    public SilentAuthenticationCallback successfulSilentCallback(final CountDownLatch latch, final Context context) {
        SilentAuthenticationCallback callback = new SilentAuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                showMessageWithToast(authenticationResult.getAccessToken());
                Assert.assertFalse(StringUtil.isEmpty(authenticationResult.getAccessToken()));
                mAccount = authenticationResult.getAccount();
                latch.countDown();
            }

            @Override
            public void onError(MsalException exception) {
                fail(exception.getMessage());
                latch.countDown();
            }
        };

        return callback;
    }

    public AuthenticationCallback failureInteractiveCallback(final CountDownLatch latch, final String errorCode, final Context context) {
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                fail("Unexpected success");
                latch.countDown();
            }

            @Override
            public void onError(MsalException exception) {
                showMessageWithToast(exception.getErrorCode());
                Assert.assertEquals(errorCode, exception.getErrorCode());
                latch.countDown();
            }

            @Override
            public void onCancel() {
                fail("User cancelled flow");
                latch.countDown();
            }
        };

        return callback;
    }

    public SilentAuthenticationCallback failureSilentCallback(final CountDownLatch latch, final String errorCode, final Context context) {
        SilentAuthenticationCallback callback = new SilentAuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                fail("Unexpected success");
                latch.countDown();
            }

            @Override
            public void onError(MsalException exception) {
                showMessageWithToast(exception.getErrorCode());
                Assert.assertSame(errorCode, exception.getErrorCode());
                latch.countDown();
            }
        };

        return callback;
    }

    private void showMessageWithToast(final String msg) {
        new Handler(mActivity.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mActivity.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
