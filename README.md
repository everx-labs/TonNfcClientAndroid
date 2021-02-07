# TonNfcClientAndroid

The library is developed to handle communication of Android smartphones with NFC TON Labs Security cards. It provides a useful API to work with all functionality (i.e. APDU commands) supported by NFC TON Labs Security card.

## Installation

The library is published on  https://jitpack.io, see https://jitpack.io/#tonlabs/TonNfcClientAndroid.

Let's suppose you have an Android project. To use TonNfcClientAndroid library in the project you must go through the following steps.

+ Add it in your root build.gradle at the end of repositories.

		allprojects {
			repositories {
				...
				maven { url 'https://jitpack.io' }
			}
		}

	
+ Add the dependency and replace here Tag by the necessary version.

		dependencies {
			implementation 'com.github.tonlabs:TonNfcClientAndroid:Tag'
		}
		
+ You must take care of AndroidManifest.xml. It must contain NFC permission and special intent filter. Add the following snippets.

		<uses-permission android:name="android.permission.NFC" />
		<uses-feature android:name="android.hardware.nfc" android:required="true" />
		<intent-filter>
    			<action android:name="android.nfc.action.NDEF_DISCOVERED" />
    			<action android:name="android.nfc.action.TECH_DISCOVERED" />
    			<action android:name="android.nfc.action.TAG_DISCOVERED" />
		</intent-filter>
		<meta-data android:name="android.nfc.action.TECH_DISCOVERED" android:resource="@xml/nfc_tech_filter" />

For this to work you must have an appropriate nfc_tech_filter.xml file in your xml subfolder (\app\src\main\res\xml). File nfc_tech_filter.xml must looks as follows.

		<?xml version="1.0" encoding="utf-8"?>
		<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    			<tech-list>
        			<tech>android.nfc.tech.IsoDep</tech>
        			<tech>android.nfc.tech.NfcA</tech>
    			</tech-list>
		</resources>
		
To get the full picture of how AndroidManifest.xml should look like you may walk through the exemplary app inside https://github.com/tonlabs/TonNfcClientAndroid/tree/master/app/ .

_Note:_ minSdkVersion now is 24 to use the library.

## Usage (Simple example)

Let's suppose you want to work with NFC TON Labs security card in your MainActivity class. And you want to make a simple request to the card: return the maximum number of card's PIN tries. For this request there is a special APDU command supported by the card. And there is a corresponding function in TonNfcClientAndroid library sending it to the card and making postprocessing of card's response for you.  To make it work you should go through the following steps.

+ Make the following imports in your MainActivity.

		import com.tonnfccard.api.CardCoinManagerApi;
		import com.tonnfccard.api.nfc.NfcApduRunner;
		
+ Add the snippet looking like this:

		private NfcApduRunner nfcApduRunner;
		private CardCoinManagerApi cardCoinManagerNfcApi;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(android.example.myapplication.R.layout.activity_main);
			...
			try {
				nfcApduRunner = NfcApduRunner.getInstance(getApplicationContext());
				cardCoinManagerNfcApi = new CardCoinManagerApi(getApplicationContext(),  nfcApduRunner);
			}
			catch (Exception e) {
				Log.e("TAG", "Error happened : " + e.getMessage());;
			}
			...
		}

+ Also take care of onNewIntent method. It intercepts the intent created after NFC card (tag) connection. And you must extract the data about the tag from the intent. You need it to start work with the tag.

		@Override
		public void onNewIntent(Intent intent) {
			super.onNewIntent(intent);
			try {
				if (nfcApduRunner.setCardTag(intent)) {
					Toast.makeText(this, "NFC hardware touched", Toast.LENGTH_SHORT).show();
				}
			}
			catch (Exception e) {
				Log.e("TAG", "Error happened : " + e.getMessage());
			}
		}

+ Finally make the request to the card. In this example we send it after pressing the button. So the activity for the button may look as follows.


		public void addListenerOnButton() {
        		button = (Button) findViewById(android.example.myapplication.R.id.button1);

        		button.setOnClickListener(new View.OnClickListener() {

            			@Override
            			public void onClick(View arg0) {
                			try {
                    				String json = cardCoinManagerNfcApi.getMaxPinTriesAndGetJson();
                    				Log.d("TAG", "Card response : " + json);
                			}
                			catch (Exception e) {
                    				e.printStackTrace();
                    				Log.e("TAG", "Error happened : " + e.getMessage());
                			}
            			}
        		});
    		}
		
Here json variable contains the response from card wrapped into json of the following simple format: 
		
		{"message":"done","status":"ok"}
		
To get the full picture of how the simplest MainActivity may look like you may walk through the exemplary app inside https://github.com/tonlabs/TonNfcClientAndroid/tree/master/app/ .

## More about responses format

### Case of successful operation

In the case of successful operation with the card any function of TonNfcClientAndroid library always returns json string with two fields "message" and "status". "status" will contain "ok". In the field "message" you will find an expected payload. So jsons may look like this.

	{"message":"done","status":"ok"}
	{"message":"generated","status":"ok"}
	{"message":"HMac key to sign APDU data is generated","status":"ok"}
	{"message":"980133A56A59F3A59F174FD457EB97BE0E3BAD59E271E291C1859C74C795A83368FD8C7405BC37E1C4146F4D175CF36421BF6AD2AFF4329F5A6C6D772247ED03","status":"ok"}
	etc.

### Case of error

If some error happened then functions of TonNfcClientAndroid library produce error messages wrapped into json strings of special format. The structure of json depends on the  error class. There are two main classes of errors.

#### Applet (card) errors

It is the case when applet (installed on the card) threw some error status word (SW). So Android code just catches it and throws away. The exemplary error json looks like this.

	{
		"message":"Incorrect PIN (from Ton wallet applet).",
		"status":"fail",
		"errorCode":"6F07",
		"errorTypeId":0,
		"errorType":"Applet fail: card operation error",
		"cardInstruction":"VERIFY_PIN",
		"apdu":"B0 A2 00 00 44 35353538EA579CD62F072B82DA55E9C780FCD0610F88F3FA1DD0858FEC1BB55D01A884738A94113A2D8852AB7B18FFCB9424B66F952A665BF737BEB79F216EEFC3A2EE37 FFFFFFFF "
	}
	
Here:
+ *errorCode* — error status word (SW) produced by the card (applet)

+ *cardInstruction* — title of APDU command that failed

+ *errorTypeId* — id of error type ( it will always be zero here)

+ *errorType* — description of error type 

+ *message* — contains error message corresponding to errorCode thrown by the card.

+ *apdu* — full text of failed APDU command in hex format

#### Android errors

It is the case when error happened in Android code itself. The basic examples: troubles with NFC connection or incorrect format of input data passed into TonNfcClientAndroid library from the outside world. The exemplary error json looks like this.

	{
		"errorType": "Native code fail: incorrect format of input data",
		"errorTypeId": "3",
		"errorCode": "30006",
		"message": "Pin must be a numeric string of length 4.",
		"status": "fail"
	}
	
In this [document](https://github.com/tonlabs/TonNfcClientAndroid/blob/master/docs/ErrorrList.md) you may find the full list of json error messages (and their full classification) that can be thrown by the library.

_Note:_ In above snippet in the case of any exception happened during work of cardCoinManagerNfcApi.getMaxPinTriesAndGetJson() we will come into catch block. Message inside exception e is always in json format. 	

## Test work with the card

After you prepared the appliction run it on your Android device (not simulator). Then you need to establish NFC connection. For this hold the card to the top of the smartphone (field near te camera) as close as possible. Usually smartphone vibrates after establishing a connection. And if you see above example you must get the toast with the message "NFC hardware touched". It means that NFC connection is established. To keep connection alive you must not move card and smartphone and they should have physical contact.

After NFC connection is ready we can send command to te card. Push the button to make request getMaxPinTries. Check your Logcat console in Android Studio. You must here the following output.

		===============================================================
		===============================================================
		>>> Send apdu  00 A4 04 00 
		(SELECT_COIN_MANAGER)
		SW1-SW2: 9000, No error., response data bytes: 	6F5C8408A000000151000000A...
		===============================================================
		===============================================================
		 >>> Send apdu  80 CB 80 00 05 DFFF028103 
		(GET_PIN_TLT)
 		SW1-SW2: 9000, No error., response data bytes: 0A
 
 		Card response : {"message":"10","status":"ok"}

Here you see the log of APDU commands sent to the card and their responses in raw format. And in the end there is a final wrapped response.

## Card activation

When user gets NFC TON Labs security card  at the first time, the applet on the card is in a special state. It waits for user authentication. And the main functionality of applet is blocked for now. So you must authenticate the user, i.e. go through procedure of card activation.

To activate the card user must have three secret hex strings authenticationPassword, commonSecret, initialVector. There is a bijection between serial number (SN) printed on the card and the tuple (authenticationPassword, commonSecret, initialVector). These strings he is going to get from Tracking Smartcontract deployed for his NFC TON Labs security card .

Let's suppose the user somehow got authenticationPassword, commonSecret, initialVector. Then to activate the card he may use the following exemplary snippet.

import com.tonnfccard.api.CardCoinManagerApi;
import com.tonnfccard.api.CardActivationApi;
import com.tonnfccard.api.nfc.NfcApduRunner;

....

private NfcApduRunner nfcApduRunner;
private CardActivationApi cardActivationApi;
private CardCoinManagerApi cardCoinManagerNfcApi;
private static final String DEFAULT_PIN = "5555";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.example.myapplication.R.layout.activity_main);
	addListenerOnButton();
	...
        try {
            nfcApduRunner = NfcApduRunner.getInstance(getApplicationContext());
            cardCoinManagerNfcApi = new CardCoinManagerApi(getApplicationContext(),  nfcApduRunner);
            cardActivationApi = new CardActivationApi(getApplicationContext(),  nfcApduRunner);
				}
        catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
	...
    }


    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            if (nfcApduRunner.setCardTag(intent)) {
                Toast.makeText(this, "NFC hardware touched", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Log.e("TAG", "Error happened : " + e.getMessage());
        }
    }
    
    public void addListenerOnButton() {
    	button = (Button) findViewById(android.example.myapplication.R.id.button1);
	button.setOnClickListener(new View.OnClickListener() {
            @Override
	    public void onClick(View arg0) {
	    	try {
			// here we already have authenticationPassword, commonSecret, initialVector
			String jsonStr = cardCoinManagerNfcApi.getRootKeyStatusAndGetJson();
			JSONObject jObject = new JSONObject(jsonStr);
			String seedStatus = jObject.getString("message");
			if (seedStatus.equals("not generated")) {
				cardCoinManagerNfcApi.generateSeedAndGetJson(DEFAULT_PIN);
			}
			jsonStr = cardActivationApi.getTonAppletStateAndGetJson();
			jObject = new JSONObject(jsonStr);
			String appletState = jObject.getString("message");
			
			if (!state.equals("TonWalletApplet waits two-factor authorization.")) {
				return; //throw "Incorret applet state"
			}
			
			jsonStr = cardActivationApi.getHashOfCommonSecretAndGetJson();
			JSONObject jObject = new JSONObject(jsonStr);
			String hashOfCommonSecret = jObject.getString("message"); // SHA256 has
			// check that hashOfCommonSecret is correct based on the data from smartcontract
			
			jsonStr = cardActivationApi.hashOfEncryptedPasswordAndGetJson();
			jObject = new JSONObject(jsonStr);
			String hashOfEncryptedPassword = jObject.getString("message"); // SHA256 has
			// check that hashOfEncryptedPassword is correct based on the data from smartcontract
			
			String newPin = "7777";
			jsonStr = cardActivationApi.turnOnWalletAndGetJson(newPin, authenticationPassword, commonSecret, initialVector);
			jObject = new JSONObject(jsonStr);
			appletState = jObject.getString("message");
			Log.d("TAG", "Card response : " + appletState);
			
			if (!state.equals("TonWalletApplet is personalized.")) {
				throw new Exception("Incorret applet state after activation : " + appletState)
			}

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getMessage());
                }
            }
        });
	}
	
## Request ED25519 signature

The main functionality provided by NFC TON Labs security card is Ed25519 signature. You may request public key and request the signature for some message.

		//Create these two objects.
		nfcApduRunner = NfcApduRunner.getInstance(getApplicationContext());
		CardCryptoApi cardCryptoApi = new CardCryptoApi(getApplicationContext(), nfcApduRunner);

		....

		//Get public key for given hdIndex
		String hdInd = "1";
		String jsonStr = cardCryptoApi.getPublicKeyAndGetJson(hdIndex);
		jObject = new JSONObject(jsonStr);
		String pkInHex = jObject.getString("message");

		....

		//Sign a message for given hdIndex
		String msg = "A10D";
		String pin = "5555";
		jsonStr = cardCryptoApi.verifyPinAndSignAndGetJson(msg, hdInd, pin);
		jObject = new JSONObject(jsonStr);
		String signature = jObject.getString("message");

**Important note:** The important point for ed25519 signature request is that it is under an additional protection against MITM attack. In above example function cardCryptoApi.verifyPinAndSignAndGetJson() sends two critical APDU commads into the card : VERIFY_PIN and SIGN_SHORT_MESSAGE. For protection the data fields of these APDUs are signed by HMAC SHA256 symmetric signature. The key for it is elaborated based on user's authentication data (see Card activation section). This key is saved into Android Keystore and then it is used by the app to sign APDU commands data fields. Usually after correct card activation in the app (call of cardActivationApi.turnOnWalletAndGetJson) this key is produced and saved into keystore. So no extra code is required it work.

Another situation is possible. Let's suppose you activated the card earlier. After that you reinstalled the app working with NFC TON Labs security card (or you started using new Android device). In this case Android Keystore does not have the key to sign APDU commands. So you must create it.

	String jsonStr = cardCryptoApi.createKeyForHmac(authenticationPassword, commonSecret, serialNumber);
	JSONObject jObject = new JSONObject(jsonStr);
	String status = jObject.getString("message"); // must be == "done"
	
## Full functions list 

The full list of functions provided by the library to communicate with the card you will find [here](https://github.com/tonlabs/TonNfcClientAndroid/blob/master/docs/FuntionsList.md)


