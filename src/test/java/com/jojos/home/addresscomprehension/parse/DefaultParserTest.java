/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.parse;

import com.jojos.home.addresscomprehension.download.DefaultDownloader;
import com.jojos.home.addresscomprehension.download.DefaultDownloaderCtx;
import com.jojos.home.addresscomprehension.download.DownloadResult;
import com.jojos.home.addresscomprehension.download.Downloader;
import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Test class for {@link Parser}'s default implementation {@link DefaultParser}.
 * <p>
 * Created by karanikasg@gmail.com.
 */
public class DefaultParserTest {

    private static File downloadDirectory;
    @BeforeClass
    public static void setUp() {
        downloadDirectory = new File("download");
    }

    @Test
    public void testExtractSingleLineStrIdealo() throws IOException {
        // parse, extract the addresses and check if everything went fine
        Parser parser = new DefaultParser();

        Company company = new Company("http://www.idealo.de/preisvergleich/AGB.html", Optional.of("idealo"), false);

        String addressSingleLine = "Ritterstraße 11 10969 Berlin, Deutschland";
        List<Address> addresses = parser.extractAddressesFromString(addressSingleLine, company, Optional.<ParserCtx>empty());
        Assert.assertEquals(1, addresses.size());
        Assert.assertEquals("Ritterstraße 11 10969 Berlin, Deutschland", addresses.get(0).getValue());
    }

    @Test
    public void testExtractMultiLineStrIdealo() throws IOException {
        // parse, extract the addresses and check if everything went fine
        Parser parser = new DefaultParser();

        Company company = new Company("http://www.idealo.de/preisvergleich/AGB.html", Optional.of("idealo"), false);

        String addressMultipleLines = "Ritterstraße 11<br>\n" +
                "10969 Berlin, Deutschland";
        List<Address> addresses = parser.extractAddressesFromString(addressMultipleLines, company, Optional.<ParserCtx>empty());
        Assert.assertEquals(1, addresses.size());
        Assert.assertEquals("Ritterstraße 11  10969 Berlin, Deutschland", addresses.get(0).getValue());
    }

    @Test
    public void testExtractFileIdealo() throws IOException {
        String companyUrl = "http://www.idealo.de/preisvergleich/AGB.html";
        URL url = new URL(companyUrl);
        DefaultDownloaderCtx downloadCtx = new DefaultDownloaderCtx(downloadDirectory, url);

        Downloader<DefaultDownloaderCtx, DownloadResult> downloader = DefaultDownloader.instance;
        DownloadResult downloadResult = downloader.download(downloadCtx);

        Assert.assertTrue("Download result must be success", downloadResult.isSuccess());
        Assert.assertTrue("Download content must not be empty", !downloadResult.getContent().isEmpty());
        Assert.assertTrue("Download error message must be empty", downloadResult.getErrorMessage().isEmpty());
        Assert.assertTrue("File does not exist", downloadCtx.getDownloadFile().exists());
        Assert.assertTrue("File contains no data", (downloadCtx.getDownloadFile().length() != 0));

        // parse, extract the addresses and check if everything went fine
        Parser parser = new DefaultParser();

        Company company = new Company(companyUrl, Optional.of("idealo"), false);

        List<Address> addresses = parser.extractAddressesFromFile(downloadCtx.getDownloadFile(), company, Optional.<ParserCtx>empty());
        Assert.assertEquals(1, addresses.size());
        Assert.assertEquals("Ritterstraße 11 10969 Berlin, Deutschland", addresses.get(0).getValue());

        downloadCtx.cleanUp();
        Assert.assertTrue("File exists", !downloadCtx.getDownloadFile().exists());
    }

    @Test
    public void testExtractFileRegis24() throws IOException {
        String companyUrl = "http://www.regis24.de/impressum/";
        URL url = new URL(companyUrl);
        DefaultDownloaderCtx downloadCtx = new DefaultDownloaderCtx(downloadDirectory, url);

        Downloader<DefaultDownloaderCtx, DownloadResult> downloader = DefaultDownloader.instance;
        DownloadResult downloadResult = downloader.download(downloadCtx);

        Assert.assertTrue("Download result must be success", downloadResult.isSuccess());
        Assert.assertTrue("Download content must not be empty", !downloadResult.getContent().isEmpty());
        Assert.assertTrue("Download error message must be empty", downloadResult.getErrorMessage().isEmpty());
        Assert.assertTrue("File does not exist", downloadCtx.getDownloadFile().exists());
        Assert.assertTrue("File contains no data", (downloadCtx.getDownloadFile().length() != 0));

        // parse, extract the addresses and check if everything went fine
        Parser parser = new DefaultParser();

        Company company = new Company(companyUrl, Optional.of("Regis 24"), false);

        List<Address> addresses = parser.extractAddressesFromFile(downloadCtx.getDownloadFile(), company, Optional.<ParserCtx>empty());
        Assert.assertEquals(1, addresses.size());
        Assert.assertEquals("Wallstraße 58 10179 Berlin", addresses.get(0).getValue());

        downloadCtx.cleanUp();
        Assert.assertTrue("File exists", !downloadCtx.getDownloadFile().exists());
    }

    @Test
    public void testExtractFileSavageWear() throws IOException {
        String companyUrl = "https://www.savage-wear.com/de/content/6_impressum";
        URL url = new URL(companyUrl);
        DefaultDownloaderCtx downloadCtx = new DefaultDownloaderCtx(downloadDirectory, url);

        Downloader<DefaultDownloaderCtx, DownloadResult> downloader = DefaultDownloader.instance;
        DownloadResult downloadResult = downloader.download(downloadCtx);

        Assert.assertTrue("Download result must be success", downloadResult.isSuccess());
        Assert.assertTrue("Download content must not be empty", !downloadResult.getContent().isEmpty());
        Assert.assertTrue("Download error message must be empty", downloadResult.getErrorMessage().isEmpty());
        Assert.assertTrue("File does not exist", downloadCtx.getDownloadFile().exists());
        Assert.assertTrue("File contains no data", (downloadCtx.getDownloadFile().length() != 0));

        // parse, extract the addresses and check if everything went fine
        Parser parser = new DefaultParser();

        Company company = new Company(companyUrl, Optional.of("Savage Wear"), false);

        List<Address> addresses = parser.extractAddressesFromFile(downloadCtx.getDownloadFile(), company, Optional.<ParserCtx>empty());
        Assert.assertEquals(2, addresses.size());
        Assert.assertEquals("Gubener Straße 29 10243 Berlin", addresses.get(0).getValue());
        Assert.assertEquals("Grünberger Straße 16 10243 berlin", addresses.get(1).getValue());

        downloadCtx.cleanUp();
        Assert.assertTrue("File exists", !downloadCtx.getDownloadFile().exists());
    }

//    @Test
    public void testExtractFilePowerflasher() throws IOException {
        String companyUrl = "http://www.powerflasher.de/#/de/kontakt";
        URL url = new URL(companyUrl);
        DefaultDownloaderCtx downloadCtx = new DefaultDownloaderCtx(downloadDirectory, url);

        Downloader<DefaultDownloaderCtx, DownloadResult> downloader = DefaultDownloader.instance;
        DownloadResult downloadResult = downloader.download(downloadCtx);

        Assert.assertTrue("Download result must be success", downloadResult.isSuccess());
        Assert.assertTrue("Download content must not be empty", !downloadResult.getContent().isEmpty());
        Assert.assertTrue("Download error message must be empty", downloadResult.getErrorMessage().isEmpty());
        Assert.assertTrue("File does not exist", downloadCtx.getDownloadFile().exists());
        Assert.assertTrue("File contains no data", (downloadCtx.getDownloadFile().length() != 0));

        // parse, extract the addresses and check if everything went fine
        Parser parser = new DefaultParser();

        Company company = new Company(companyUrl, Optional.of("Power Flasher"), false);

        List<Address> addresses = parser.extractAddressesFromFile(downloadCtx.getDownloadFile(), company, Optional.<ParserCtx>empty());
        Assert.assertEquals(1, addresses.size());

        downloadCtx.cleanUp();
        Assert.assertTrue("File exists", !downloadCtx.getDownloadFile().exists());
    }

    @Test
    public void testNormalize() throws IOException {
        URL url = new URL("http://www.idealo.de/preisvergleich/AGB.html");
        DefaultDownloaderCtx downloadCtx = new DefaultDownloaderCtx(downloadDirectory, url);

        Downloader<DefaultDownloaderCtx, DownloadResult> downloader = DefaultDownloader.instance;
        DownloadResult downloadResult = downloader.download(downloadCtx);

        Assert.assertTrue("Download result must be success", downloadResult.isSuccess());
        Assert.assertTrue("Download content must not be empty", !downloadResult.getContent().isEmpty());
        Assert.assertTrue("Download error message must be empty", downloadResult.getErrorMessage().isEmpty());
        Assert.assertTrue("File does not exist", downloadCtx.getDownloadFile().exists());
        Assert.assertTrue("File contains no data", (downloadCtx.getDownloadFile().length() != 0));

        DefaultParser parser = new DefaultParser();
        String normalizedContent = parser.normalize(downloadResult.getContent());

        Assert.assertTrue(!normalizedContent.contains("\\n"));
        Assert.assertTrue(!normalizedContent.contains("\\r"));
        Assert.assertTrue(!normalizedContent.contains("\\r\\n"));

        downloadCtx.cleanUp();
        Assert.assertTrue("File exists", !downloadCtx.getDownloadFile().exists());
    }


    @AfterClass
    public static void cleanUp() {
        downloadDirectory.delete();
    }

}
