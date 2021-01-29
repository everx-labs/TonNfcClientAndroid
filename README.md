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
		
To get the full picture of how AndroidManifest.xml must look like you may walk through the exemplary app inside https://github.com/tonlabs/TonNfcClientAndroid/tree/master/app/ .
