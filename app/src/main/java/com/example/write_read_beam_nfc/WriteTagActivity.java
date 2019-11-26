package com.example.write_read_beam_nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

public class WriteTagActivity extends AppCompatActivity {

    private EditText edtWriteTag;
    private Button btnWriteTag;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] writeTagFilters;
    private boolean write = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_tag);

        edtWriteTag = findViewById(R.id.edtWriteTag);
        btnWriteTag = findViewById(R.id.btnWriteTag);

        btnWriteTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeTag();
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null){
            Toast.makeText(this, "your device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkEnable();

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEnable();

    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (write){
            if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
                Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                writeToTag(buildNdefMessage(), detectedTag);
            }
        }
    }



    private void checkEnable() {
        Boolean nfcEnable = nfcAdapter.isEnabled();
        if (!nfcEnable){
            new AlertDialog.Builder(WriteTagActivity.this)
                    .setTitle(getString(R.string.text_warning_nfc_is_off))
                    .setMessage(getString(R.string.text_turn_on_nfc))
                    .setPositiveButton(getString(R.string.text_update_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
                        }
                    }).create().show();
        }
    }

    private void writeTag() {
        if (edtWriteTag.getText().toString().trim() == null){
            Toast.makeText(this, "The data to write is empty. Please fill it!", Toast.LENGTH_SHORT).show();
        }else {
            enableWriteTag();
        }
    }

    private void enableWriteTag() {
        write = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        edtWriteTag.setEnabled(false);
    }

    boolean writeToTag(NdefMessage message, Tag tag){
        int size = message.toByteArray().length;
            Ndef ndef = Ndef.get(tag);
            if (ndef != null){
                try {
                    ndef.connect();
                    if (!ndef.isWritable()){
                        Toast.makeText(this, "Cannot write to this tag. This tag is read-only", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (ndef.getMaxSize() < size){
                        Toast.makeText(this,
                                "Cannot write to this tag. Message size (" + size + " bytes) exceeds this tag's capacity of " + ndef.getMaxSize()
                                        + " bytes.", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    ndef.writeNdefMessage(message);
                    Toast.makeText(this, "A pre-formatted tag was successfully updated", Toast.LENGTH_SHORT).show();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
            }
        return false;
    }

    private NdefMessage buildNdefMessage() {
        String data = edtWriteTag.getText().toString().trim();
        String mimeType = "application/com.elsinga.sample.nfc";

        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        byte[] idBytes = new byte[0];

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, idBytes, dataBytes);
        NdefMessage message = new NdefMessage(new NdefRecord[]{record});
        return message;

    }
}
