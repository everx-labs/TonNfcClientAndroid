package com.tonnfccard;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.util.Random;

import static com.tonnfccard.TonWalletApi.JSON_HELPER;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.FALSE_MSG;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_CONTEXT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class NfcApiTest {
    protected final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    protected final StringHelper STRING_HELPER = StringHelper.getInstance();
    protected final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    protected final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    protected Random random = new Random();
    private NfcApi nfcApi;

    @Before
    public  void init() {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApi = new NfcApi(context);
    }

    @Test
    public void testCheckIfNfcSupportedFail() {
        try {
            nfcApi.setActivity(null);
            nfcApi.checkIfNfcSupportedAndGetJson();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NO_CONTEXT)));
        }
    }

    @Test
    public void testCheckIfNfcSupported() {
        try {
            Context context = ApplicationProvider.getApplicationContext();
            Context contextMock = Mockito.spy(context);
            PackageManager packageManager = mock(PackageManager.class);
            when(contextMock.getPackageManager()).thenReturn(packageManager);
            when(packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)).thenReturn(false);
            nfcApi.setActivity(contextMock);
            String response = nfcApi.checkIfNfcSupportedAndGetJson();
            assertEquals(response, JSON_HELPER.createResponseJson(FALSE_MSG));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCheckIfNfcEnabledFail() {
        try {
            nfcApi.setActivity(null);
            nfcApi.checkIfNfcEnabledAndGetJson();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NO_CONTEXT)));
        }
    }

    @Test
    public void testCheckIfNfcEnabled() {
        try {
            nfcApi.setActivity(ApplicationProvider.getApplicationContext());
            NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
            when(nfcAdapterMock.isEnabled())
                    .thenReturn(false);
            MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class);
            nfcAdapterMockedStatic
                    .when(() -> NfcAdapter.getDefaultAdapter(any()))
                    .thenReturn(nfcAdapterMock);
            String response = nfcApi.checkIfNfcEnabledAndGetJson();
            assertEquals(response, JSON_HELPER.createResponseJson(FALSE_MSG));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testOpenNfcSettingsFail() {
        try {
            nfcApi.setActivity(null);
            nfcApi.openNfcSettingsAndGetJson();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NO_CONTEXT)));
        }
    }

    @Test
    public void testOpenNfcSettings() {
        try {
            nfcApi.setActivity(ApplicationProvider.getApplicationContext());
            String response = nfcApi.openNfcSettingsAndGetJson();
            assertEquals(response, JSON_HELPER.createResponseJson(DONE_MSG));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}