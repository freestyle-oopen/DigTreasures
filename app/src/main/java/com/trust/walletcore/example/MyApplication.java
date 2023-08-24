package com.trust.walletcore.example;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setupBouncyCastle();
        CrashReport.initCrashReport(getApplicationContext(), "3cd1f61475", false);
    }
    private void setupBouncyCastle(){

       final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
       if(provider == null){
           return;
       }
       if(provider.getClass().equals(BouncyCastleProvider.class)){
           return;
       }
       Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(),1);

    }
}
