package com.tonnfccard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.utils.ByteArrayUtil;

import static com.tonnfccard.CardActivationApi.ECS_HASH_FIELD;
import static com.tonnfccard.CardActivationApi.EP_HASH_FIELD;
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
import org.riversun.promise.Promise;

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
    private static final String PASSWORD  = "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";

    private static final String SURF_PUBLIC_KEY = "B81F0E0E07416DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A";
    private static final String MULTISIG_ADDR = "A11F0E0E07416DAB6C320ECC6BF3DBA48A70121C5251CC31B1D8F8A1B36E0F2F";

    /**
     *
     * {"CS":"855D821CD4678B002E3D6E69F07947D3A8C608245E4381C4324FC532C1915484",
     * "P1":"3149765A6CFD52E3CFCFC8F77EFB1BBFE78BCA5F2378FC46A9C9EA5E9CE78B30060CE6ECF6372F9BC15B35F8B5B773DB577048EF848026609959D5B09FE6C5E52B2F61F4168FEE9E49F3C6A41CF2635D7F5862F9F403F1583A06B0B54BD652045A5E73EBF40CCE312E360891604CF77D7A7F9DF05325056B8212BB2297C04A1E",
     * "ECS":"6254AA7054F285CD08E083459118E6D892553B04F465549178215A05A288A8D9",
     * "H1":"F1B0B07121C4D20A0855336EDBBBF90E28C92521B87DCB118742FA19F652617A",
     * "H2":"1F91610DC5588B746BC19D5083A3DD169D98FD08C6A837F2F67D7D636701C4BD",
     * "SN":"647250988796071878800855",
     * "H3":"C9E197ECB19C6930F2914A8B9F37BFB7B328F9CA98963447AAD4F3AFDFC8FBA7",
     * "IV":"2FB8418BC77EC9BDB7CB706165339505",
     * "B1":"EC208FC96DCE8E99CA5ACE7B3EB129ABEDDD3418EA90E7DD86694D036B3ABF40F28E2A29F256F1B5BB7E3EDA59E18C4FA75588AD4C66FD82BDD68297C91CC47FD255B0696A98559AA91F18F3450290DE78CA2530D5B31D28CB0152C5DB2ECC00B5D86BCE70FB7B53FE04E66C34A9CCA43F7C1639D0780E555177834C740B0C1B"},
     */

    /**
     * {"CS":"7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4",
     * "P1":"F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE",
     * "ECS":"71E872C73979904C17722CB2A5FA6B7A107DBA38924338F739A1C0E96D74BC33",
     * "H1":"6F83BBEF900614F609DDBBB0CC014CC1ED19A30A40E5E171C5734901B8047705",
     * "H2":"112716D2053C2828DC265B5DF14F85F203F8350DCB5774950901F3136108FA2C",
     * "SN":"504394802433901126813236",
     * "H3":"71106ED2161D12E5E59FA7FF298930F0F4BB398171A712CB26D947A0DAF5F0EF",
     * "IV":"1A550F4B413D0E971C28293F9183EA8A",
     * "B1":"7BF6D157F017189AE9904959878A851376BE01127582D675004790CFC194E6AC273D85F55B7050B08FC48F3142AA68974B9765D0799BB5804F6FD4A4BF38686D8E1AE548E60603D32DD85C57DADB146CDE4CFD30D0321DCD5A2B8010760E70A93E429FBC2A458FE84B63B35DB9902893E2C81CD53A2AA20E268A57D188F93D69"}
     */


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
    Button buttonGetRemainingPinTries;
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
    Button buttonGetSerialNumber;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "Current thread 1 : " + Thread.currentThread().getName());
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
        addListenerOnActivateCardButton();
        addListenerOnGetRemainingPinTriesButton();
        addListenerOnGetSerialNumberButton();
        textView = findViewById(R.id.textView1);
        try {
            NfcApduRunner nfcApduRunner = NfcApduRunner.getInstance(MainActivity.this);
            cardCoinManagerNfcApi = new CardCoinManagerApi(MainActivity.this,  nfcApduRunner);
            cardActivationApi = new CardActivationApi(MainActivity.this,  nfcApduRunner);
            cardCryptoApi =  new CardCryptoApi(MainActivity.this,  nfcApduRunner);
            cardKeyChainApi = new CardKeyChainApi(MainActivity.this,  nfcApduRunner);
            recoveryDataApi = new RecoveryDataApi(MainActivity.this,  nfcApduRunner);
            nfcApi = new NfcApi(MainActivity.this);
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

    public void addListenerOnGetRemainingPinTriesButton() {

        buttonGetRemainingPinTries = findViewById(R.id.getRemainingPinTries);

        buttonGetRemainingPinTries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                cardCoinManagerNfcApi.getRemainingPinTries(new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnGetSerialNumberButton() {

        buttonGetSerialNumber = findViewById(R.id.getSerialNumber);

        buttonGetSerialNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                 cardActivationApi.getSerialNumber(new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnGetKeyChainDataAboutAllKeysButton() {

        buttonGetKeyChainDataAboutAllKeys = findViewById(R.id.getKeyChainDataAboutAllKeys);

        buttonGetKeyChainDataAboutAllKeys.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                try {
                    Promise.resolve("start")
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    cardKeyChainApi.resetKeyChain(new NfcCallback((result) -> {
                                        textView.setText(String.valueOf(result));
                                        System.out.println(result);
                                        action.resolve(result);
                                    }, (error) -> {
                                        System.out.println(error);
                                        action.reject(error);
                                    }), showDialog);
                                    ;
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    try {
                                        Thread.sleep(5000);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("resetKeyChain result = " + data);
                                    cardKeyChainApi.getKeyChainInfo(new NfcCallback((result) -> {
                                        System.out.println(result);
                                        textView.append("\n");
                                        textView.append(String.valueOf(result));
                                        action.resolve(result);
                                    }, (e) -> {
                                        System.out.println(e);
                                        action.reject(e);
                                    }), showDialog);
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    try {
                                        Thread.sleep(5000);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("getKeyChainInfo result = " + data);
                                    String keyInHex = "001122334455";
                                    cardKeyChainApi.addKeyIntoKeyChain(keyInHex, new NfcCallback((result) -> {
                                        System.out.println(result);
                                        textView.append("\n");
                                        textView.append(String.valueOf(result));
                                        action.resolve(result);
                                    }, (e) -> {
                                        System.out.println(e);
                                        action.reject(e);
                                    }), showDialog);
                                    ;
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    try {
                                        Thread.sleep(5000);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("addKeyIntoKeyChain result = " + data);
                                    String keyInHex = "667788";
                                    cardKeyChainApi.addKeyIntoKeyChain(keyInHex, new NfcCallback((result) -> {
                                        System.out.println(result);
                                        textView.append("\n");
                                        textView.append(String.valueOf(result));
                                        action.resolve(result);
                                    }, (e) -> {
                                        System.out.println(e);
                                        action.reject(e);
                                    }), showDialog);
                                    ;
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    try {
                                        Thread.sleep(10000);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("addKeyIntoKeyChain result #2 = " + data);
                                    cardKeyChainApi.getKeyChainInfo(new NfcCallback((result) -> {
                                        System.out.println(result);
                                        textView.append("\n");
                                        textView.append(String.valueOf(result));
                                        action.resolve(result);
                                    }, (e) -> {
                                        System.out.println(e);
                                        action.reject(e);
                                    }), showDialog);
                                    ;;
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    try {
                                        Thread.sleep(10000);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("getKeyChainInfo result = " + data);
                                    cardKeyChainApi.getKeyChainDataAboutAllKeys(new NfcCallback((result) -> {
                                        System.out.println(result);
                                        textView.append("\n");
                                        textView.append(String.valueOf(result));
                                        action.resolve(result);
                                    }, (e) -> {
                                        System.out.println(e);
                                        action.reject(e);
                                    }), showDialog);
                                    ;
                                });
                            }))
                            .start();

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
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String response = recoveryDataApi.resetRecoveryDataAndGetJson();
                            Log.d("TAG", "resetRecoveryData response : " + response);

                            Thread.sleep(5000);

                            response = recoveryDataApi.isRecoveryDataSetAndGetJson();
                            Log.d("TAG", "isRecoveryDataSet response : " + response);

                            Thread.sleep(5000);

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

                            Thread.sleep(5000);

                            response = recoveryDataApi.isRecoveryDataSetAndGetJson();
                            Log.d("TAG", "isRecoveryDataSet response : " + response);

                            Thread.sleep(5000);

                            response = recoveryDataApi.getRecoveryDataLenAndGetJson();
                            Log.d("TAG", "getRecoveryDataLen response : " + response);

                            Thread.sleep(5000);

                            response = recoveryDataApi.getRecoveryDataHashAndGetJson();
                            Log.d("TAG", "getRecoveryDataHash response : " + response);

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("TAG", "Error happened : " + e.getMessage());
                        }
                    }
                }).start();

            }

        });

    }

    public void addListenerOnGetRecoveryDataButton() {

        buttonGetRecoveryData = findViewById(R.id.getRecoveryData);

        buttonGetRecoveryData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new Thread(new Runnable() {
                    public void run() {
                        try {

                            String response = recoveryDataApi.isRecoveryDataSetAndGetJson();
                            Log.d("TAG", "isRecoveryDataSet response : " + response);

                            Thread.sleep(5000);

                            String status = extractMessage(response, MESSAGE_FIELD);
                            if (status.equals("true")) {
                                response = recoveryDataApi.getRecoveryDataAndGetJson();
                                Log.d("TAG", "getRecoveryData response : " + response);
                                String encryptedRecoveryDataHex = extractMessage(response, MESSAGE_FIELD);

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
                }).start();
            }

        });

    }

    public void addListenerOnTryKeychainButton() {

        buttonTryKeychain = findViewById(R.id.testKeychain);

        buttonTryKeychain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String status = cardCryptoApi.createKeyForHmacAndGetJson(PASSWORD, COMMON_SECRET, SERIAL_NUMBER);
                            Log.d("TAG", "status : " + status);

                            String response = cardKeyChainApi.resetKeyChainAndGetJson();
                            Log.d("TAG", "resetKeyChain response : " + response);

                            Thread.sleep(5000);

                            response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                            Log.d("TAG", "getKeyChainInfo response : " + response);

                            Thread.sleep(5000);

                            String keyInHex = StringHelper.getInstance().randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN);
                            response = cardKeyChainApi.addKeyIntoKeyChainAndGetJson(keyInHex);
                            Log.d("TAG", "addKeyIntoKeyChain response : " + response);
                            Log.d("TAG", "addKeyIntoKeyChain response : " + keyInHex .length());

                            Thread.sleep(5000);

                            String keyHmac = extractMessage(response, MESSAGE_FIELD);
                            Log.d("TAG", "keyHmac : " + response);

                            response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                            Log.d("TAG", "getKeyChainInfo response : " + response);

                            Thread.sleep(5000);

                            response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(keyHmac);
                            String keyFromCard = extractMessage(response, MESSAGE_FIELD);
                            Log.d("TAG", "keyFromCard : " + response);

                            Thread.sleep(5000);

                            if (!keyInHex.toLowerCase().equals(keyFromCard.toLowerCase())) {
                                System.out.println(keyInHex);
                                System.out.println(keyFromCard);;
                                throw  new Exception("Bad key from card : " + keyFromCard);
                            }

                            String newKeyInHex =  StringHelper.getInstance().randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN);
                            response = cardKeyChainApi.changeKeyInKeyChainAndGetJson(newKeyInHex, keyHmac);
                            Log.d("TAG", "changeKeyInKeyChain response : " + response);
                            String newKeyHmac = extractMessage(response, MESSAGE_FIELD);

                            Thread.sleep(5000);

                            response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                            Log.d("TAG", "getKeyChainInfo response : " + response);

                            Thread.sleep(5000);

                            response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(newKeyHmac);
                            String newKeyFromCard = extractMessage(response, MESSAGE_FIELD);
                            Log.d("TAG", "keyFromCard : " + response);

                            Thread.sleep(5000);

                            if (!newKeyInHex.toLowerCase().equals(newKeyFromCard.toLowerCase())) {
                                throw  new Exception("Bad key from card : " + newKeyFromCard);
                            }

                            response = cardKeyChainApi.deleteKeyFromKeyChainAndGetJson(newKeyHmac);
                            Log.d("TAG", "deleteKeyFromKeyChain response : " + response);

                            Thread.sleep(5000);

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
                }).start();
            }

        });

    }

    public void addListenerOnGetMaxPinTriesButton() {

        buttonGetMaxPinTries = findViewById(R.id.getMaxPinTries);

        buttonGetMaxPinTries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                cardCoinManagerNfcApi.getMaxPinTries(new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
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
                    boolean showDialog = true;
                    String hdIndex = "234";
                    String msg = "000011";
                    cardCryptoApi.verifyPinAndSign(msg, hdIndex, DEFAULT_PIN, new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
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
                boolean showDialog = true;
                String hdIndex = "1";
                cardCryptoApi.getPublicKey(hdIndex, new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });

    }


    public void addListenerOnActivateCardButton() {
        buttonActivateCard = findViewById(R.id.activateCard);
        buttonActivateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String seedStatus = extractMessage(cardCoinManagerNfcApi.getRootKeyStatusAndGetJson(), MESSAGE_FIELD);
                            if (seedStatus.equals(NOT_GENERATED_MSG)) {
                                cardCoinManagerNfcApi.generateSeedAndGetJson(DEFAULT_PIN);
                            }

                            String appletState = extractMessage(cardActivationApi.getTonAppletStateAndGetJson(), MESSAGE_FIELD);
                            if (!appletState.equals(WAITE_AUTHORIZATION_MSG)) {
                                throw new Exception("Incorrect applet state : " + appletState);
                            }

                            String hashesJsonStr = cardActivationApi.getHashesAndGetJson();
                            String hashOfEncryptedCommonSecret = extractMessage(hashesJsonStr, ECS_HASH_FIELD);
                            String hashOfEncryptedPassword = extractMessage(hashesJsonStr, EP_HASH_FIELD);

                            Log.d("TAG", "hashOfEncryptedCommonSecret : " + hashOfEncryptedCommonSecret);
                            Log.d("TAG", "hashOfEncryptedPassword : " + hashOfEncryptedPassword);

                            String newPin = "5555";
                            appletState = extractMessage(cardActivationApi.turnOnWalletAndGetJson(newPin, PASSWORD, COMMON_SECRET, IV),  MESSAGE_FIELD);
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
                }).start();
            }
        });
    }

    private String extractMessage(String jsonStr, String field) throws JSONException {
        JSONObject jObject = new JSONObject(jsonStr);
        return jObject.getString(field);
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
