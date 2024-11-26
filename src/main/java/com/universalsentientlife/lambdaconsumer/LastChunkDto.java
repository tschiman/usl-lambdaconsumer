package com.universalsentientlife.lambdaconsumer;

import java.math.BigDecimal;

public class LastChunkDto {
    private String fileName;
    private Integer fileCount;
    private BigDecimal fileSizeInMB;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public BigDecimal getFileSizeInMB() {
        return fileSizeInMB;
    }

    public void setFileSizeInMB(BigDecimal fileSizeInMB) {
        this.fileSizeInMB = fileSizeInMB;
    }
}
