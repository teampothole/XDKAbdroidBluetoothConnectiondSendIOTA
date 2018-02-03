package org.iota.mobile;

import jota.IotaLocalPoW;
import jota.model.Transaction;

public class NativePoW implements IotaLocalPoW {
    @Override
    public String performPoW(String trytes, int mwm) {
        long start = System.currentTimeMillis();
        // The JNI interface only returns the nonce.
        String nonce = Interface.doPOW(trytes, mwm);
        long end = System.currentTimeMillis();

        System.err.println("NativePoW duration = " + (end - start));

        // We have to replace the last 27 trytes in the input with the nonce.
        String finalTrytes = trytes.substring(0, trytes.length() - nonce.length()) + nonce;

        return finalTrytes;
    }
}
