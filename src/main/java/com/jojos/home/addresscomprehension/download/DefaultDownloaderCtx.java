/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * The context defined for the {@link DefaultDownloader}
 *
 * It's the responsibility of this class to clean the downloaded file by calling {@link #cleanUp()}
 * but it's the caller's responsibility to delete the download directory.
 *
 * Created by karanikasg@gmail.com.
 */
public class DefaultDownloaderCtx implements DownloaderCtx {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final File downloadFile;
    private final URL url;

    public DefaultDownloaderCtx(File downloadDirectory, URL url) {
        this.url = url;
        String downloadFileName = extractFileName(url);
        downloadFile = new File(downloadDirectory, downloadFileName);
    }

    // todo use a more sophisticated method of extraction int he future
    private String extractFileName(URL url) {
        return url.getHost() + ".html";
    }

    public File getDownloadFile() {
        return downloadFile;
    }

    public URL getUrl() {
        return url;
    }

    /**
     * Delete the download file created.
     */
    public void cleanUp() {
        if (downloadFile.delete()) {
            log.info("File '{}' deleted.", downloadFile.getName());
        }
    }
}
