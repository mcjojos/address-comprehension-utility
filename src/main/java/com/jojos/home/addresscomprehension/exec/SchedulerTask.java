/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.exec;

import com.jojos.home.addresscomprehension.ApplicationException;
import com.jojos.home.addresscomprehension.db.Database;
import com.jojos.home.addresscomprehension.download.DefaultDownloader;
import com.jojos.home.addresscomprehension.download.DefaultDownloaderCtx;
import com.jojos.home.addresscomprehension.download.DownloadResult;
import com.jojos.home.addresscomprehension.download.Downloader;
import com.jojos.home.addresscomprehension.email.Email;
import com.jojos.home.addresscomprehension.parse.Parser;
import com.jojos.home.addresscomprehension.util.Util;
import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * A runnable class representing the elements of execution.
 * Downloading, parsing and storing are defined here
 *
 * Created by karanikasg@gmail.com.
 */
public class SchedulerTask implements Runnable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Company company;
    private final File downloadDirectory;
    private final Database database;
    private final Email email;

    private SchedulerTask(Company company, File downloadDirectory, Database database, Email email) {
        this.company = company;
        this.downloadDirectory = downloadDirectory;
        this.database = database;
        this.email = email;
    }

    public static SchedulerTask of(Company company, File downloadDirectory, Database database, Email email) {
        return new SchedulerTask(company, downloadDirectory, database, email);
    }

    @Override
    public void run() {
        log.info("running for {} ", company);
        DefaultDownloaderCtx downloaderCtx = null;
        try {
            // todo: perhaps implement a way to register a downloader to a specific company

            // download first
            downloaderCtx = new DefaultDownloaderCtx(downloadDirectory, company.getUrl());
            Downloader<DefaultDownloaderCtx, DownloadResult> downloader = DefaultDownloader.instance;
            DownloadResult downloadResult = downloader.download(downloaderCtx);

            // then extract the content if downloading succeeded
            if (downloadResult.isSuccess()) {
                List<Parser> registeredParsers = company.getRegisteredParsers();

                for (Parser parser : registeredParsers) {
                    try {
                        List<Address> addresses = parser.extractAddressesFromFile(downloaderCtx.getDownloadFile(), company, Optional.empty());

                        if (addresses != null && !addresses.isEmpty()) {
                            try {
                                // insert the addresses. the underlying code will make sure that the companies are also updated if needed
                                if (database.insertAddresses(addresses)) {
                                    email.dispatch(addresses);
                                } else {
                                    log.info("No address update was necessary for " + Util.toString(addresses, Address::getValue));
                                }
                            } catch (SQLException | ApplicationException | MalformedURLException e) {
                                log.error("Cannot save {} in the database.", Util.toString(addresses, Address::toString));
                                e.printStackTrace();
                            }

                        }
                    } catch (IOException e) {
                        log.error("Error while extracting address from file {}", downloaderCtx.getDownloadFile());
                    }
                }

            } else {    // nothing to extract/parse on download failure. log the error and move on
                log.error("Downloading {} failed with reason {}",
                        company.toString(), downloadResult.getErrorMessage());
            }

        } catch (Exception e) {
            //our last resort exception catch
            log.error("something fucked up");
            e.printStackTrace();
        } finally {
            if (downloaderCtx != null && !company.isRetainDownloadData()) {
                downloaderCtx.cleanUp();
            }
        }
    }

    public Company getCompany() {
        return company;
    }
}
