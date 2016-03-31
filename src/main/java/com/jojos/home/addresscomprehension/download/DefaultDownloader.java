/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.download;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * The default downloader is all we got for now.
 * You'll have to live with it.
 *
 * Created by karanikasg@gmail.com.
 */
public class DefaultDownloader implements Downloader<DefaultDownloaderCtx, DownloadResult> {

    public static final DefaultDownloader instance = new DefaultDownloader();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DefaultDownloader() {}
    
    @Override
    public DownloadResult download(DefaultDownloaderCtx context) {
        URL url = context.getUrl();
        log.info("Attempt to download '{}'", url);

        // start optimistically
        boolean success = true;
        String content = "";
        String errorMessage = "";

        InputStream in = null;
        try {
            in = url.openStream();

            content = IOUtils.toString(in);
            FileUtils.writeStringToFile(
                    context.getDownloadFile(),
                    content,
                    StandardCharsets.UTF_8,
                    false);

        } catch (IOException e) {
            log.error("Failed to download '{}'", url);
            success = false;
            errorMessage = e.getMessage();
        } finally {
            IOUtils.closeQuietly(in);
        }

        if (success) {
            return DownloadResult.success(content);
        } else {
            return DownloadResult.fail(errorMessage);
        }

    }

}
