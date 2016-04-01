/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.db;

import com.jojos.home.addresscomprehension.ApplicationException;
import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jojos.home.addresscomprehension.db.DatabaseTestHelper.CompaniesAndAddresses;

/**
 * Test class for Database class
 * In this unit test order matters!
 * The order in which the tests are executed are important, ie the companies must be created before the addresses.
 * The pattern followed is test1XXX, test2XXX, etc
 * Created by karanikasg@gmail.com.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseTest {
    private final static String dbName = "TestDB";
    private static Database database;

    @BeforeClass
    public static void setUp() throws ApplicationException, SQLException, MalformedURLException {
        // ensure we start with a clean database
        Database.deleteDirectory(new File(dbName));

        database = new Database(dbName);
    }

    @Test
    public void test1InsertAndRetrieveCompaniesAndAddresses() throws MalformedURLException {
        CompaniesAndAddresses companiesAndAddresses = DatabaseTestHelper.mockCompaniesAndAddresses();
        List<Company> companies = companiesAndAddresses.getCompanies();
        List<Address> addresses = companiesAndAddresses.getAddresses();

        try {
            database.insertCompanies(companies);
            Map<Integer, Company> retrievedCompanies = database.retrieveCompanies();
            String retrievedCompaniesStr = retrievedCompanies.values().stream().map(Company::toString).collect(Collectors.joining("--"));
            String companiesStr = companies.stream().map(Company::toString).collect(Collectors.joining("--"));
            Assert.assertTrue("Companies stored and companies retrieved should match. Retrieved: " + retrievedCompaniesStr + ", companies: " + companiesStr,
                    retrievedCompanies.values().containsAll(companies));
            Assert.assertTrue("Companies count should be 4", retrievedCompanies.size() == 4);
        } catch (SQLException e) {
            Assert.fail(String.format("Unexpected SQL exception. Error code %d, sql state '%s', message '%s'",
                    e.getErrorCode(), e.getSQLState(), e.getMessage()));
        }

        try {
            database.insertAddresses(addresses);
            Map<Integer, Address> retrievedAddresses = database.retrieveLatestAddresses();
            Set<String> retrievedAddressesValues = retrievedAddresses.values().stream().map(Address::getValue).collect(Collectors.toSet());
            Set<String> addressesValues = addresses.stream().map(Address::getValue).collect(Collectors.toSet());
            String retrievedAddressesStr = retrievedAddresses.values().stream().map(Address::toString).collect(Collectors.joining("--"));
            String addressesStr = addresses.stream().map(Address::toString).collect(Collectors.joining("--"));
            Assert.assertTrue("Addresses stored and addresses retrieved should match. \n\n" +
                            "Retrieved: " + retrievedAddressesStr + ", \n\n" +
                            "addresses: " + addressesStr,
                    retrievedAddressesValues.containsAll(addressesValues));
            Assert.assertTrue("Addresses count should be 6", retrievedAddresses.size() == 6);
        } catch (SQLException e) {
            Assert.fail(String.format("Unexpected SQL exception. Error code %d, sql state '%s', message '%s'",
                    e.getErrorCode(), e.getSQLState(), e.getMessage()));
        } catch (ApplicationException e) {
            Assert.fail(String.format("Unexpected Application exception. Message '%s'", e.getMessage()));
        }
    }

    @Test
    public void test2InsertAndRetrieveCompaniesAndFewerAddresses() throws MalformedURLException {
        CompaniesAndAddresses companiesAndAddresses = DatabaseTestHelper.mockCompaniesAndFewerAddresses();
        List<Company> companies = companiesAndAddresses.getCompanies();
        List<Address> addresses = companiesAndAddresses.getAddresses();

        try {
            database.insertCompanies(companies);
            Map<Integer, Company> retrievedCompanies = database.retrieveCompanies();
            String retrievedCompaniesStr = retrievedCompanies.values().stream().map(Company::toString).collect(Collectors.joining("--"));
            String companiesStr = companies.stream().map(Company::toString).collect(Collectors.joining("--"));
            Assert.assertTrue("Companies count should be 4", retrievedCompanies.size() == 4);
            Assert.assertTrue("Companies stored and companies retrieved should match. Retrieved: " + retrievedCompaniesStr + ", companies: " + companiesStr,
                    retrievedCompanies.values().containsAll(companies));
        } catch (SQLException e) {
            Assert.fail(String.format("Unexpected SQL exception. Error code %d, sql state '%s', message '%s'",
                    e.getErrorCode(), e.getSQLState(), e.getMessage()));
        }

        try {
            database.insertAddresses(addresses);
            Map<Integer, Address> retrievedAddresses = database.retrieveLatestAddresses();
            Set<String> retrievedAddressesValues = retrievedAddresses.values().stream().map(Address::getValue).collect(Collectors.toSet());
            Set<String> addressesValues = addresses.stream().map(Address::getValue).collect(Collectors.toSet());
            String retrievedAddressesStr = retrievedAddresses.values().stream().map(Address::toString).collect(Collectors.joining("--"));
            String addressesStr = addresses.stream().map(Address::toString).collect(Collectors.joining("--"));
            Assert.assertTrue("Addresses count should be 4", retrievedAddresses.size() == 4);
            Assert.assertTrue("Addresses stored and addresses retrieved should match. \n\n" +
                            "Retrieved: " + retrievedAddressesStr + ", \n\n" +
                            "addresses: " + addressesStr,
                    retrievedAddressesValues.containsAll(addressesValues));
        } catch (SQLException e) {
            Assert.fail(String.format("Unexpected SQL exception. Error code %d, sql state '%s', message '%s'",
                    e.getErrorCode(), e.getSQLState(), e.getMessage()));
        } catch (ApplicationException e) {
            Assert.fail(String.format("Unexpected Application exception. Message '%s'", e.getMessage()));
        }
    }

    @Test
    public void test3InsertAndRetrieveCompaniesAndAddresses() throws MalformedURLException {
        test1InsertAndRetrieveCompaniesAndAddresses();
    }

    @Test
    public void test4InsertAndRetrieveCompaniesAndFewerAddresses() throws MalformedURLException {
        test2InsertAndRetrieveCompaniesAndFewerAddresses();
    }

    @AfterClass
    public static void cleanUp() {
        database.shutdown();
        // Comment this to check the contents of the database after each run.
        Assert.assertTrue(database.deleteDatabase());

        database = null;
    }
}