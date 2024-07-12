package com.example.smstry;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE =1;
    private EditText phoneEditTet,messageEditText;
    private ImageView sendSmsButton;
    private EditText editText;
    private String name;
    private TextView textView;
    private static final int REQUEST_READ_CONTACTS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        phoneEditTet = findViewById(R.id.edit1);
        messageEditText = findViewById(R.id.edit2);
        sendSmsButton = findViewById(R.id.imagebutton);

        editText= findViewById(R.id.editext);
        Button button2 = findViewById(R.id.button2);
        textView=findViewById(R.id.contacts_text_view);

        button2.setOnClickListener(v -> {
            name = editText.getText().toString();

            // Check if the READ_CONTACTS permission is already available.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                // If the permission is not granted, request it.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            } else {
                // Permission is already available, perform the contact search.
                findContact(name);
            }
        });



        sendSmsButton.setOnClickListener(v->{
            String msgText = messageEditText.getText().toString();
            String phoneText = phoneEditTet.getText().toString();
            if(checkSmsPermission())
            {
                sendSms(phoneText,msgText);
                Toast.makeText(this, "SMS SENT", Toast.LENGTH_SHORT).show();
            }
            else
            {
                requestSmsPermission();
            }
        });
    }
    private boolean checkSmsPermission()
    {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS);
        return result== PackageManager.PERMISSION_GRANTED;
    }

    private boolean requestSmsPermission()
    {
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},REQUEST_CODE);
        return false;
    }
    private void sendSms(String phone,String message)
    {
        try
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone,null,message,null,null);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    private void findContact(String contactName) {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (name != null && name.equalsIgnoreCase(contactName)) {
                    String phoneNumber = null;

                    // Check if the contact has a phone number
                    int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null
                        );

                        if (phoneCursor != null && phoneCursor.moveToFirst()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phoneCursor.close();
                        }
                    }

                    // Display the contact name and phone number
                    String message = "Name: " + name + "\n"+"Number: " + phoneNumber;
                    String toast = name + "-" + phoneNumber;
                    String number = phoneNumber;
                    phoneEditTet.setText(number);
                    return; // Exit after finding the first matching contact
                }
            }
            cursor.close();
        }

        // If no contact was found
        Toast.makeText(this, "Contact " + contactName + " not found.", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform the contact search.
                findContact(name);
            } else {
                // Permission denied, show a message to the user.
                Toast.makeText(this, "Permission to read contacts was denied", Toast.LENGTH_LONG).show();
            }
        }
    }

}
