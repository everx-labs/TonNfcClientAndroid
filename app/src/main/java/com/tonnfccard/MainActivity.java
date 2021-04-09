package com.tonnfccard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.utils.ByteArrayUtil;

import static com.tonnfccard.CardKeyChainApi.NUMBER_OF_KEYS_FIELD;
import static com.tonnfccard.TonWalletConstants.MAX_KEY_SIZE_IN_KEYCHAIN;
import static com.tonnfccard.TonWalletConstants.MESSAGE_FIELD;
import static com.tonnfccard.TonWalletConstants.NOT_GENERATED_MSG;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.TonWalletConstants.WAITE_AUTHORIZATION_MSG;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private static final String DEFAULT_PIN = "5555";

    // AES parameters
    private static final int AES_KEY_SIZE = 128; // in bits
    private static final int AES_COUNTER_SIZE = 16; // in bytes

    // Hardcoded for now activation data
    private static final String SERIAL_NUMBER = "504394802433901126813236";
    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String IV = "1A550F4B413D0E971C28293F9183EA8A";
    private static final String PASSWORD =  "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";

    private static final String SURF_PUBLIC_KEY = "B81F0E0E07416DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A";
    private static final String MULTISIG_ADDR = "A11F0E0E07416DAB6C320ECC6BF3DBA48A70121C5251CC31B1D8F8A1B36E0F2F";

    private CardCoinManagerApi cardCoinManagerNfcApi;
    private CardActivationApi cardActivationApi;
    private CardCryptoApi cardCryptoApi;
    private CardKeyChainApi cardKeyChainApi;
    private RecoveryDataApi recoveryDataApi;
    private NfcApi nfcApi;

    private SecureRandom sr = new SecureRandom();
    private KeyGenerator kg;
    private SecretKey key;
    private byte[] counter = new byte[AES_COUNTER_SIZE];

    Button buttonGetMaxPinTries;
    Button buttonActivateCard;
    Button buttonPk;
    Button buttonSign;
    Button buttonTryKeychain;
    Button buttonAddRecoveryData;
    Button buttonGetRecoveryData;
    Button buttonCheckIfNfcSupported;
    Button buttonCheckIfNfcEnabled;
    Button buttonOpenNfcSettings;
    Button buttonGetKeyChainDataAboutAllKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        addListenerOnGetMaxPinTriesButton();
        addListenerOnActivateCardButton();
        addListenerOnGetPkButton();
        addListenerOnSignButton();
        addListenerOnTryKeychainButton();
        addListenerOnAddRecoveryDataButton();
        addListenerOnGetRecoveryDataButton();
        addListenerOnCheckIfNfcSupportedButton();
        addListenerOnCheckIfNfcEnabledButton();
        addListenerOnOpenNfcSettingsButton();
        addListenerOnGetKeyChainDataAboutAllKeysButton();
        try {
            Context activity = getApplicationContext();
            NfcApduRunner nfcApduRunner = NfcApduRunner.getInstance(activity);
            cardCoinManagerNfcApi = new CardCoinManagerApi(activity,  nfcApduRunner);
            cardActivationApi = new CardActivationApi(activity,  nfcApduRunner);
            cardCryptoApi =  new CardCryptoApi(activity,  nfcApduRunner);
            cardKeyChainApi = new CardKeyChainApi(activity,  nfcApduRunner);
            recoveryDataApi = new RecoveryDataApi(activity,  nfcApduRunner);
            nfcApi = new NfcApi(activity);

            kg = KeyGenerator.getInstance("AES");
            kg.init(AES_KEY_SIZE);
            key = kg.generateKey();
        }
        catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            if (cardActivationApi.setCardTag(intent)) {
                Toast.makeText(this, "NFC hardware touched!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Log.e("TAG", "Error happened : " + e.getMessage());
        }
    }

    public void addListenerOnGetKeyChainDataAboutAllKeysButton() {

        buttonGetKeyChainDataAboutAllKeys = findViewById(R.id.getKeyChainDataAboutAllKeys);

        buttonGetKeyChainDataAboutAllKeys.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String status = cardCryptoApi.createKeyForHmacAndGetJson(PASSWORD, COMMON_SECRET, SERIAL_NUMBER);
                    Log.d("TAG", "status : " + status);

                    String response = cardKeyChainApi.resetKeyChainAndGetJson();
                    Log.d("TAG", "resetKeyChain response : " + response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    String keyInHex = "001122334455";
                    response = cardKeyChainApi.addKeyIntoKeyChainAndGetJson(keyInHex);
                    Log.d("TAG", "addKeyIntoKeyChain response : " + response);

                    String keyHmac = extractMessage(response);
                    Log.d("TAG", "keyHmac : " + response);

                    keyInHex = "667788";
                    response = cardKeyChainApi.addKeyIntoKeyChainAndGetJson(keyInHex);
                    Log.d("TAG", "addKeyIntoKeyChain response : " + response);

                    keyHmac = extractMessage(response);
                    Log.d("TAG", "keyHmac : " + response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    response = cardKeyChainApi.getIndexAndLenOfKeyInKeyChainAndGetJson(keyHmac);
                    Log.d("TAG", "getIndexAndLenOfKeyInKeyChain : " + response);

                    response = cardKeyChainApi.getKeyChainDataAboutAllKeysAndGetJson();
                    Log.d("TAG", "getKeyChainDataAboutAllKeys : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });

    }

    public void addListenerOnOpenNfcSettingsButton() {

        buttonOpenNfcSettings = findViewById(R.id.openNfcSettings);

        buttonOpenNfcSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String response = nfcApi.openNfcSettingsAndGetJson();
                    Log.d("TAG", "openNfcSettings response : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });
    }

    public void addListenerOnCheckIfNfcEnabledButton() {

        buttonCheckIfNfcEnabled = findViewById(R.id.checkIfNfcEnabled);

        buttonCheckIfNfcEnabled.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String response = nfcApi.checkIfNfcEnabledAndGetJson();
                    Log.d("TAG", "checkIfNfcEnabled response : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });
    }

    public void addListenerOnCheckIfNfcSupportedButton() {

        buttonCheckIfNfcSupported = findViewById(R.id.checkIfNfcSupported);

        buttonCheckIfNfcSupported.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String response = nfcApi.checkIfNfcSupportedAndGetJson();
                    Log.d("TAG", "checkIfNfcSupported response : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });
    }

    public void addListenerOnAddRecoveryDataButton() {

        buttonAddRecoveryData = findViewById(R.id.addRecoveryData);

        buttonAddRecoveryData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String response = recoveryDataApi.resetRecoveryDataAndGetJson();
                    Log.d("TAG", "resetRecoveryData response : " + response);

                    response = recoveryDataApi.isRecoveryDataSetAndGetJson();
                    Log.d("TAG", "isRecoveryDataSet response : " + response);

                    JSONObject recoveryData = new JSONObject();
                    recoveryData.put("surfPublicKey", SURF_PUBLIC_KEY);
                    recoveryData.put("multisigAddress", MULTISIG_ADDR);
                    recoveryData.put("p1", PASSWORD);
                    recoveryData.put("cs", COMMON_SECRET);

                    Log.d("TAG", "recoveryData : " + recoveryData.toString());

                    byte[] recoveryDataBytes = recoveryData.toString().getBytes(StandardCharsets.UTF_8);
                    Log.d("TAG", "recoveryDataBytes length : " + recoveryDataBytes.length);

                    Cipher aesCtr = Cipher.getInstance("AES/CTR/NoPadding");
                    sr.nextBytes(counter);
                    aesCtr.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(counter));

                    byte[] encryptedRecoveryDataBytes = aesCtr.doFinal(recoveryDataBytes);

                    String encryptedRecoveryDataHex = ByteArrayUtil.getInstance().hex(encryptedRecoveryDataBytes);

                    Log.d("TAG", "encryptedRecoveryDataHex : " + encryptedRecoveryDataHex);

                    response = recoveryDataApi.addRecoveryDataAndGetJson(encryptedRecoveryDataHex );
                    Log.d("TAG", "addRecoveryData response : " + response);

                    response = recoveryDataApi.isRecoveryDataSetAndGetJson();
                    Log.d("TAG", "isRecoveryDataSet response : " + response);

                    response = recoveryDataApi.getRecoveryDataLenAndGetJson();
                    Log.d("TAG", "getRecoveryDataLen response : " + response);

                    response = recoveryDataApi.getRecoveryDataHashAndGetJson();
                    Log.d("TAG", "getRecoveryDataHash response : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });

    }

    public void addListenerOnGetRecoveryDataButton() {

        buttonGetRecoveryData = findViewById(R.id.getRecoveryData);

        buttonGetRecoveryData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {

                    String response = recoveryDataApi.isRecoveryDataSetAndGetJson();
                    Log.d("TAG", "isRecoveryDataSet response : " + response);

                    String status = extractMessage(response);
                    if (status.equals("true")) {
                        response = recoveryDataApi.getRecoveryDataAndGetJson();
                        Log.d("TAG", "getRecoveryData response : " + response);
                        String encryptedRecoveryDataHex = extractMessage(response);

                        Log.d("TAG", "encryptedRecoveryDataHex : " + encryptedRecoveryDataHex);

                        byte[] encryptedRecoveryDataBytes = ByteArrayUtil.getInstance().bytes(encryptedRecoveryDataHex);
                        Log.d("TAG", "encryptedRecoveryDataBytes length : " + encryptedRecoveryDataBytes.length);

                        Cipher aesCtr = Cipher.getInstance("AES/CTR/NoPadding");
                        aesCtr.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(counter));

                        byte[] recoveryDataBytes = aesCtr.doFinal(encryptedRecoveryDataBytes);

                        String recoveryData = new String(recoveryDataBytes, StandardCharsets.UTF_8);

                        Log.d("TAG", "Got recoveryData from card : " + recoveryData);
                    }
                    else {
                        Log.d("TAG", "Recovery data is no set yet.");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });

    }

    public void addListenerOnTryKeychainButton() {

        buttonTryKeychain = findViewById(R.id.testKeychain);

        buttonTryKeychain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String status = cardCryptoApi.createKeyForHmacAndGetJson(PASSWORD, COMMON_SECRET, SERIAL_NUMBER);
                    Log.d("TAG", "status : " + status);

                    String response = cardKeyChainApi.resetKeyChainAndGetJson();
                    Log.d("TAG", "resetKeyChain response : " + response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    String keyInHex = StringHelper.getInstance().randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN);
                    response = cardKeyChainApi.addKeyIntoKeyChainAndGetJson(keyInHex);
                    Log.d("TAG", "addKeyIntoKeyChain response : " + response);
                    Log.d("TAG", "addKeyIntoKeyChain response : " + keyInHex .length());

                    String keyHmac = extractMessage(response);
                    Log.d("TAG", "keyHmac : " + response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(keyHmac);
                    String keyFromCard = extractMessage(response);
                    Log.d("TAG", "keyFromCard : " + response);

                    if (!keyInHex.toLowerCase().equals(keyFromCard.toLowerCase())) {
                        System.out.println(keyInHex);
                        System.out.println(keyFromCard);;
                        throw  new Exception("Bad key from card : " + keyFromCard);
                    }

                    String newKeyInHex =  StringHelper.getInstance().randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN);
                    response = cardKeyChainApi.changeKeyInKeyChainAndGetJson(newKeyInHex, keyHmac);
                    Log.d("TAG", "changeKeyInKeyChain response : " + response);
                    String newKeyHmac = extractMessage(response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(newKeyHmac);
                    String newKeyFromCard = extractMessage(response);
                    Log.d("TAG", "keyFromCard : " + response);

                    if (!newKeyInHex.toLowerCase().equals(newKeyFromCard.toLowerCase())) {
                        throw  new Exception("Bad key from card : " + newKeyFromCard);
                    }

                    response = cardKeyChainApi.deleteKeyFromKeyChainAndGetJson(newKeyHmac);
                    Log.d("TAG", "deleteKeyFromKeyChain response : " + response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    JSONObject jObject = new JSONObject(response);
                    Integer num  =  Integer.parseInt(jObject.getString(NUMBER_OF_KEYS_FIELD));

                    if (num != 0) {
                        throw  new Exception("Bad number of keys : " + num);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });

    }

    public void addListenerOnGetMaxPinTriesButton() {

        buttonGetMaxPinTries = findViewById(R.id.getMaxPinTries);

        buttonGetMaxPinTries.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String response = cardCoinManagerNfcApi.getAvailableMemoryAndGetJson();//.getMaxPinTriesAndGetJson();
                    Log.d("TAG", "Card response : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });

    }

    public void addListenerOnSignButton() {

        buttonSign = findViewById(R.id.sign);

        buttonSign.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String status = cardCryptoApi.createKeyForHmacAndGetJson(PASSWORD, COMMON_SECRET, SERIAL_NUMBER);
                    Log.d("TAG", "status : " + status);

                    String hdIndex = "234";
                    String msg = "000011";
                  //  String response = cardCryptoApi.verifyPinAndGetJson(DEFAULT_PIN);
                   // response = cardCryptoApi.signAndGetJson(msg, hdIndex);
                   // String response = cardCryptoApi.verifyPinAndSignForDefaultHdPathAndGetJson(msg, DEFAULT_PIN);
                    String response = cardCryptoApi.verifyPinAndSignAndGetJson(msg, hdIndex, DEFAULT_PIN);
                    Log.d("TAG", "Card response (ed25519 signature) : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });

    }

    public void addListenerOnGetPkButton() {

        buttonPk = findViewById(R.id.getPk);

        buttonPk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String hdIndex = "1";
                    String response = cardCryptoApi.getTonAppletStateAndGetJson();
                    //String response = cardCryptoApi.getPublicKeyAndGetJson(hdIndex);
                    Log.d("TAG", "Card response (ed25519 public key) : " + response);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });

    }


    public void addListenerOnActivateCardButton() {

        buttonActivateCard = findViewById(R.id.activateCard);

        buttonActivateCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String seedStatus = extractMessage(cardCoinManagerNfcApi.getRootKeyStatusAndGetJson());
                    if (seedStatus.equals(NOT_GENERATED_MSG)) {
                        cardCoinManagerNfcApi.generateSeedAndGetJson(DEFAULT_PIN);
                    }

                    String appletState = extractMessage(cardActivationApi.getTonAppletStateAndGetJson());

                    if (!appletState.equals(WAITE_AUTHORIZATION_MSG)) {
                        throw new Exception("Incorret applet state : " + appletState);
                    }

                    String hashOfEncryptedCommonSecret = extractMessage(cardActivationApi.getHashOfEncryptedCommonSecretAndGetJson());
                    String hashOfEncryptedPassword = extractMessage(cardActivationApi.getHashOfEncryptedPasswordAndGetJson());

                    Log.d("TAG", "hashOfEncryptedCommonSecret : " + hashOfEncryptedCommonSecret);
                    Log.d("TAG", "hashOfEncryptedPassword : " + hashOfEncryptedPassword);

                    String newPin = "7777";

                    appletState = extractMessage(cardActivationApi.turnOnWalletAndGetJson(newPin, PASSWORD, COMMON_SECRET, IV));

                    Log.d("TAG", "Card response (state) : " + appletState);

                    if (!appletState.equals(PERSONALIZED_STATE_MSG)) {
                        throw new Exception("Incorrect applet state after activation : " + appletState);
                    }
                }

                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }

        });
    }

    private String extractMessage(String jsonStr) throws JSONException {
        JSONObject jObject = new JSONObject(jsonStr);
        return jObject.getString(MESSAGE_FIELD);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up buttonGetMaxPinTries, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
