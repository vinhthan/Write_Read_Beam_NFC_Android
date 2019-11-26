package com.example.write_read_beam_nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;

public class BeamActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback{

    private EditText edtBeam;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beam);

        edtBeam = findViewById(R.id.edtBeam);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null){
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nfcAdapter.setNdefPushMessageCallback(this, this);

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String data = edtBeam.getText().toString().trim();
        String mimeType = "application/com.elsinga.sample.nfc";

        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        byte[] idBytes = new byte[0];

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, idBytes, dataBytes);
        NdefMessage message = new NdefMessage(new NdefRecord[]{record});

        return message;
    }


}
