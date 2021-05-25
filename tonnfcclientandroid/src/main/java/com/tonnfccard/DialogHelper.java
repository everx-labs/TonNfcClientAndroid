package com.tonnfccard;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.tonnfccard.smartcard.ApduRunner;

public class DialogHelper {

    public static final String NFC_CARD_OPERATION_INTERRUPTED = "NFC Card operation was interrupted!";

   // private Context activity;
    //private ApduRunner apduRunner;
    private final AlertDialog.Builder builder;

    public DialogHelper(Context activity, ApduRunner apduRunner) {
        ImageView image = new ImageView(activity);
        image.setImageResource(R.drawable.sphone);
        builder = new AlertDialog.Builder(activity)
                .setTitle("Ready to scan the card")
                // .setMessage("Hold your smartphone near the card.")
                /* .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                     // Continue with delete operation
                   }
                 })*/
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            apduRunner.disconnectCard();
                            Toast.makeText(activity, NFC_CARD_OPERATION_INTERRUPTED, Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(image);
    }

    public AlertDialog createInvitationDialog(){
        return builder.create();
    }
}
