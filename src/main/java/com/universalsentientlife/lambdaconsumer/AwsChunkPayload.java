package com.universalsentientlife.lambdaconsumer;

import java.math.BigDecimal;

public class AwsChunkPayload {
    private String fileName;
    private Integer chunkNumber;
    private byte[] chunk;
    private String email;
    private String password;
    private boolean lastChunk;
    private BigDecimal fileSizeInMB;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(Integer chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }

    public void setLastChunk(boolean lastChunk) {
        this.lastChunk = lastChunk;
    }

    public boolean isLastChunk() {
        return lastChunk;
    }

    public void setFileSizeInMB(BigDecimal fileSizeInMB) {
        this.fileSizeInMB = fileSizeInMB;
    }

    public BigDecimal getFileSizeInMB() {
        return fileSizeInMB;
    }
}

