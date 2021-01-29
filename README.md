# TonNfcClientAndroid

The library is developed to handle communication of Android smartphones with NFC TON Labs Security cards. It provides a useful API to work with all functionality (i.e. APDU commands) supported by NFC TON Labs Security card.

## Installation

The library now is published onto  https://jitpack.io, see https://jitpack.io/#tonlabs/TonNfcClientAndroid.

Let's suppose you have an Android project. To use TonNfcClientAndroid library in your project you must go through the following steps.

+ Add it in your root build.gradle at the end of repositories

	allprojects {
	}

+ Add the dependency

+ You must take care of AndroidManifest.xml. It must contain some stuff related to NFC. You must add the following snippets.

For this to work you must have an appropriate nfc_tech_filter.xml file in your xml subfolder.

    print 'Hello, World!'
