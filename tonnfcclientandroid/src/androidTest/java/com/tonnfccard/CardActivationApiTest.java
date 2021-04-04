package com.tonnfccard;

import android.content.Context;
import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.KeyStore;
import java.util.Random;

import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.TonWalletConstants.WAITE_AUTHORIZATION_STATE;
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
    private static final String SERIAL_NUMBER = "504394802433901126813236";
    private static final String SN = "050004030904080002040303090001010206080103020306";
    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String IV = "1A550F4B413D0E971C28293F9183EA8A";
    private static final String PASSWORD =  "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";
    private static final String H3 = "71106ED2161D12E5E59FA7FF298930F0F4BB398171A712CB26D947A0DAF5F0EF";
    private static final String H2 = "112716D2053C2828DC265B5DF14F85F203F8350DCB5774950901F3136108FA2C";

    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardActivationApi = new CardActivationApi(context, nfcApduRunner);
    }

    @Test
    public void testTurnOnWalletAppletSuccessfullOperations()  throws Exception {
        try {
            NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
            Mockito.doReturn(SUCCESS_RAPDU).when(nfcApduRunnerMock).sendCoinManagerAppletAPDU(RESET_WALLET_APDU);
            Mockito.doReturn(SUCCESS_RAPDU).when(nfcApduRunnerMock).sendAPDU(getGenerateSeedAPDU(DEFAULT_PIN));
            Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{WAITE_AUTHORIZATION_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                    .doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                    .when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_APP_INFO_APDU);
            Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H3), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                    .when(nfcApduRunnerMock).sendAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
            Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(H2), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                    .when(nfcApduRunnerMock).sendAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU);
            Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)))
                    .when(nfcApduRunnerMock).sendAPDU(getVerifyPasswordAPDU(BYTE_ARRAY_HELPER.bytes(PASSWORD), BYTE_ARRAY_HELPER.bytes(IV)));
            Mockito.doReturn(SUCCESS_RAPDU).when(nfcApduRunnerMock).sendCoinManagerAppletAPDU(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN));
            Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                    .when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_SERIAL_NUMBER_APDU);
            cardActivationApi.setApduRunner(nfcApduRunnerMock);
            String response = cardActivationApi.turnOnWalletAndGetJson(DEFAULT_PIN_STR, PASSWORD, COMMON_SECRET, IV);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(PERSONALIZED_STATE_MSG).toLowerCase());
            System.out.println(response);
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            String keyAlias = HmacHelper.HMAC_KEY_ALIAS + SERIAL_NUMBER;
            keyStore.load(null);
            assertTrue(keyStore.containsAlias(keyAlias));
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

}