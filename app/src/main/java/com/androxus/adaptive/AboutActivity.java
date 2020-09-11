package com.androxus.adaptive;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.androxus.aiheadphone";
    public static final String PRIVACY_POLICY = "https://docs.google.com/document/d/1PU7tMwcdutVQoX93KBKCCK-hFpoGj6FINZT_3zqQ_vY/edit?usp=sharing";
    private static final String TAG = "AboutActivity";
    LinearLayout rateOnPlayStore, eMail, licenses, privacyPolicy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar!=null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        rateOnPlayStore = findViewById(R.id.rate_on_google_play);
        eMail = findViewById(R.id.report_bugs);
        licenses = findViewById(R.id.licenses);
        privacyPolicy = findViewById(R.id.privacy_policy);

        rateOnPlayStore.setOnClickListener(this);
        eMail.setOnClickListener(this);
        licenses.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        if(v==licenses){
            LicensesDialogFragment dialog = LicensesDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "LicensesDialog");
        }
        else if (v == rateOnPlayStore){
            openUrl(RATE_ON_GOOGLE_PLAY);
        }

        else if(v == eMail){
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:androxus.app@gmail.com"));
            intent.putExtra(Intent.EXTRA_EMAIL, "androxus.app@gmail.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "AI Headphone");
            startActivity(Intent.createChooser(intent, "E-Mail"));
        }
        else if (v == privacyPolicy){
            openUrl(PRIVACY_POLICY);
        }
    }
}
