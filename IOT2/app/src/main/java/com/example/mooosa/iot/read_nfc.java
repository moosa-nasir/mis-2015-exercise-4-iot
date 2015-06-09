package com.example.mooosa.iot;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// -------------------------- References -----------------------------------------------
//http://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc.html
//http://stackoverflow.com/questions/12453658/reading-data-from-nfc-tag
//http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
//http://stackoverflow.com/questions/6060312/how-do-you-read-the-unique-id-of-an-nfc-tag-on-android
//------------------------------------------------------------------------------------

public class read_nfc extends Activity {

    TextView mTagTech,mTagId,mTagData,mTagHex,mTagAscii, mTagHexHead, mTagAsciiHead;
    NfcAdapter nfcAdapter;
    IntentFilter[] readTagFilters;
    PendingIntent pendingIntent;
    byte[] payload;
    String text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfc);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mTagTech  = (TextView) findViewById(R.id.tag_tech);
        mTagId  = (TextView) findViewById(R.id.tag_id);
        mTagData  = (TextView) findViewById(R.id.tag_data);
        mTagHex  = (TextView) findViewById(R.id.tag_hex);
        mTagAscii  = (TextView) findViewById(R.id.tag_ascii);
        mTagHexHead = (TextView) findViewById(R.id.tag_hex_head);
        mTagAsciiHead = (TextView) findViewById(R.id.tag_ascii_head);

        mTagHex.setVisibility(View.GONE);
        mTagHexHead.setVisibility(View.GONE);
        mTagAscii.setVisibility(View.GONE);
        mTagAsciiHead.setVisibility(View.GONE);


        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!nfcAdapter.isEnabled()) {
            mTagData.setText("NFC is disabled.");
        } else {
            mTagData.setText("Tag data will be shown here.");
        }

        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(this,getClass()).
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter filter2     = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        readTagFilters = new IntentFilter[]{tagDetected,filter2};

    }

    protected void onNewIntent(Intent intent) {
        if(intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
            setIntent(intent);
            mTagHex.setVisibility(View.GONE);
            mTagHexHead.setVisibility(View.GONE);
            mTagAscii.setVisibility(View.GONE);
            mTagAsciiHead.setVisibility(View.GONE);
            readFromTag(intent);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, readTagFilters, null);
    }



    public void showHexAndAscii(View v) {
        mTagHex.setVisibility(View.VISIBLE);
        mTagAscii.setVisibility(View.VISIBLE);
        mTagHexHead.setVisibility(View.VISIBLE);
        mTagAsciiHead.setVisibility(View.VISIBLE);

        mTagHex.setText(bytesToHexString(payload));
        mTagAscii.setText(StringToAscii(text));
    }
    public void readFromTag(Intent intent){

        try {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);


            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    mTagTech.setText(tech+"");
                }
            }
            mTagId.setText(String.valueOf(bytesToHexString(tag.getId()))+"");


            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (messages != null) {
                NdefMessage[] ndefMessages = new NdefMessage[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    ndefMessages[i] = (NdefMessage) messages[i];
                }
                NdefRecord record = ndefMessages[0].getRecords()[0];

                payload = record.getPayload();
                text = new String(payload);
                mTagData.setText(text);


            }
            else{
                mTagData.setText("Empty Tag");
            }
        }
        catch(Exception e) {
            Toast.makeText(getApplicationContext(), "Cannot Read From Tag.", Toast.LENGTH_LONG).show();
        }

    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    }

    private String StringToAscii(String input){
        String result = "";

        for (char ch: input.toCharArray()) {
            int ascii = (int) ch;
            result += Integer.toString(ascii) + " ";
        }
        return result;
    }

}