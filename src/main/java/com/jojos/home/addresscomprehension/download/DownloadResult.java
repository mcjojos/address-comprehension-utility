/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.download;


/**
 * Object representing the result of a download, signifying success or not.
 * <p>
 * Created by karanikasg@gmail.com.
 */
public class DownloadResult {
    private final String content;
    private final boolean success;
    private final String errorMessage;

    private DownloadResult(String content, boolean success, String errorMessage) {
        this.content = content;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static DownloadResult success(String content) {
        return new DownloadResult(content, true, "");
    }

    public static DownloadResult fail(String errorMessage) {
        return new DownloadResult("", false, errorMessage);
    }

    public String getContent() {
        return content;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
