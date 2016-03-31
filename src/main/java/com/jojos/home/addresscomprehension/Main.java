/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension;

import com.jojos.home.addresscomprehension.email.Email;
import com.jojos.home.addresscomprehension.util.Util;
import com.jojos.home.addresscomprehension.values.Company;
import com.jojos.home.addresscomprehension.db.Database;
import com.jojos.home.addresscomprehension.exec.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Application's main entry point.
 *
 * Created by karanikasg@gmail.com.
 */
public class Main {

    private Logger log;
    private final String[] commandLineArguments;
    private final Properties properties= new Properties();
    private volatile Database database;
    private volatile Engine engine;

    private Main(String[] commandLineArguments) {
        this.commandLineArguments = commandLineArguments;
    }

    private void start() throws FileNotFoundException, SQLException, ApplicationException, MalformedURLException {
        loadConfigOrThrow();

        Optional<String> logDir = getOptionalProperty("logsDir");
        if (!logDir.isPresent()) {
            logDir = Optional.of("LOGS");
        }

        System.setProperty("LOGDIR", logDir.get());

        // NOTE: only here can we request a logger!
        // if we do it before, we won't have the environment variable
        // set to get the logs into the correct directory.
        log = LoggerFactory.getLogger(Main.class);

        final long applicationStart = System.currentTimeMillis();
        log.info("starting application");

        // instantiate the database
        String dbName = getProperty("db.name");
        this.database = new Database(dbName);

        // attach a shut down hook AFTER instantiating the database
        attachShutDownHook();

        // resolve the companies defined in the properties file and deal with the sites to download.
        Set<Company> companiesWithoutAddressCount = resolveCompanies();

        // get the time to run. if not specified start at the time the tool started
        Optional<LocalTime> optionalLocalTime = resolveTime();

        int engineThreads = resolveEngineThreads();

        File downloadDirectory = new File(getOptionalProperty("download.directory").orElse("."));
        if (!downloadDirectory.exists()) {
            boolean ok = downloadDirectory.mkdirs();
            if (!ok) {
                throw new IllegalStateException("Could not create report directory: " +
                        downloadDirectory.getAbsolutePath());
            }
            // clean after our shit
            downloadDirectory.deleteOnExit();
        }

        long periodInSeconds = resolvePeriodInSeconds();

        log.info("Running every {} seconds {} ", periodInSeconds,
                optionalLocalTime.isPresent() ? "at " + optionalLocalTime.get() : "starting now");

        Email email = new Email(properties);

        engine = new Engine(
                companiesWithoutAddressCount,
                database,
                email,
                optionalLocalTime,
                periodInSeconds,
                downloadDirectory,
                engineThreads);

        // log the duration before the threads kick in
        log.info(String.format("Application took %.2f seconds to start",
                (System.currentTimeMillis() - applicationStart) / 1000d));

        engine.start();

    }

    /**
     * Resolve the number of threads our {@link Engine} will use.
     * Don't use more than our cpu cores.
     * @return thread count to be used for the engine.
     */
    private int resolveEngineThreads() {
        int maxEngineThreads = Runtime.getRuntime().availableProcessors();
        Optional<String> optionalEngineThreads = getOptionalProperty("engine.threads");
        if (optionalEngineThreads.isPresent()) {
            try {
                int engineThreads = Integer.parseInt(optionalEngineThreads.get());
                if (engineThreads < maxEngineThreads) {
                    maxEngineThreads = engineThreads;
                }
            } catch (NumberFormatException e) {
                log.warn("Unparsable property for engine.threads: {}", optionalEngineThreads.get());
            }
        }
        log.info("Using {} threads", maxEngineThreads);
        return maxEngineThreads;
    }

    /**
     * Resolve the time from the properties file.
     * The format of the time can have one of the two formats:
     *
     * HH:mm:ss
     * or
     * HH:mm
     *
     * @return A {@link LocalTime} wrapped inside an {@link Optional}
     * An {@link Optional#empty()} if the time could not be parsed or the property was not found.
     *
     */
    private Optional<LocalTime> resolveTime() {
        Optional<LocalTime> optionalLocalTime = Optional.empty();
        Optional<String> runTime = getOptionalProperty("run.time");
        if (runTime.isPresent()) {
            optionalLocalTime = Util.parseOptionalLocalTime(runTime.get());
        }
        return optionalLocalTime;
    }

    private long resolvePeriodInSeconds() {
        long period = 24 * 60 * 60;
        Optional<String> runTime = getOptionalProperty("run.period_seconds");
        if (runTime.isPresent()) {
            period = Util.parseOptionalLong(runTime.get()).orElse(period);
        }
        return period;
    }

    /**
     * The companies are defined based on the syntax format of the following example
     *
     * <pre>
     * ## a comma-separated list of company aliases to be used further down.
     * companies=company1, company2
     *
     * companies.company1.name=Example Name 1
     * companies.company1.url=http://www.example1.com
     * companies.company1.parsers=com.example.parse.MockParser, com.example.parse.DefaultParser
     *
     * ## name and parsers are optional properties. The DefaultParser is always added in the list of parsers
     * companies.company2.url=http://www.example2.com
     * </pre>
     *
     * The method is parsing this syntax and produces a collection of companies out of it.
     *
     * @return A set of {@link Company} identified in the properties file
     * @throws ApplicationException if one of the mandatory properties is not found. {@code companies} and {@code companies.companyAlias.name}
     * @throws MalformedURLException if at least one of the URLs have an unresolved URL.
     */
    private Set<Company> resolveCompanies() throws ApplicationException, MalformedURLException {
        Set<Company> companiesWithoutAddressCount = new HashSet<>();
        String companies = getProperty("companies");
        String[] companyAliases = companies.split("\\s*,\\s*");
        for (String companyAlias : companyAliases) {
            Optional<String> name = getOptionalProperty("companies." + companyAlias + ".name");
            String url = getProperty("companies." + companyAlias + ".url");
            Optional<String> parsersStr = getOptionalProperty("companies." + companyAlias + ".parsers");
            boolean retainDownloadData = getOptionalBooleanProperty("companies." + companyAlias + ".retain_download_data", false);
            if (parsersStr.isPresent()) {
                companiesWithoutAddressCount.add(new Company(url, name, retainDownloadData, parsersStr.get().split("\\s*,\\s*")));
            } else {
                companiesWithoutAddressCount.add(new Company(url, name, retainDownloadData));
            }
        }

        return companiesWithoutAddressCount;
    }

    /**
     * Get an optional property
     * @param key the key of the property we are looking for
     * @return an {@link Optional#empty()} if the property is not found,
     * otherwise the value of the property wrapper in an {@link Optional}
     */
    private Optional<String> getOptionalProperty(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    /**
     * Get an optional boolean property
     * @param key the key in question
     * @param defaultValue the value to return if the key does not exist
     * @return true if the property value is true, TRUE or 1. False if the value exists but it's something else
     * return the defaultValue if the property does not exist.
     */
    private boolean getOptionalBooleanProperty(String key, boolean defaultValue) {
        Optional<String> optionalStr = Optional.ofNullable(properties.getProperty(key));
        if (optionalStr.isPresent()) {
            return "true".equalsIgnoreCase(optionalStr.get());
        } else {
            return defaultValue;
        }
    }

    /**
     * Get a mandatory property and throw an exception if the property is not found
     * @param key the key to look for
     * @return the value of the property in question
     * @throws ApplicationException when the mandatory property is not found
     */
    private String getProperty(String key) throws ApplicationException {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new ApplicationException(String.format("Property '%s' is mandatory", key));
        }
        return value;
    }

    /**
     * parse the command line usage of something like -init config.properties
     * @throws FileNotFoundException
     */
    private void loadConfigOrThrow() throws FileNotFoundException {
        String init = "config.properties";
        if (commandLineArguments != null) {
            for (int i = 0; i < commandLineArguments.length - 1; i++) {
                if (commandLineArguments[i] == null ||
                        commandLineArguments[i].length() == 0 ||
                        commandLineArguments[i].charAt(0) != '-' ||
                        commandLineArguments[i + 1] == null) {
                    continue;
                }

                String arg = commandLineArguments[i].substring(1);
                if (arg.equalsIgnoreCase("init")) {
                    init = commandLineArguments[i + 1];
                }
            }
        }
        if (init != null && !init.equals("")) {
            File file = new File(init);
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath() + " not found.");
            }
            try (InputStream input = new FileInputStream(init)) {
                properties.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void attachShutDownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (database != null) {
                    // every piece of instructions that JVM should execute before going down should be defined here
                    database.shutdown();
                }
                if (engine != null) {
                    engine.shutdown();
                }
            }
        });
    }

    public static void main(String[] args)
            throws FileNotFoundException, SQLException, ApplicationException, MalformedURLException {
        Main main = new Main(args);
        main.start();
    }

}
