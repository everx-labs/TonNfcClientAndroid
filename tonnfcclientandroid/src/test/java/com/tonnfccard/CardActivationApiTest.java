package com.tonnfccard;

import android.content.Context;
import android.nfc.tech.IsoDep;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.CardApiInterface;
import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.NfcMockHelper.SW_SUCCESS;
import static com.tonnfccard.NfcMockHelper.mockAndroidKeyStore;
import static com.tonnfccard.NfcMockHelper.prepareNfcApduRunnerMock;
import static com.tonnfccard.NfcMockHelper.prepareTagMock;
import static com.tonnfccard.TonWalletConstants.BLOCKED_STATE;
import static com.tonnfccard.TonWalletConstants.COMMON_SECRET_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DELETE_KEY_FROM_KEYCHAIN_STATE;
import static com.tonnfccard.TonWalletConstants.ECS_HASH_FIELD;
import static com.tonnfccard.TonWalletConstants.EP_HASH_FIELD;
import static com.tonnfccard.TonWalletConstants.INSTALLED_STATE;
import static com.tonnfccard.TonWalletConstants.IV_SIZE;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.TonWalletConstants.SERIAL_NUMBER_SIZE;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.STATUS_FIELD;
import static com.tonnfccard.TonWalletConstants.SUCCESS_STATUS;
import static com.tonnfccard.TonWalletConstants.WAITE_AUTHENTICATION_STATE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHENTICATION;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
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
public class CardActivationApiTest {
    private CardActivationApi cardActivationApi;
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private final Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private final CardApiInterface<List<String>> getHashOfEncryptedCommonSecret = list -> cardActivationApi.getHashOfEncryptedCommonSecretAndGetJson();
    private final CardApiInterface<List<String>> getHashOfEncryptedPassword = list -> cardActivationApi.getHashOfEncryptedPasswordAndGetJson();
    private final CardApiInterface<List<String>> getHashes = list -> cardActivationApi.getHashesAndGetJson();
    private final CardApiInterface<List<String>> turnOnWallet = list ->  cardActivationApi.turnOnWalletAndGetJson(list.get(0), list.get(1), list.get(2), list.get(3));
    private final CardApiInterface<List<String>> turnOnWalletWithoutPin = list ->  cardActivationApi.turnOnWalletAndGetJson(list.get(0), list.get(1), list.get(2));
    List<CardApiInterface<List<String>>> cardOperationsTurnOnWalletList = Arrays.asList(turnOnWallet, turnOnWalletWithoutPin);
    List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(getHashOfEncryptedCommonSecret, getHashOfEncryptedPassword);
    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(getHashOfEncryptedCommonSecret, getHashOfEncryptedPassword, getHashes);
    public  static final String SN = "050004030904080002040303090001010206080103020306";
    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String IV = "1A550F4B413D0E971C28293F9183EA8A";
    private static final String PASSWORD =  "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";
    private static final String H3 = "71106ED2161D12E5E59FA7FF298930F0F4BB398171A712CB26D947A0DAF5F0EF";
    private static final String SPOILED_H3 = "71106ED2161D12E5E59FA7FF298930F0F4BB398171A712CB26D947A0DAF5F022";
    private static final String H2 = "112716D2053C2828DC265B5DF14F85F203F8350DCB5774950901F3136108FA2C";
    private static final String SPOILED_H2 = "112716D2053C2828DC265B5DF14F85F203F8350DCB5774950901F3136108FABD";

    @Before
    public  void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardActivationApi = new CardActivationApi(context, nfcApduRunner);
    }

    /** Tests for incorrect card responses **/

    /** Invalid RAPDU object/responses from card tests **/

    @Test
    public void testInvalidResponse() {
        Map<CardApiInterface<List<String>>, String> opsErrors = new LinkedHashMap<>();
        opsErrors.put(getHashes, ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getHashOfEncryptedCommonSecret, ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getHashOfEncryptedPassword, ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU("9000"), new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE + 2) + "9000"),
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
    public void testGetHashesInvalidResponse() throws Exception{
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU("9000"), new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE - 2) + "9000"));
        Mockito.doReturn(new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE) + "9000")).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(any());
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(any());
                cardActivationApi.setApduRunner(nfcApduRunnerMock);
                cardActivationApi.getHashesAndGetJson();
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT)));

            }
        });
    }


    @Test
    public void testTurnOnWalletInvalidGetAppInfoResponse() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTag();
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(SW_SUCCESS))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[] {0, 0}, SW_SUCCESS))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(SW_SUCCESS))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[] {0, 0}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 3; i++) {
                try {
                    if (i == 2) {
                        Mockito.doReturn(null).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_APP_INFO_APDU);
                    }
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT)));
                }
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidGetHashOfEncryptedCommonSecretResponse() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTag(WAITE_AUTHENTICATION_STATE);
        byte[] h1 = new byte[SHA_HASH_SIZE - 1]; byte[] h2 = new byte[SHA_HASH_SIZE + 1];
        random.nextBytes(h1); random.nextBytes(h2);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes()))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, SW_SUCCESS))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 4; i++) {
                try {
                    if (i == 3) {
                        Mockito.doReturn(null).when(nfcApduRunnerMock).sendAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
                    }
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT)));
                }
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidGetHashOfEncryptedPasswordResponse() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTag(WAITE_AUTHENTICATION_STATE);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        byte[] h1 = new byte[SHA_HASH_SIZE - 1]; byte[] h2 = new byte[SHA_HASH_SIZE + 1];
        random.nextBytes(h1); random.nextBytes(h2);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 4; i++) {
                try {
                    if (i == 3) {
                        Mockito.doReturn(null).when(nfcApduRunnerMock).sendAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU);
                    }
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT)));
                }
            }
        }
    }

    @Test
    public void testTurnOnWalletInvalidGetSerialNumberResponseLength() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTag(WAITE_AUTHENTICATION_STATE);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        byte[] h1 = new byte[SERIAL_NUMBER_SIZE - 1]; byte[] h2 = new byte[SERIAL_NUMBER_SIZE + 1];
        random.nextBytes(h1); random.nextBytes(h2);
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h1, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(h2, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 4; i++) {
                try {
                    if (i == 3) {
                        Mockito.doReturn(null).when(nfcApduRunnerMock).sendAPDU(GET_SERIAL_NUMBER_APDU);
                    }
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT)));
                }
            }
        }
    }


    /** Test for successfull response from applet **/

    @Test
    public void testGetHashesSuccessfullOperation() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS));
        String hash1 = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        String hash2 = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(hash1), SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(hash2), SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(ECS_HASH_FIELD, hash2);
        jsonResponse.put(EP_HASH_FIELD, hash1);
        jsonResponse.put(STATUS_FIELD, SUCCESS_STATUS);
        String response = cardActivationApi.getHashesAndGetJson();
        assertEquals(response.toLowerCase(), jsonResponse.toString().toLowerCase());
    }

    @Test
    public void testAppletSuccessfullOperations() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS));
        String hash = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(hash), SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(hash), SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < Arrays.asList(getHashOfEncryptedPassword, getHashOfEncryptedCommonSecret).size(); i++) {
            CardApiInterface<List<String>> op = cardOperationsListShort.get(i);
            String response = op.accept(Collections.emptyList());
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(hash).toLowerCase());
        }
    }

    @Test
    public void testTurnOnWalletAppletSuccessfullOperations() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTag();
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), SW_SUCCESS));;
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), SW_SUCCESS));;
        when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        for (int i = 0; i < cardOperationsTurnOnWalletList.size(); i++) {
            String response = cardOperationsTurnOnWalletList.get(i).accept(i == 0 ?
                    Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                    Arrays.asList(PASSWORD, COMMON_SECRET, IV));
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(PERSONALIZED_STATE_MSG).toLowerCase());
        }
    }


    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_COMMAND_ABORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardOperationsList.size(); i++) { ;
            try {
                cardOperationsList.get(i).accept(Collections.emptyList());
                fail();
            }
            catch (Exception e){
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                System.out.println(errMsg);
                System.out.println(e.getMessage());
               // e.printStackTrace();
                assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    @Test
    public void testGetHashesFailedOperation() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_COMMAND_ABORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        try {
            cardActivationApi.getHashesAndGetJson();
            fail();
        }
        catch (Exception e){
            String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
            assertEquals(e.getMessage(), errMsg);
        }
    }


    @Test
    public void testTurnOnWalletAppletFailedOperation() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);;
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(RESET_WALLET_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn( BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS))
                .thenReturn( BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes()))
                .thenReturn( BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), SW_SUCCESS))
                .thenReturn( BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), SW_SUCCESS));
        when(tag.transceive(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS)
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 10; i++) {
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    e.printStackTrace();
                    String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                    System.out.println(errMsg);
                    System.out.println(e.getMessage());
                    assertEquals(e.getMessage(), errMsg);
                }
            }
        }
    }

    /** Test bad applet state **/

    @Test
    public void testTurnOnWalletIncorrectState() throws Exception{
        List<Byte> badStates = Arrays.asList(BLOCKED_STATE, INSTALLED_STATE, DELETE_KEY_FROM_KEYCHAIN_STATE, PERSONALIZED_STATE);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        for(Byte state : badStates){
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, SW_SUCCESS));
            nfcApduRunnerMock.setCardTag(tag);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, IV));
                    fail();
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                    assertTrue(e.getMessage().contains(ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHENTICATION));
                    assertTrue(e.getMessage().startsWith("{"));
                    assertTrue(e.getMessage().endsWith("}"));
                }
            }
        }
    }

    /** Test bad hashes **/

    @Test
    public void testTurnOnWalletBadHashes() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHENTICATION_STATE}, SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SPOILED_H3), SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SPOILED_H3), SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), SW_SUCCESS));
        when(tag.transceive(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SPOILED_H2), SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardActivationApi.setApduRunner(nfcApduRunnerMock);
        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 2; i++) {
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    String msg = i == 0 ? ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_INCORRECT : ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_INCORRECT;
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(msg)));
                }
            }
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
        incorrectPins = Arrays.asList("123456789666", "34", "123", "78900");
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
            for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, password, COMMON_SECRET, IV) :
                            Arrays.asList(password, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PASSWORD_NOT_HEX)));
                }
            }
        });

        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 100; i++) {
                int len = random.nextInt(400);
                if (len == 2 * PASSWORD_SIZE || len % 2 != 0 || len == 0) continue;
                String password = STRING_HELPER.randomHexString(len);
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, password, COMMON_SECRET, IV) :
                            Arrays.asList(password, COMMON_SECRET, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PASSWORD_LEN_INCORRECT)));
                }
            }
        }
    }

    @Test
    public void turnOnWalletAndGetJsonTestBadCommonSecret() {
        List<String> incorrectCS = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7", "123456789", "jj55667788aa");
        incorrectCS.forEach(cs -> {
            for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, cs, IV) :
                            Arrays.asList(PASSWORD, cs, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_COMMON_SECRET_NOT_HEX)));
                }
            }
        });

        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 100; i++) {
                int len = random.nextInt(500);
                if (len == 2 * COMMON_SECRET_SIZE || len % 2 != 0 || len == 0) continue;
                String cs = STRING_HELPER.randomHexString(len);
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, cs, IV) :
                            Arrays.asList(PASSWORD, cs, IV));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_COMMON_SECRET_LEN_INCORRECT)));
                }
            }
        }
    }

    @Test
    public void turnOnWalletAndGetJsonTestBadIv() {
        List<String> incorrectIV = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7", "123456789", "jj55667788aa");
        incorrectIV.forEach(iv -> {
            for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, iv) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, iv));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_INITIAL_VECTOR_NOT_HEX)));
                }
            }
        });

        for (int k = 0; k < cardOperationsTurnOnWalletList.size(); k++) {
            for (int i = 0; i < 100; i++) {
                int len = random.nextInt(500);
                if (len == 2 * IV_SIZE || len % 2 != 0 || len == 0) continue;
                String iv = STRING_HELPER.randomHexString(len);
                try {
                    cardOperationsTurnOnWalletList.get(k).accept(k == 0 ?
                            Arrays.asList(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, iv) :
                            Arrays.asList(PASSWORD, COMMON_SECRET, iv));
                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_INITIAL_VECTOR_LEN_INCORRECT)));
                }
            }
        }
    }

    private IsoDep prepareTag() throws Exception {
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        return tag;
    }

    private IsoDep prepareTag(byte state) throws Exception {
        IsoDep tag = prepareTag();
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        return tag;
    }
}