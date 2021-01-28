package com.tonnfccard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.tonnfccard.api.CardCoinManagerApi;
import com.tonnfccard.api.nfc.NfcApduRunner;

public class MainActivity extends AppCompatActivity {

    private NfcApduRunner nfcApduRunner;
    private CardCoinManagerApi cardCoinManagerNfcApi;

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButton();
        try {
            nfcApduRunner = NfcApduRunner.getInstance(getApplicationContext());
            cardCoinManagerNfcApi = new CardCoinManagerApi(getApplicationContext(),  nfcApduRunner);
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

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.button1);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    String json = cardCoinManagerNfcApi.getMaxPinTriesAndGetJson();
                    Log.d("TAG", "Card response : " + json);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error happened : " + e.getStackTrace().toString());
                }
            }

        });

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
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
