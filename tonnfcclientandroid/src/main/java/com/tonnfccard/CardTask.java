package com.tonnfccard;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.helpers.CardApiInterface;
import com.tonnfccard.helpers.ExceptionHelper;

import java.util.List;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_HMAC_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_NFC_CONNECTION_INTERRUPTED;

public class CardTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "CardTask";
    public static final String NFC_CARD_OPERATION_INTERRUPTED = "NFC Card operation was interrupted!";
    public static final String NFC_CARD_OPERATION_FINISHED = "NFC Card operation is finished!";
    public static final String NFC_CARD_OPERATION_FALED = "NFC Card operation failed!";
    public static final String READY_TO_SCAN_NFC = "Ready to scan the card";
    private static final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();

    private AlertDialog alert = null;
    private final NfcCallback nfcCallback;
    private final List<String> cardArgs;
    private final CardApiInterface<List<String>> cardOp;
    private final TonWalletApi tonWalletApi;


    public CardTask(TonWalletApi tonWalletApi, NfcCallback callBack, List<String> cardArgs, CardApiInterface<List<String>> cardOp, boolean showDialog) {
        this.nfcCallback = callBack;
        this.cardArgs = cardArgs;
        this.cardOp = cardOp;
        this.tonWalletApi = tonWalletApi;
        if (!showDialog || TonWalletApi.getActivity() == null) return;
        ImageView image = new ImageView(TonWalletApi.getActivity());
        image.setImageResource(R.drawable.sphone);
        AlertDialog.Builder builder = new AlertDialog.Builder(TonWalletApi.getActivity())
                .setTitle(READY_TO_SCAN_NFC)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                        Toast.makeText(TonWalletApi.getActivity(), NFC_CARD_OPERATION_INTERRUPTED, Toast.LENGTH_SHORT).show();
                        try {
                            tonWalletApi.getApduRunner().disconnectCard();
                            throw new Exception(ERROR_NFC_CONNECTION_INTERRUPTED);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            EXCEPTION_HELPER.handleException(e, nfcCallback, TAG);
                        }

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(image);
        alert = builder.create();
    }

    private void cancel() {
        this.cancel(true);
    }


    @Override
    protected void onPreExecute() {
        System.out.println("Start onPreExecute");
        if (alert != null) {
            alert.show();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        System.out.println("Start doInBackground");
        String json = null;
        try{
            json = cardOp.accept(cardArgs);
            System.out.println(json);
        } catch (Exception e){
            e.printStackTrace();
            if(nfcCallback != null) {
                EXCEPTION_HELPER.handleException(e, nfcCallback, TAG);
            }
        }
        return json;
    }

    @Override
    protected void onPostExecute(String result) {
        System.out.println("Start onPostExecute");
        if (alert != null) alert.dismiss();
        if(nfcCallback != null && result != null) {
            tonWalletApi.resolveJson(result, nfcCallback);
        }
        Toast.makeText(TonWalletApi.getActivity(), result != null ? NFC_CARD_OPERATION_FINISHED : NFC_CARD_OPERATION_FALED, Toast.LENGTH_SHORT).show();
    }
}