package com.tonnfccard;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class RecoveryDataApiTest {
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private Context context;
    private final RAPDU SUCCESS_RAPDU = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

    private RecoveryDataApi recoveryDataApi;

    private final CardApiInterface<List<String>> addRecoveryData = list -> recoveryDataApi.addRecoveryDataAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getRecoveryData = list -> recoveryDataApi.getRecoveryDataAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataHash = list -> recoveryDataApi.getRecoveryDataHashAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataLen = list -> recoveryDataApi.getRecoveryDataLenAndGetJson();
    private final CardApiInterface<List<String>> isRecoveryDataSet = list -> recoveryDataApi.isRecoveryDataSetAndGetJson();
    private final CardApiInterface<List<String>> resetRecovery = list -> recoveryDataApi.resetRecoveryDataAndGetJson();


    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        recoveryDataApi = new RecoveryDataApi(context, nfcApduRunner);
    }




}