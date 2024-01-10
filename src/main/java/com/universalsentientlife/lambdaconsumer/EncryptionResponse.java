package com.universalsentientlife.lambdaconsumer;

public class EncryptionResponse {
    //base64 encoded encryption key
    private byte[] encryptionKey;
    private String salt;

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }
}
