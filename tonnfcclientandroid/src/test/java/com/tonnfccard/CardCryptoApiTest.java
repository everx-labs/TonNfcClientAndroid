package com.tonnfccard;

import android.content.Context;
import android.nfc.tech.IsoDep;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CardCryptoApiTest extends TonWalletApiTest {
    private CardCryptoApi cardCryptoApi;
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private Context context;
    private final RAPDU SUCCESS_RAPDU = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

    private final CardApiInterface<List<String>> sign = list -> cardCryptoApi.signAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPinAndSign = list -> cardCryptoApi.verifyPinAndSignAndGetJson(list.get(0), list.get(1), list.get(2));
    private final CardApiInterface<List<String>> signForDefaultHdPath = list -> cardCryptoApi.signForDefaultHdPathAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> verifyPinAndSignForDefaultHdPath = list -> cardCryptoApi.verifyPinAndSignForDefaultHdPathAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPin = list -> cardCryptoApi.verifyPinAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKey = list -> cardCryptoApi.getPublicKeyAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKeyForDefaultPath = list -> cardCryptoApi.getPublicKeyForDefaultPathAndGetJson();

    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(sign, verifyPinAndSign, signForDefaultHdPath, verifyPinAndSignForDefaultHdPath,
           verifyPin, getPublicKey, getPublicKeyForDefaultPath);

    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardCryptoApi = new CardCryptoApi(context, nfcApduRunner);
    }

    /** Common tests for NFC errors**/

    private void prepareNfcTest(String errMsg) {
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "1")
                    : i == 1 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "2", DEFAULT_PIN_STR)
                    : i == 2 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH))
                    : i == 3 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), DEFAULT_PIN_STR)
                    : i == 4 ? Collections.singletonList(DEFAULT_PIN_STR)
                    : i == 5 ? Collections.singletonList("3")
                    : Collections.emptyList();
            CardApiInterface<List<String>> op = cardOperationsList.get(i);
            try {
                op.accept(args);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        }
    }

    @Test
    public void testNoNfcTag() throws Exception{
        IsoDep isoDep = null;
        nfcApduRunner.setCardTag(isoDep);
        cardCryptoApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_TAG);
    }

    @Test
    public void testNoNfc() {
        mockNfcAdapterToBeNull(nfcApduRunner);
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        cardCryptoApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_NFC);
    }

    @Test
    public void testNfcDisabled() {
        mockNfcAdapter(nfcApduRunner, false);
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        cardCryptoApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_DISABLED);
    }

    @Test
    public void testNfcConnectFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        nfcApduRunner.setCardTag(isoDep);
        cardCryptoApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testNfcTransceiveFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        IsoDep tag = prepareTagMock();
        Mockito.doThrow(new IOException()).when(tag).transceive(any());
        nfcApduRunner.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_TRANSCEIVE + ", More details: null");
    }

    @Test
    public void testNfcTooShortResponse() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = prepareTagMock();
            if (i <  2) {
                when(tag.transceive(any())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(any())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            cardCryptoApi.setApduRunner(nfcApduRunner);
            prepareNfcTest(ERROR_BAD_RESPONSE);
        }
    }

    /** Tests for incorrect input arguments **/

    @Test
    public void testBadData() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckBadData = Arrays.asList(signForDefaultHdPath, verifyPinAndSignForDefaultHdPath);
        Map<String, String> badDataToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ABC", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("98777ff", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ssAA", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("12n", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("1234k7", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE + 2), ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE + 100), ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT);
        }};
        badDataToErrMsg.keySet().forEach(data -> {
            for(int i = 0 ; i < cardOperationsListToCheckBadData.size(); i++) {
                try {
                    cardOperationsListToCheckBadData.get(i).accept(getArgForDataTest(data, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badDataToErrMsg.get(data))));
                }
            }
        });
    }

    private List<String> getArgForDataTest(String data, int i) {
        return  i == 0 ? Arrays.asList(data)
                : Arrays.asList(data,  DEFAULT_PIN_STR);
    }


    @Test
    public void testBadDataWithHd() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckBadDataWithoutHd = Arrays.asList(sign, verifyPinAndSign);
        Map<String, String> badDataToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ABC", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("98777ff", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ssAA", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("12n", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("1234k7", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH + 2), ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH + 100), ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT);
        }};
        badDataToErrMsg.keySet().forEach(data -> {
            for(int i = 0 ; i < cardOperationsListToCheckBadDataWithoutHd.size(); i++) {
                try {
                    cardOperationsListToCheckBadDataWithoutHd.get(i).accept(getArgForDataWithHdTest(data, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badDataToErrMsg.get(data))));
                }
            }
        });
    }

    private List<String> getArgForDataWithHdTest(String data, int i) {
        return  i == 0 ? Arrays.asList(data, "1")
                : Arrays.asList(data, "1", DEFAULT_PIN_STR);
    }

    @Test
    public void testBadHdIndexArg() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckHdIndex = Arrays.asList(sign, verifyPinAndSign, getPublicKey);
        Map<String, String> badHdIndexToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("012a", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("98777f", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("ssAA", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("12n", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("12345678901", ERROR_MSG_HD_INDEX_LEN_INCORRECT);
            put("233333344441", ERROR_MSG_HD_INDEX_LEN_INCORRECT);
        }};
        badHdIndexToErrMsg.keySet().forEach(hdIndex -> {
            for(int i = 0 ; i < cardOperationsListToCheckHdIndex.size(); i++) {
                try {
                    cardOperationsListToCheckHdIndex.get(i).accept(getArgForHdIndexTest(hdIndex, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badHdIndexToErrMsg.get(hdIndex))));
                }
            }
        });
    }

    private List<String> getArgForHdIndexTest(String hdIndex, int i) {
        return  i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), hdIndex)
                : i == 1 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), hdIndex, DEFAULT_PIN_STR)
                : Collections.singletonList(hdIndex);
    }

    @Test
    public void testBadPinArg() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckPin = Arrays.asList(verifyPinAndSign, verifyPinAndSignForDefaultHdPath, verifyPin);
        Map<String, String> badPinToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("012a", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("98777f", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("ssAA", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("12n", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("123", ERROR_MSG_PIN_LEN_INCORRECT);
            put("56733", ERROR_MSG_PIN_LEN_INCORRECT);
            put("233333344441", ERROR_MSG_PIN_LEN_INCORRECT);
        }};
        badPinToErrMsg.keySet().forEach(hdIndex -> {
            for(int i = 0 ; i < cardOperationsListToCheckPin .size(); i++) {
                try {
                    cardOperationsListToCheckPin.get(i).accept(getArgForPinTest(hdIndex, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badPinToErrMsg.get(hdIndex))));
                }
            }
        });
    }

    private List<String> getArgForPinTest(String pin, int i) {
        return  i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "1", pin)
                : i == 1 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), pin)
                : Collections.singletonList(pin);
    }





}