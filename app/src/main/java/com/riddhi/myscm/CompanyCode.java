package com.riddhi.myscm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;


import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import support.SharedValues;

public class CompanyCode extends AppCompatActivity {

    EditText companyCode;
    Button submit;
    Context context;
    String[] requests = {Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int REQUEST_PERMISSION_LOCATION = 10;
    public static final int REQUEST_READ_PHONE_STATE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_code);

        context = this;
        companyCode = findViewById(R.id.companyCode);
        submit = findViewById(R.id.submitComCode);

        final String comCode = new SharedValues(context).loadSharedPreferences_CompanyCode();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            ActivityCompat.requestPermissions(this,requests,1);

        if (!comCode.equals(""))
        {
            startActivity(new Intent(context,MainActivity.class));
            finish();
        }

        submit.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(companyCode.getText().toString()))
            {
                new SharedValues(context).saveSharedPreference("companyCode",companyCode.getText().toString());
                startActivity(new Intent(context,MainActivity.class));
                finish();
            }
            else
            {
                companyCode.setError("Company code should not be empty");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;

            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                }
                break;

            default:
                break;
        }
    }
}
