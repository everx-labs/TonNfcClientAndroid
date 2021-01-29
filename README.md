# TonNfcClientAndroid

The library is developed to handle communication of Android smartphones with NFC TON Labs Security cards. It provides a useful API to work with all functionality (i.e. APDU commands) supported by NFC TON Labs Security card.

## Installation

The library now is published onto  https://jitpack.io, see https://jitpack.io/#tonlabs/TonNfcClientAndroid.

Let's suppose you have an Android project. To use TonNfcClientAndroid library in your project you must go through the following steps.

+ Add it in your root build.gradle at the end of repositories.

		allprojects {
			repositories {
				...
				maven { url 'https://jitpack.io' }
			}
		}
	
+ Add the dependency andreplace here Tag by the necessary version.

		dependencies {
			implementation 'com.github.tonlabs:TonNfcClientAndroid:Tag'
		}
		
+ You must take care of AndroidManifest.xml. It must contain some stuff related to NFC. You must add the following snippets.

		<uses-permission android:name="android.permission.NFC" />
		<uses-feature android:name="android.hardware.nfc" android:required="true" />
		<intent-filter>
    			<action android:name="android.nfc.action.NDEF_DISCOVERED" />
    			<action android:name="android.nfc.action.TECH_DISCOVERED" />
    			<action android:name="android.nfc.action.TAG_DISCOVERED" />
		</intent-filter>
		<meta-data android:name="android.nfc.action.TECH_DISCOVERED" android:resource="@xml/nfc_tech_filter" />

For this to work you must have an appropriate nfc_tech_filter.xml file in your xml subfolder. File nfc_tech_filter.xml must looks as follows.

		<?xml version="1.0" encoding="utf-8"?>
		<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    			<tech-list>
        			<tech>android.nfc.tech.IsoDep</tech>
        			<tech>android.nfc.tech.NfcA</tech>
    			</tech-list>
		</resources>
		
To get the full picture of how AndroidManifest.xml must look like you may walk through the exemplary app inside https://github.com/tonlabs/TonNfcClientAndroid/tree/master/app/.


## Usage (Simple example)

Let's suppose you want to work with NFC TON Labs security card in your MainActivity class. And you want to make a simple request to the card: ask it to return the maximum number of card's PIN tries into Android app. For this request there is a corresponding APDU command supporting by the card. And there is a corresponding function in TonNfcClientAndroid library that sends this APDU to the card and makes postprocessing of card's response for you. 

To make it work you should go through the following steps.

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
				Log.e("TAG", e.getMessage());
			}
			...
		}

+ Also you must take care of onNewIntent method. It intercepts the intent created after card (tag) connection. And you must extract  the data about your tag from the intent. You need it to start work with the tag.

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

+ And finally let's make the request to the card. In our simple example we send it after pressing some button. So the activity for the button may look as follows.


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
		
"message" field in jsons produced by the library always contains payload in the case of success. And in the case of fail this field will contain error message.

In above snippet in the case of any exception happened during work of cardCoinManagerNfcApi.getMaxPinTriesAndGetJson() we will come into catch block. Message inside exception e is always in json format. And here you may find the full list of json error messages that can be thrown by the library.

Again to get the full picture of howthe simplest MainActivity may look like you may walk through the exemplary app inside https://github.com/tonlabs/TonNfcClientAndroid/tree/master/app/ .

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
