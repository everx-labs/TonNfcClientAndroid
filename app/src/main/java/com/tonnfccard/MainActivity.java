package com.tonnfccard;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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

import static com.tonnfccard.TonWalletConstants.ECS_HASH_FIELD;
import static com.tonnfccard.TonWalletConstants.EP_HASH_FIELD;
import static com.tonnfccard.TonWalletConstants.MAX_KEY_SIZE_IN_KEYCHAIN;
import static com.tonnfccard.TonWalletConstants.MESSAGE_FIELD;
import static com.tonnfccard.TonWalletConstants.NOT_GENERATED_MSG;
import static com.tonnfccard.TonWalletConstants.NUMBER_OF_KEYS_FIELD;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.TonWalletConstants.WAITE_AUTHENTICATION_MSG;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

import org.json.JSONException;
import org.json.JSONObject;
import org.riversun.promise.Action;
import org.riversun.promise.Promise;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private static final String DEFAULT_PIN = "5555";

    // AES parameters
    private static final int AES_KEY_SIZE = 128; // in bits
    private static final int AES_COUNTER_SIZE = 16; // in bytes

   /* "SN": "748452894231855071597801",
            "data": {
        "P1": "e2ec17cc0029ca458b1aa29b8f50ed457e20b70b480da8573306d42b53b6a23ea9199beb9d3cb006547b2b49cb03eaf432b11c72c5fbe924741001c18d4608cd1bbca874ab35b2118e8f279626533bb603aa752360a8f1348d6e3b5990fd620beb755c2c3089079e1589eda7d897d57fccd1fc59206e00baa1d1eb8de8d8c446",
                "IV": "e8d40dde93378473dc714cc6a8710a83",
                "CS": "57bc53323d5e09a987d5d358e5a7f2c97a0175df0c39670055535c5976feeaf6",
                "ECS": "21de33142f089aaf15a749c1f2ff12a3f730bc430a91df235cf8ab53c8e40afe",
                "H1": "a578eebec8db78efc4386ec28b85ee556a83f6a754323e43f6c5f5301a660216",
                "B1": "9a2c49b03976cead49f7ef55b7db3e88c3abb9dab44ca6ccffbecb46e859e963303ce1dd3d3f5c6633dfb55904052fd1da9e7ff2a5a9390398be94a6ee914a3e1c7a40b2d0e4e2b03bb0556c177ab275b0b1b27b2927536faeda7b8d65d3dde8a3b8679759dd4881043fb08203bcdb8fa1d05acb5753b0181c3cca771721496d",
                "H2": "59d5f576e4b69e0703c832a450886f15103f01c3f6ab0599da707b7a2406db12",
                "H3": "954101ab6a143be0dd43bbd84b97cab24c354ca427551d155c41bb5da8b37fa3"*/


    // Hardcoded for now activation data
    private static final String SERIAL_NUMBER = "504394802433901126813236";
    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String IV = "1A550F4B413D0E971C28293F9183EA8A";
    private static final String PASSWORD  = "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";

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
    Button buttonCreateKeyForHmac;
    Button buttonGetAllSerialNumbers;
    Button buttonCheckSnANdGetPkForDefaultHdPath;
    Button buttonCheckSnANdGetPk;
    Button buttonCheckSnANdSignForDefaultHdPath;
    Button buttonCheckSnANdSign;
    Button buttonCheckSnANdSignForDefaultHdPath2;
    Button buttonCheckSnANdSign2;
    Button buttonVerifyPin;
    Button buttonGetHashes;
    Button buttonSignTwice;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Log.d("TAG", "Current thread 1 : " + Thread.currentThread().getName());
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
        addListenerOnGetAllSerialNumbersButton();
        addListenerOnCreateKeyForHmacButton();
        addListenerOnCheckSnAndGetPkForDefaultHdPathButton();
        addListenerOnCheckSnAndGetPkButton();
        addListenerOnCheckSnAndSignForDefaultHdPathButton();
        addListenerOnCheckSnAndSignButton();
        addListenerOnCheckSnAndSignForDefaultHdPathButton2();
        addListenerOnCheckSnAndSignButton2();
        addListenerOnVerifyPinButton();
        addListenerOnGetHashesButton();
        addListenerOnSignTwiceButton();
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

    public void addListenerOnVerifyPinButton() {
        buttonVerifyPin = findViewById(R.id.verifyPin);
        buttonVerifyPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String pin = "5555";
                cardCryptoApi.verifyPin(pin, new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnSignTwiceButton() {
        buttonSignTwice = findViewById(R.id.signTwice);
        buttonSignTwice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String sn = "504394802433901126813236";
                String data = "11";
                String hdIndex = "2";
                String pin = "5555";
                System.out.println("HERE1");
                cardCryptoApi.checkSerialNumberAndVerifyPinAndSign(sn, data, hdIndex, pin,  new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
                System.out.println("HERE2");
                cardCryptoApi.checkSerialNumberAndVerifyPinAndSign(sn, data, hdIndex, pin,  new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
                System.out.println("HERE3");
            }
        });
    }

    public void addListenerOnGetHashesButton() {
        buttonGetHashes = findViewById(R.id.getHashes);
        buttonGetHashes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                cardActivationApi.generateSeedAndGetHashes(new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnCheckSnAndGetPkForDefaultHdPathButton() {
        buttonCheckSnANdGetPkForDefaultHdPath = findViewById(R.id.chrckSNAndGetpk);
        buttonCheckSnANdGetPkForDefaultHdPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String sn = "929526125066377952749605";
                cardCryptoApi.checkSerialNumberAndGetPublicKeyForDefaultPath(sn, new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnCheckSnAndGetPkButton() {
        buttonCheckSnANdGetPk = findViewById(R.id.chrckSNAndGetpk2);
        buttonCheckSnANdGetPk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String sn = "929526125066377952749605";
                String hdIndex = "1";
                cardCryptoApi.checkSerialNumberAndGetPublicKey(sn,hdIndex,  new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnCheckSnAndSignForDefaultHdPathButton() {
        buttonCheckSnANdSignForDefaultHdPath = findViewById(R.id.checkSnAndSign);
        buttonCheckSnANdSignForDefaultHdPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String sn = "504394802433901126813236";
                String data = "11";
                String pin = "5555";
                cardCryptoApi.checkSerialNumberAndVerifyPinAndSignForDefaultHdPath(sn, data, pin,  new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnCheckSnAndSignButton() {
        buttonCheckSnANdSign = findViewById(R.id.checkSnAndSign2);
        buttonCheckSnANdSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String sn = "504394802433901126813236";
                String data = "11";
                String hdIndex = "2";
                String pin = "5555";
                cardCryptoApi.checkSerialNumberAndVerifyPinAndSign(sn, data, hdIndex, pin,  new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnCheckSnAndSignForDefaultHdPathButton2() {
        buttonCheckSnANdSignForDefaultHdPath2 = findViewById(R.id.button7);
        buttonCheckSnANdSignForDefaultHdPath2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String sn = "504394802433901126813236";
                String data = "11";
                cardCryptoApi.checkSerialNumberAndSignForDefaultHdPath(sn, data, new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }

    public void addListenerOnCheckSnAndSignButton2() {
        buttonCheckSnANdSign2 = findViewById(R.id.button8);
        buttonCheckSnANdSign2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                String sn = "504394802433901126813236";
                String data = "11";
                String hdIndex = "2";
                cardCryptoApi.checkSerialNumberAndSign(sn, data, hdIndex, new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println), showDialog);
            }
        });
    }


    public void addListenerOnGetAllSerialNumbersButton() {

        buttonGetAllSerialNumbers = findViewById(R.id.getAllSerialNumbers);

        buttonGetAllSerialNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                cardActivationApi.getAllSerialNumbers(new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println));
            }
        });
    }

    public void addListenerOnCreateKeyForHmacButton() {

        buttonCreateKeyForHmac = findViewById(R.id.createKeyForHmac);

        buttonCreateKeyForHmac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showDialog = true;
                cardActivationApi.createKeyForHmac(PASSWORD, COMMON_SECRET, SERIAL_NUMBER, new NfcCallback((result) -> textView.setText(String.valueOf(result)), System.out::println));
            }
        });
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

    public NfcCallback createCallback(Action action){
        return new NfcCallback((result) -> {
            System.out.println(result);
            textView.append("\n");
            textView.append(String.valueOf(result));
            action.resolve(result);
        }, (error) -> {
            System.out.println(error);
            action.reject(error);
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
                                    cardKeyChainApi.resetKeyChain(createCallback(action), showDialog);
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    System.out.println("resetKeyChain result = " + data);
                                    cardKeyChainApi.getKeyChainInfo(createCallback(action), showDialog);
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    System.out.println("getKeyChainInfo result = " + data);
                                    String keyInHex = "001122334455";
                                    cardKeyChainApi.addKeyIntoKeyChain(keyInHex, createCallback(action), showDialog);
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    System.out.println("addKeyIntoKeyChain result = " + data);
                                    String keyInHex = "667788";
                                    cardKeyChainApi.addKeyIntoKeyChain(keyInHex, createCallback(action), showDialog);
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    System.out.println("addKeyIntoKeyChain result #2 = " + data);
                                    cardKeyChainApi.getKeyChainInfo(createCallback(action), showDialog);
                                });
                            }))
                            .then(new Promise((action, data) -> {
                                runOnUiThread(() -> {
                                    System.out.println("getKeyChainInfo result = " + data);
                                    cardKeyChainApi.getKeyChainDataAboutAllKeys(createCallback(action), showDialog);
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

                            String status = extractMessage(response, MESSAGE_FIELD);
                            if (status.equals(TRUE_MSG)) {
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
                                Log.d("TAG", "Recovery data is not set yet.");
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

                            response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                            Log.d("TAG", "getKeyChainInfo response : " + response);

                            String keyInHex = StringHelper.getInstance().randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN);
                            Log.d("TAG", "key to add :  : " + keyInHex .length());
                            response = cardKeyChainApi.addKeyIntoKeyChainAndGetJson(keyInHex);
                            Log.d("TAG", "addKeyIntoKeyChain response : " + response);

                            String keyHmac = extractMessage(response, MESSAGE_FIELD);
                            Log.d("TAG", "keyHmac : " + response);

                            response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                            Log.d("TAG", "getKeyChainInfo response : " + response);

                            response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(keyHmac);
                            String keyFromCard = extractMessage(response, MESSAGE_FIELD);
                            Log.d("TAG", "keyFromCard : " + response);


                            if (!keyInHex.toLowerCase().equals(keyFromCard.toLowerCase())) {
                                System.out.println(keyInHex);
                                System.out.println(keyFromCard);;
                                throw  new Exception("Bad key from card : " + keyFromCard);
                            }

                            String newKeyInHex =  StringHelper.getInstance().randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN);
                            Log.d("TAG", "new key :  : " +  newKeyInHex.length());
                            response = cardKeyChainApi.changeKeyInKeyChainAndGetJson(newKeyInHex, keyHmac);
                            Log.d("TAG", "changeKeyInKeyChain response : " + response);
                            String newKeyHmac = extractMessage(response, MESSAGE_FIELD);

                            response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                            Log.d("TAG", "getKeyChainInfo response : " + response);

                            response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(newKeyHmac);
                            String newKeyFromCard = extractMessage(response, MESSAGE_FIELD);
                            Log.d("TAG", "keyFromCard : " + response);

                            if (!newKeyInHex.toLowerCase().equals(newKeyFromCard.toLowerCase())) {
                                throw  new Exception("Bad key from card : " + newKeyFromCard);
                            }

                            response = cardKeyChainApi.deleteKeyFromKeyChainAndGetJson(newKeyHmac);
                            Log.d("TAG", "deleteKeyFromKeyChain response : " + response);

                            response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                            Log.d("TAG", "getKeyChainInfo response : " + response);

                            JSONObject jObject = new JSONObject(response);
                            int num  =  Integer.parseInt(jObject.getString(NUMBER_OF_KEYS_FIELD));

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
                          /*    if (!appletState.equals(WAITE_AUTHENTICATION_MSG)) {
                                throw new Exception("Incorrect applet state : " + appletState);
                            }

                           String hashesJsonStr = cardActivationApi.getHashesAndGetJson();
                            String hashOfEncryptedCommonSecret = extractMessage(hashesJsonStr, ECS_HASH_FIELD);
                            String hashOfEncryptedPassword = extractMessage(hashesJsonStr, EP_HASH_FIELD);

                            Log.d("TAG", "hashOfEncryptedCommonSecret : " + hashOfEncryptedCommonSecret);
                            Log.d("TAG", "hashOfEncryptedPassword : " + hashOfEncryptedPassword);*/

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
