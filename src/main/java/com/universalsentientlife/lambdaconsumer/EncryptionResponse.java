package com.universalsentientlife.lambdaconsumer;

public class EncryptionResponse {
    //base64 encoded encryption key
    private byte[] encryptionKey;

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
