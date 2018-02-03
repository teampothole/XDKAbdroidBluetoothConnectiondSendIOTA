package com.example.telefon.pothole;

/**
 * Created by csp4abt on 29.01.2018.
 */

import android.app.Application;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Security;

import rs.iota.jni.IOTA;

public class ExampleApplication extends Application{

    static {
        Security.addProvider(new BouncyCastleProvider());
        System.loadLibrary("iotajni");
        System.loadLibrary("iotapow");
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }
}
