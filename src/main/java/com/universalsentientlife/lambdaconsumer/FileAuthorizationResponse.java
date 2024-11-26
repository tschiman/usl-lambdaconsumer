package com.universalsentientlife.lambdaconsumer;

public class FileAuthorizationResponse {
    private String fileName;
    private boolean isDownloadAllowed;
    private String reason;
    private boolean isUploadAllowed;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDownloadAllowed() {
        return isDownloadAllowed;
    }

    public void setDownloadAllowed(boolean downloadAllowed) {
        isDownloadAllowed = downloadAllowed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isUploadAllowed() {
        return isUploadAllowed;
    }

    public void setUploadAllowed(boolean uploadAllowed) {
        isUploadAllowed = uploadAllowed;
    }
}
