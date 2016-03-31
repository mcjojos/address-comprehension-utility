/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.exec;

import com.jojos.home.addresscomprehension.ApplicationException;
import com.jojos.home.addresscomprehension.db.Database;
import com.jojos.home.addresscomprehension.email.Email;
import com.jojos.home.addresscomprehension.util.Util;
import com.jojos.home.addresscomprehension.values.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * The class responsible for the heavy lifting of the application.
 * Tasks like scheduling the download, parsing and storing all information relevant to addresses and companies
 *
 * Created by karanikasg@gmail.com.
 */
public class Engine {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<SchedulerTask> tasks;
    private final Database database;
    private final Scheduler<SchedulerTask> scheduler;
    private final Email email;

    public Engine(Set<Company> companies,
                  Database database,
                  Email email,
                  Optional<LocalTime> schedulerRunTime,
                  long periodInSeconds,
                  File downloadDirectory,
                  int threadCount) throws ApplicationException {
        this.database = database;
        this.tasks = createTasks(companies, downloadDirectory);
        this.scheduler = new Scheduler<>(schedulerRunTime, periodInSeconds, threadCount);
        this.email = email;
    }

    private Set<SchedulerTask> createTasks(Set<Company> companies, File downloadDirectory) throws ApplicationException {
        try {
            database.insertCompanies(companies);
        } catch (SQLException | MalformedURLException e) {
            String errorMsg = String.format("Cannot save %s in the database.", Util.toString(companies, Company::toString));
            log.error(errorMsg);
            e.printStackTrace();
            throw new ApplicationException(errorMsg, e);
        }

        Set<SchedulerTask> tasksWrappers = new HashSet<>();
        for (Company company : companies) {
            tasksWrappers.add(SchedulerTask.of(company, downloadDirectory, database, email));
        }
        return tasksWrappers;
    }

    public void start() {
        scheduler.submit(tasks);
    }

    public void shutdown() {
        scheduler.stop();
    }

}
