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

import com.tonnfccard.api.CardActivationApi;
import com.tonnfccard.api.CardCoinManagerApi;
import com.tonnfccard.api.CardCryptoApi;
import com.tonnfccard.api.CardKeyChainApi;
import com.tonnfccard.api.nfc.NfcApduRunner;

import static com.tonnfccard.api.CardKeyChainApi.NUMBER_OF_KEYS_FIELD;
import static com.tonnfccard.api.utils.JsonHelper.*;
import static com.tonnfccard.api.utils.ResponsesConstants.*;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.*;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String DEFAULT_PIN = "5555";

    // Hardcoded for now activation data
    private static final String SERIAL_NUMBER = "504394802433901126813236";
    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String IV = "1A550F4B413D0E971C28293F9183EA8A";
    private static final String PASSWORD =  "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";

    private NfcApduRunner nfcApduRunner;
    private CardCoinManagerApi cardCoinManagerNfcApi;
    private CardActivationApi cardActivationApi;
    private CardCryptoApi cardCryptoApi;
    private CardKeyChainApi cardKeyChainApi;

    Button buttonGetMaxPinTries;
    Button buttonActivateCard;
    Button buttonPk;
    Button buttonSign;
    Button buttonTryKeychain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnGetMaxPinTriesButton();
        addListenerOnActivateCardButton();
        addListenerOnGetPkButton();
        addListenerOnSignButton();
        addListenerOnTryKeychainButton();
        try {
            Context activity = getApplicationContext();
            nfcApduRunner = NfcApduRunner.getInstance(activity);
            cardCoinManagerNfcApi = new CardCoinManagerApi(activity,  nfcApduRunner);
            cardActivationApi = new CardActivationApi(activity,  nfcApduRunner);
            cardCryptoApi =  new CardCryptoApi(activity,  nfcApduRunner);
            cardKeyChainApi = new CardKeyChainApi(activity,  nfcApduRunner);
        }
        catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            if (nfcApduRunner.setCardTag(intent)) {
                Toast.makeText(this, "NFC hardware touched!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Log.e("TAG", "Error happened : " + e.getMessage());
        }
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

                    String keyInHex = "001122334455";
                    response = cardKeyChainApi.addKeyIntoKeyChainAndGetJson(keyInHex);
                    Log.d("TAG", "addKeyIntoKeyChain response : " + response);

                    String keyHmac = extractMessage(response);
                    Log.d("TAG", "keyHmac : " + response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(keyHmac);
                    String keyFromCard = extractMessage(response);
                    Log.d("TAG", "keyFromCard : " + response);

                    if (!keyInHex.equals(keyFromCard)) {
                        throw  new Exception("Bad key from card : " + keyFromCard);
                    }

                    String newKeyInHex = "00AA22334466";
                    response = cardKeyChainApi.changeKeyInKeyChainAndGetJson(newKeyInHex, keyHmac);
                    Log.d("TAG", "changeKeyInKeyChain response : " + response);
                    String newKeyHmac = extractMessage(response);

                    response = cardKeyChainApi.getKeyChainInfoAndGetJson();
                    Log.d("TAG", "getKeyChainInfo response : " + response);

                    response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(newKeyHmac);
                    String newKeyFromCard = extractMessage(response);
                    Log.d("TAG", "keyFromCard : " + response);

                    if (!newKeyInHex.equals(newKeyFromCard)) {
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
                    String response = cardCoinManagerNfcApi.getMaxPinTriesAndGetJson();
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
                    String response = cardCryptoApi.getPublicKeyAndGetJson(hdIndex);
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
