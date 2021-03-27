package com.tonnfccard;

import android.content.Context;
import android.nfc.NfcAdapter;
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
import org.mockito.MockedStatic;
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

import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.FALSE_MSG;
import static com.tonnfccard.TonWalletConstants.RECOVERY_DATA_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APPLET_STATE_APDU_LIST;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CardActivationApiTest {
    private CardActivationApi cardActivationApi;
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private Context context;
    private final RAPDU SUCCESS_RAPDU = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

    private final CardApiInterface<List<String>> turnOnWallet = list -> cardActivationApi.turnOnWalletAndGetJson(list.get(0), list.get(1), list.get(2), list.get(3));
    private final CardApiInterface<List<String>> getHashOfEncryptedCommonSecret = list -> cardActivationApi.getHashOfEncryptedCommonSecretAndGetJson();
    private final CardApiInterface<List<String>> getHashOfEncryptedPassword = list -> cardActivationApi.getHashOfEncryptedPasswordAndGetJson();

    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(turnOnWallet, getHashOfEncryptedCommonSecret, getHashOfEncryptedPassword);
    List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(getHashOfEncryptedCommonSecret, getHashOfEncryptedPassword);

    private static final String SERIAL_NUMBER = "504394802433901126813236";
    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String IV = "1A550F4B413D0E971C28293F9183EA8A";
    private static final String PASSWORD =  "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";


    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardActivationApi = new CardActivationApi(context, nfcApduRunner);
    }

    /** Common tests for NFC errors**/

    private void prepareNfcTest(String errMsg) {
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = i == 0 ? Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV)
                    : Collections.emptyList();
            CardApiInterface<List<String>> op = cardOperationsList.get(i);

            try {
                op.accept(args);
                fail();
            }
            catch (Exception e){
                e.printStackTrace();
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        }
    }

    @Test
    public void testNoNfcTag() throws Exception{
        IsoDep isoDep = null;
        nfcApduRunner.setCardTag(isoDep);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_TAG);
    }

    @Test
    public void testNoNfc() {
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class);
        nfcAdapterMockedStatic
                .when(() -> NfcAdapter.getDefaultAdapter(any()))
                .thenReturn(null);
        nfcApduRunner.setNfcAdapter(null);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_NFC);
    }

    @Test
    public void testNfcDisabled() {
        IsoDep isoDep = mock(IsoDep.class);
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(false);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_DISABLED);
    }

    @Test
    public void testNfcConnectFail() throws Exception{
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testNfcTransceiveFail() throws Exception{
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        Mockito.doThrow(new IOException()).when(tag).transceive(any());
        nfcApduRunner.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_TRANSCEIVE + ", More details: null");
    }

    @Test
    public void testNfcTooShortResponse() throws Exception{
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            if (i <  2) {
                when(tag.transceive(any())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(any())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunner);
            prepareNfcTest(ERROR_BAD_RESPONSE);
        }
    }

    /** Tests for incorrect card responses **/

    /** Invalid RAPDU object/responses from card tests **/

    @Test
    public void testInvalidRAPDUAndInvalisResponseLength() throws Exception {
        Map<CardApiInterface<List<String>>, String> opsErrors = new LinkedHashMap<>();
        opsErrors.put(getHashOfEncryptedCommonSecret, ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getHashOfEncryptedPassword, ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        List<RAPDU> badRapdus= Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE + 2) + "9000"),
               new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE - 2) + "9000"));
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(any());
                cardActivationApi.setApduRunner(nfcApduRunnerMock);
                for (CardApiInterface<List<String>> op : opsErrors.keySet()) {
                    try {
                        op.accept(Collections.emptyList());
                        fail();
                    } catch (Exception e) {
                        assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(opsErrors.get(op))));
                    }
                }
            }
            catch (Exception e) {
                fail();
            }
        });
    }

    /** Test for successfull response from applet **/

    @Test
    public void testAppletSuccessfullOperations() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x27}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        String hash1 = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        String hash2 = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(getHashOfEncryptedPassword, hash1 + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getHashOfEncryptedCommonSecret, hash2 + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            CardApiInterface<List<String>> op = cardOperationsListShort.get(i);
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(map.get(op)));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            String response = op.accept(Collections.emptyList());
            String msg = map.get(op).substring(0, map.get(op).length() - 4);
            System.out.println(response.toLowerCase());
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(msg).toLowerCase());
        }
    }

    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception{
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_COMMAND_ABORTED));
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x27}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) { ;
            try {
                cardOperationsListShort.get(i).accept(Collections.emptyList());
                fail();
            }
            catch (Exception e){
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                System.out.println(errMsg);
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), errMsg);
            }
        }
    }




}