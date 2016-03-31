/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.download;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Test class for {@link DefaultDownloader}.
 * <p>
 * Created by karanikasg@gmail.com.
 */
public class DefaultDownloaderTest {

    private static File downloadDirectory;

    @BeforeClass
    public static void setUp() {
        downloadDirectory = new File("download");
    }

    @Test
    public void testDownloadOneURL() throws IOException {
        URL url = new URL("http://www.google.com");
        DefaultDownloaderCtx downloadCtx = new DefaultDownloaderCtx(downloadDirectory, url);

        Downloader<DefaultDownloaderCtx, DownloadResult> downloader = DefaultDownloader.instance;
        DownloadResult downloadResult = downloader.download(downloadCtx);

        Assert.assertTrue("Download result must be success", downloadResult.isSuccess());
        Assert.assertTrue("Download content must not be empty", !downloadResult.getContent().isEmpty());
        Assert.assertTrue("Download error message must be empty", downloadResult.getErrorMessage().isEmpty());
        Assert.assertTrue("File does not exist", downloadCtx.getDownloadFile().exists());
        Assert.assertTrue("File contains no data", (downloadCtx.getDownloadFile().length() != 0));

        downloadCtx.cleanUp();
        Assert.assertTrue("File exists", !downloadCtx.getDownloadFile().exists());
    }


    @AfterClass
    public static void cleanUp() {
        downloadDirectory.delete();
    }

}
