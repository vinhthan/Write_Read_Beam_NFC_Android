package com.example.write_read_beam_nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ReadTagActivity extends AppCompatActivity {

    private static final String TAG = ReadTagActivity.class.getSimpleName();

    private TextView txvData;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] readTagFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_tag);

        txvData = findViewById(R.id.txvData);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null){
            Toast.makeText(this, "Your device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkNfcEnable();

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("application/com.elsinga.sample.nfc");
        }catch (IntentFilter.MalformedMimeTypeException e){
            throw new RuntimeException("Could not add MIME type", e);
        }

        readTagFilters = new IntentFilter[]{ndefDetected};



    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNfcEnable();

        if (getIntent().getAction() != null){
            if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
                NdefMessage[] messages = getNdefMessageFromIntent(getIntent());
                NdefRecord record = messages[0].getRecords()[0];
                byte[] payload = record.getPayload();

                String paloadString = new String(payload);
                txvData.setText(paloadString);

            }
        }
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, readTagFilters, null);

    }



    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            NdefMessage[] msgs = getNdefMessageFromIntent(intent);
            confirmDisplayContentOverwrite(msgs[0]);
        }else {
            Toast.makeText(this, "This NFC tag has no NDEF data", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkNfcEnable() {
        Boolean nfcEnabled = nfcAdapter.isEnabled();
        if (!nfcEnabled)
        {
            new AlertDialog.Builder(ReadTagActivity.this).setTitle(getString(R.string.text_warning_nfc_is_off))
                    .setMessage(getString(R.string.text_turn_on_nfc)).setCancelable(false)
                    .setPositiveButton(getString(R.string.text_update_settings), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }).create().show();
        }
    }

    private NdefMessage[] getNdefMessageFromIntent(Intent intent) {
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) || action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++){
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }else {
                // unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }

        }else {
            Log.e(TAG, "getNdefMessageFromIntent: ");
            finish();
        }
        return msgs;
    }

    private void confirmDisplayContentOverwrite(final NdefMessage msg) {
        final String data = txvData.getText().toString().trim();

        new AlertDialog.Builder(this)
                .setTitle("New tag found")
                .setMessage("do you wanna show the content of this tag?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String payload = new String(msg.getRecords()[0].getPayload());
                        txvData.setText(new String(payload));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        txvData.setText(data);
                        dialogInterface.cancel();
                    }
                }).show();
    }
}
