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
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.TonWalletConstants.BLOCKED_STATE;
import static com.tonnfccard.TonWalletConstants.COMMON_SECRET_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DELETE_KEY_FROM_KEYCHAIN_STATE;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.FALSE_MSG;
import static com.tonnfccard.TonWalletConstants.INSTALLED_STATE;
import static com.tonnfccard.TonWalletConstants.IV_SIZE;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.TonWalletConstants.RECOVERY_DATA_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.SERIAL_NUMBER_SIZE;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.TonWalletConstants.WAITE_AUTHORIZATION_STATE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHORIZATION;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APPLET_STATE_APDU_LIST;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_PASSWORD_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getVerifyPasswordAPDU;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CardActivationApiTest extends TonWalletApiTest {
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

    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String IV = "1A550F4B413D0E971C28293F9183EA8A";
    private static final String PASSWORD =  "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";
    private static final String H3 = "71106ED2161D12E5E59FA7FF298930F0F4BB398171A712CB26D947A0DAF5F0EF";
    private static final String SPOILED_H3 = "71106ED2161D12E5E59FA7FF298930F0F4BB398171A712CB26D947A0DAF5F022";
    private static final String H2 = "112716D2053C2828DC265B5DF14F85F203F8350DCB5774950901F3136108FA2C";
    private static final String SPOILED_H2 = "112716D2053C2828DC265B5DF14F85F203F8350DCB5774950901F3136108FABD";

    /**
     * {"CS":"7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4","P1":"F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE","ECS":"71E872C73979904C17722CB2A5FA6B7A107DBA38924338F739A1C0E96D74BC33","H1":"6F83BBEF900614F609DDBBB0CC014CC1ED19A30A40E5E171C5734901B8047705","H2":"112716D2053C2828DC265B5DF14F85F203F8350DCB5774950901F3136108FA2C","SN":"504394802433901126813236","H3":"71106ED2161D12E5E59FA7FF298930F0F4BB398171A712CB26D947A0DAF5F0EF","IV":"1A550F4B413D0E971C28293F9183EA8A","B1":"7BF6D157F017189AE9904959878A851376BE01127582D675004790CFC194E6AC273D85F55B7050B08FC48F3142AA68974B9765D0799BB5804F6FD4A4BF38686D8E1AE548E60603D32DD85C57DADB146CDE4CFD30D0321DCD5A2B8010760E70A93E429FBC2A458FE84B63B35DB9902893E2C81CD53A2AA20E268A57D188F93D69"}
     */

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
        mockNfcAdapterToBeNull(nfcApduRunner);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_NFC);
    }

    @Test
    public void testNfcDisabled() {
        mockNfcAdapter(nfcApduRunner, false);
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_DISABLED);
    }

    @Test
    public void testNfcConnectFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        nfcApduRunner.setCardTag(isoDep);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testNfcTransceiveFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        IsoDep tag =  prepareTagMock();
        Mockito.doThrow(new IOException()).when(tag).transceive(any());
        nfcApduRunner.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_TRANSCEIVE + ", More details: null");
    }

    @Test
    public void testNfcTooShortResponse() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
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
    public void testInvalidRAPDUAndInvalidResponseLength() throws Exception {
        Map<CardApiInterface<List<String>>, String> opsErrors = new LinkedHashMap<>();
        opsErrors.put(getHashOfEncryptedCommonSecret, ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getHashOfEncryptedPassword, ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE + 2) + "9000"),
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

    @Test
    public void testTurnOnWalletInvalidResponseLength() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[] {0, 0}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < 2; i++) {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidResponseLength1() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        Mockito.doReturn(null).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_APP_INFO_APDU);
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        try {
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT)));
        }
    }

    @Test
    public void testTurnOnWalletInvalidResponseLength2() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        byte[] h1 = new byte[SHA_HASH_SIZE - 1];
        byte[] h2 = new byte[SHA_HASH_SIZE + 1];
        random.nextBytes(h1); random.nextBytes(h2);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < 3; i++) {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidResponseLength21() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        Mockito.doReturn(null).when(nfcApduRunnerMock).sendAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        try {
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT)));
        }
    }

    @Test
    public void testTurnOnWalletInvalidResponseLength3() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        byte[] h1 = new byte[SHA_HASH_SIZE - 1];
        byte[] h2 = new byte[SHA_HASH_SIZE + 1];
        random.nextBytes(h1); random.nextBytes(h2);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < 3; i++) {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidResponseLength31() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        Mockito.doReturn(null).when(nfcApduRunnerMock).sendAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU);
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < 3; i++) {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidResponseLength4() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        byte[] h1 = new byte[SERIAL_NUMBER_SIZE - 1];
        byte[] h2 = new byte[SERIAL_NUMBER_SIZE + 1];
        random.nextBytes(h1); random.nextBytes(h2);
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < 3; i++) {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidResponseLength41() throws Exception { ;
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        Mockito.doReturn(null).when(nfcApduRunnerMock).sendAPDU(GET_SERIAL_NUMBER_APDU);
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < 3; i++) {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT)));
            }
        }
    }

    /** Test for successfull response from applet **/

    @Test
    public void testAppletSuccessfullOperations() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x27}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        String hash1 = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        String hash2 = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(getHashOfEncryptedPassword, hash1 + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getHashOfEncryptedCommonSecret, hash2 + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            CardApiInterface<List<String>> op = cardOperationsListShort.get(i);
            IsoDep tag =  prepareTagMock();
            when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(map.get(op)));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            String response = op.accept(Collections.emptyList());
            String msg = map.get(op).substring(0, map.get(op).length() - 4);
            System.out.println(response.toLowerCase());
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(msg).toLowerCase());
        }
    }

    @Test
    public void testTurnOnWalletAppletSuccessfullOperations() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            mockAndroidKeyStore();
            String response = cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(PERSONALIZED_STATE_MSG).toLowerCase());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_COMMAND_ABORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x27}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        IsoDep tag =  prepareTagMock();
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

    @Test
    public void testTurnOnWalletAppletFailedOperation() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_COMMAND_NOT_ALLOWED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);;
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_NOT_ALLOWED));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        try {
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            e.printStackTrace();
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            System.out.println(errMsg);
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), errMsg);
        }
    }


    @Test
    public void testTurnOnWalletAppletFailedOperation1() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);;
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        try {
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            e.printStackTrace();
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            System.out.println(errMsg);
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation2() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        try {
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation3() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_APPLET_SELECT_FAILED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_APPLET_SELECT_FAILED));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation4() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn( BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));;
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation5() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn( BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation6() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation7() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation8() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }

    @Test
    public void testTurnOnWalletAppletFailedOperation9() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }



    /** Test bad applet state **/

    @Test
    public void testTurnOnWalletIncorrectState() throws Exception{
        List<Byte> badStates = Arrays.asList(BLOCKED_STATE, INSTALLED_STATE, DELETE_KEY_FROM_KEYCHAIN_STATE, PERSONALIZED_STATE);
        badStates.forEach(state -> {
            NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
            try {
                IsoDep tag = prepareTagMock();
                when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
                when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
                when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
                when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
                nfcApduRunnerMock.setCardTag(tag);
                cardActivationApi.setApduRunner(nfcApduRunnerMock);
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                assertTrue(e.getMessage().contains(ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHORIZATION));
                assertTrue(e.getMessage().startsWith("{"));
                assertTrue(e.getMessage().endsWith("}"));
            }
        });
    }

    /** Test bad hashes **/

    @Test
    public void testTurnOnWalletBadashOfEncryptedCommonSecret() {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SPOILED_H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_INCORRECT)));
        }
    }

    @Test
    public void testTurnOnWalletBadashOfEncryptedPassword() {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        try {
            IsoDep tag = prepareTagMock();
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SPOILED_H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            fail();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_INCORRECT)));
        }
    }


    /** Tests for incorrect input arguments **/

    /** turnOnWallet **/
    @Test
    public void turnOnWalletAndGetJsonTestBadPin() {
        List<String> incorrectPins = Arrays.asList(null, "", "ABC", "123456789666g", "1s34","ssAA", "1234k7");
        incorrectPins.forEach(pin -> {
            try {
                cardActivationApi.turnOnWalletAndGetJson(pin, PASSWORD, COMMON_SECRET, IV);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT)));
            }
        });
        incorrectPins = Arrays.asList( "123456789666", "34", "123", "78900");
        incorrectPins.forEach(pin -> {
            try {
                cardActivationApi.turnOnWalletAndGetJson(pin, PASSWORD, COMMON_SECRET, IV);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PIN_LEN_INCORRECT)));
            }
        });

    }


    @Test
    public void turnOnWalletAndGetJsonTestBadPassword() {
        List<String> incorrectPasswords = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7");
        incorrectPasswords.forEach(password -> {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, password, COMMON_SECRET, IV);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PASSWORD_NOT_HEX)));
            }
        });

        for(int i = 0 ; i < 100 ; i++) {
            int len = random.nextInt(400);
            if (len == 2 * PASSWORD_SIZE || len % 2 != 0 || len == 0) continue;
            String password = STRING_HELPER.randomHexString(len);
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, password, COMMON_SECRET, IV);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PASSWORD_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void turnOnWalletAndGetJsonTestBadCommonSecret() {
        List<String> incorrectCS = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7", "123456789", "jj55667788aa");
        incorrectCS.forEach(cs -> {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, cs, IV);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_COMMON_SECRET_NOT_HEX)));
            }
        });

        for(int i = 0 ; i < 100 ; i++) {
            int len = random.nextInt(500);
            if (len == 2 * COMMON_SECRET_SIZE || len % 2 != 0 || len == 0) continue;
            String cs = STRING_HELPER.randomHexString(len);
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, cs, IV);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_COMMON_SECRET_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void turnOnWalletAndGetJsonTestBadIv() {
        List<String> incorrectIV = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7", "123456789", "jj55667788aa");
        incorrectIV.forEach(iv -> {
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, iv);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_INITIAL_VECTOR_NOT_HEX)));
            }
        });

        for(int i = 0 ; i < 100 ; i++) {
            int len = random.nextInt(500);
            if (len == 2 * IV_SIZE || len % 2 != 0 || len == 0) continue;
            String iv = STRING_HELPER.randomHexString(len);
            try {
                cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, iv);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_INITIAL_VECTOR_LEN_INCORRECT)));
            }
        }
    }
}