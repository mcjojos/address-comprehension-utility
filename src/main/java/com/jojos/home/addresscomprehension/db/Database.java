/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.db;

import com.jojos.home.addresscomprehension.ApplicationException;
import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * All the database related operations go here
 *
 * Created by karanikasg@gmail.com.
 */
public class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private static final String DERBY_URL_PREFIX = "jdbc:derby:";
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String dbName;
    private final Connection dbConnection;
    private final String dbShutDownURL;

    private final Map<Integer, Company> cachedCompanies = new HashMap<>();
    private final Map<Integer, Address> cachedAddresses = new HashMap<>();

    public Database(String dbName) throws SQLException, MalformedURLException, ApplicationException {
        this.dbName = dbName;
        String connectionURL = DERBY_URL_PREFIX + dbName + ";create=true";

        // jdbc4 states that we no longer need to call Class.forName() to register the driver
        dbConnection = DriverManager.getConnection(connectionURL);
        dbShutDownURL = DERBY_URL_PREFIX + ";shutdown=true";
        log.info("Connected to database {}", dbName);

        // first create the tables
        createTablesIfNotExist();

        // then cache the companies. It's ok to do that for now.
        // If that becomes a problem over time rethink the implementation.
        populateCompanies();
        populateAddresses();
    }

    /**
     * Compare just the URL and name of the cachedCompanies against the passed argument
     * @param company the object to compare against
     * @return true if there is at least on entry with the same URL and name as the passed argument, false otherwise.
     */
    private boolean cachedCompaniesContainCompanyWithoutAddressCount(Company company) {
        for (HashMap.Entry<Integer, Company> entry : cachedCompanies.entrySet()) {
            if (entry.getValue().getUrlStr().equals(company.getUrlStr()) &&
                    entry.getValue().getName().equals(company.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Insert a collection of companies into the database.
     * If the company in question if found with a different AddressCount perform an update operation instead.
     * @param companies a Collection of presumably unique companies.
     * @throws SQLException if something went wrong and the database could not perform the insert/update operation.
     */
    public void insertCompanies(Collection<Company> companies) throws SQLException, MalformedURLException {
        for (Company company : companies) {
            if (!cachedCompaniesContainCompanyWithoutAddressCount(company)) {
                PreparedStatement statement = dbConnection.prepareStatement(
                        "INSERT INTO Companies (URL, NAME, ADDRESS_COUNT) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.closeOnCompletion();
                statement.setString(1, company.getUrlStr());
                statement.setString(2, company.getName().orElse(""));
                statement.setInt(3, company.getAddressCount());
                statement.execute();

                log.info("Inserting {}", company.toString());
                // fetch the auto-increment value we just created
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        cacheCompany(id, company);
                    } else {
                        throw new SQLException("Expected generated keys after inserting a company but got none");
                    }
                }
            } else {
                Optional<Integer> id = getIdForCompanyWithDifferentAddressCount(company);
                if (id.isPresent()) { // this is an indication to update
                    PreparedStatement statement = dbConnection.prepareStatement(
                            "UPDATE Companies SET URL=?, NAME=?, ADDRESS_COUNT=? WHERE ID=?");
                    statement.closeOnCompletion();
                    statement.setString(1, company.getUrlStr());
                    statement.setString(2, company.getName().orElse(""));
                    statement.setInt(3, company.getAddressCount());
                    statement.setInt(4, id.get());
                    statement.execute();

                    log.info("Updating  {}", company.toString());

                    cacheCompany(id.get(), company);
                }
            }
        }
    }

    /**
     * Get all companies stored in database.
     * @return a map of IDs-Company
     * @throws SQLException if retrieval went wrong
     */
    public Map<Integer, Company> retrieveCompanies() throws SQLException, MalformedURLException {
        Statement statement = dbConnection.createStatement();
        statement.closeOnCompletion();

        // it's OK to preserve order for now
        Map<Integer, Company> companies = new LinkedHashMap<>();
        try (ResultSet rs = statement.executeQuery("SELECT ID, URL, NAME, ADDRESS_COUNT FROM COMPANIES")) {

            while (rs.next()) {
                int id = rs.getInt("ID");
                String url = rs.getString("URL");
                String name = rs.getString("NAME");
                int addressCount = rs.getInt("ADDRESS_COUNT");
                Optional<String> optionalName = "".equals(name) ? Optional.empty() : Optional.of(name);
                companies.put(id, new Company(url, optionalName, addressCount));
            }
        }
        return companies;
    }

    /**
     * Get a singe company based on ID.
     * @return a map of IDs-Company
     * @throws SQLException if retrieval went wrong
     */
    public Optional<Company> retrieveCompany(int id) throws SQLException, MalformedURLException {
        PreparedStatement statement = dbConnection.prepareStatement(
                "SELECT ID, URL, NAME, ADDRESS_COUNT FROM COMPANIES WHERE ID = ?");
        statement.setInt(1, id);
        statement.closeOnCompletion();

        Optional<Company> company = Optional.empty();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String url = rs.getString("URL");
                String name = rs.getString("NAME");
                int addressCount = rs.getInt("ADDRESS_COUNT");
                Optional<String> optionalName = "".equals(name) ? Optional.empty() : Optional.of(name);
                company = Optional.of(new Company(url, optionalName, addressCount));
            }
        }
        return company;
    }

    /**
     * Retrieve all addresses stored in database.
     * @return a map of IDs-Address
     * @throws SQLException if retrieval went wrong
     */
    public Map<Integer, Address> retrieveAddresses() throws SQLException, ApplicationException, MalformedURLException {
        Statement statement = dbConnection.createStatement();
        statement.closeOnCompletion();

        // it's OK to preserve order for now
        Map<Integer, Address> addresses = new LinkedHashMap<>();
        try (ResultSet rs = statement.executeQuery("SELECT ID, ADDRESS, IMPORT_TIMESTAMP, COMPANY_ID, ORDINAL FROM ADDRESSES")) {
            while (rs.next()) {
                int id = rs.getInt("ID");
                String address = rs.getString("ADDRESS");
                LocalDateTime dateTime = rs.getTimestamp("IMPORT_TIMESTAMP").toLocalDateTime();
                int companyId = rs.getInt("COMPANY_ID");
                int ordinal = rs.getInt("ORDINAL");
                Optional<Company> company = retrieveCompany(companyId);
                // extra validation step which normally shouldn't be required
                if (company.isPresent()) {
                    if (ordinal > company.get().getAddressCount()) {
                        String errorMsg = "Can't  possibly define an address ordinal with a value bigger than it's company's address_count";
                        log.error(errorMsg);
                        throw new ApplicationException(errorMsg);
                    }
                    addresses.put(id, new Address(address, dateTime, company.get(), ordinal));
                }
            }
        }
        return addresses;
    }

    /**
     * Retrieve the latest addresses for all companies
     * @return a list of the latest addresses for each company
     */
    public Map<Integer, Address> retrieveLatestAddresses()
            throws SQLException,ApplicationException, MalformedURLException {
        // preserve the retrieve order
        Map<Integer, Address> addresses = new LinkedHashMap<>();

        Statement statement = dbConnection.createStatement();
        statement.closeOnCompletion();
//        try (ResultSet rs = statement.executeQuery("select ID, ADDRESS, IMPORT_TIMESTAMP, COMPANY_ID, ORDINAL " +
//                "from ADDRESSES where ID IN " +
//                "(select MAX(ID) AS MAX_ID from ADDRESSES GROUP BY COMPANY_ID, ORDINAL ORDER BY COMPANY_ID, ORDINAL)")) {
        try (ResultSet rs = statement.executeQuery("select A.ID, A.ADDRESS, A.IMPORT_TIMESTAMP, A.COMPANY_ID, A.ORDINAL " +
                "from ADDRESSES A INNER JOIN COMPANIES C ON C.ID=A.COMPANY_ID " +
                "where A.ORDINAL<=C.ADDRESS_COUNT AND A.ID IN " +
                "(select MAX(ID) AS MAX_ID from ADDRESSES GROUP BY COMPANY_ID, ORDINAL ORDER BY COMPANY_ID, ORDINAL) " +
                "ORDER BY A.COMPANY_ID, A.ORDINAL")) {
            while (rs.next()) {
                int id = rs.getInt("ID");
                String address = rs.getString("ADDRESS");
                LocalDateTime dateTime = rs.getTimestamp("IMPORT_TIMESTAMP").toLocalDateTime();
                int companyId = rs.getInt("COMPANY_ID");
                int ordinal = rs.getInt("ORDINAL");
                Optional<Company> company = retrieveCompany(companyId);
                // extra validation step which normally shouldn't be required
                if (company.isPresent()) {
                    if (ordinal > company.get().getAddressCount()) {
                        String errorMsg = String.format("Can't possibly define an address ordinal " +
                                "with a value bigger than it's company's address_count. %s -- %s", address, company);
                        log.error(errorMsg);
                        throw new ApplicationException(errorMsg);
                    }
                    addresses.put(id, new Address(address, dateTime, company.get(), ordinal));
                }
            }
        }
        return addresses;
    }

    /**
     * Search for a company in the collection of cached companies that contains a company with the same url and name
     * but differ only on their AddressCount. Used to signify an update.
     * @param company the object to compare against
     * @return An optional int that points to the correct company id that actually is non empty if there is
     * at least on entry with the same URL and name as the passed argument AND different AddressCount
     * (signifying an update operation). It will return an {@link Optional#empty()} otherwise.
     */
    private Optional<Integer> getIdForCompanyWithDifferentAddressCount(Company company)
            throws SQLException, MalformedURLException {
        Optional<Integer> companyId = Optional.empty();

        // first check the cached companies
        for (HashMap.Entry<Integer, Company> entry : cachedCompanies.entrySet()) {
            if (entry.getValue().getUrlStr().equals(company.getUrlStr()) &&
                    entry.getValue().getName().equals(company.getName()) &&
                    entry.getValue().getAddressCount() != company.getAddressCount()) {
                companyId = Optional.of(entry.getKey());
            }
        }

        // only in case we haven't found locally the company id then search in the database
        if (!companyId.isPresent()) {
            Map<Integer, Company> retrievedCompanies = retrieveCompanies();
            for (Map.Entry<Integer, Company> entry : retrievedCompanies.entrySet()) {
                if (entry.getValue().getUrlStr().equals(company.getUrlStr()) &&
                        entry.getValue().getName().equals(company.getName()) &&
                        entry.getValue().getAddressCount() != company.getAddressCount()) {
                    companyId = Optional.of(entry.getKey());
                }
            }
        }
        return companyId;
    }

    /**
     * Search for an address in the collection of cached addresses that contains an address with the same value
     * but differ on their timestamp. Used to signify an update.
     * @param address the object to compare against
     * @return An optional int that points to the correct address id that actually is non empty if there is
     * at least one entry with the same value AND different timestamp
     * (signifying an update operation). It will return an {@link Optional#empty()} otherwise.
     */
    private Optional<Integer> getIdForAddress(Address address)
            throws SQLException, MalformedURLException {
        Optional<Integer> addressId = Optional.empty();

        // first check the cached companies
        for (HashMap.Entry<Integer, Address> entry : cachedAddresses.entrySet()) {
            if (entry.getValue().getValue().equals(address.getValue())) {
                addressId = Optional.of(entry.getKey());
            }
        }

        // only in case we haven't found locally the address id then search in the database
        if (!addressId.isPresent()) {
            try {
                Map<Integer, Address> retrievedAddresses = retrieveAddresses();
                for (Map.Entry<Integer, Address> entry : retrievedAddresses.entrySet()) {
                    if (entry.getValue().getValue().equals(address.getValue())) {
                        addressId = Optional.of(entry.getKey());
                    }
                }
            } catch (ApplicationException e) {
                log.info("no addressed retrieved");
            }
        }
        return addressId;
    }

    private Optional<Integer> getIdForCompany(Company company) throws SQLException, MalformedURLException {
        Optional<Integer> companyId = Optional.empty();

        // first check the cached companies
        for (Map.Entry<Integer, Company> entry : cachedCompanies.entrySet()) {
            if (company.equals(entry.getValue())) {
                companyId = Optional.of(entry.getKey());
            }
        }

        // only in case we haven't found locally the company id then search in the database
        if (!companyId.isPresent()) {
            Map<Integer, Company> retrievedCompanies = retrieveCompanies();
            for (Map.Entry<Integer, Company> entry : retrievedCompanies.entrySet()) {
                if (company.equals(entry.getValue())) {
                    companyId = Optional.of(entry.getKey());
                }
            }
        }
        return companyId;
    }

    /**
     * Always insert a new record for an address keep old records.
     * Our select statement makes sure we fetch the right ones each time.
     * This method MUST be synchronized as it's accessed from different threads.
     * @param addresses a collection of addresses
     * @return true if at least one address was updated, false otherwise
     * @throws SQLException
     * @throws ApplicationException
     */
    public synchronized boolean insertAddresses(Collection<Address> addresses)
            throws SQLException, ApplicationException, MalformedURLException {
        boolean inserted = false;
        for (Address address : addresses) {
            Optional<Integer> companyId = getIdForCompany(address.getCompany());

            // update/insert the company in case not found
            if (!companyId.isPresent()) {
                log.info("Updating the company for address {}", address.toString());
                insertCompanies(Collections.singleton(address.getCompany()));

                companyId = getIdForCompany(address.getCompany());
                if (!companyId.isPresent()) {
                    log.error("No companies found for {}" + address.toString());
                    continue;
                }
            }

            // if there is a company make a sanity check in regards to the ordinal-address count relation
            if (address.getOrdinal() > address.getCompany().getAddressCount()) {
                String errorMsg = "Can't possibly define an address ordinal with a value bigger than it's company's address_count";
                log.error(errorMsg);
                throw new ApplicationException(errorMsg);
            }

            if (!getIdForAddress(address).isPresent()) {
                PreparedStatement statement = dbConnection.prepareStatement(
                        "INSERT INTO ADDRESSES (ADDRESS, IMPORT_TIMESTAMP, COMPANY_ID, ORDINAL) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.closeOnCompletion();
                statement.setString(1, address.getValue());
                statement.setTimestamp(2, Timestamp.valueOf(address.getDateTime()));
                statement.setInt(3, companyId.get());
                statement.setInt(4, address.getOrdinal());
                statement.execute();

                inserted = true;
                log.info("Inserting {}", address.toString());
                // fetch the auto-increment value we just created
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        cacheAddress(id, address);
                    } else {
                        throw new SQLException("Expected generated keys after inserting an address but got none");
                    }
                }
            }
        }
        return inserted;
    }

    private void createTablesIfNotExist() throws SQLException {
        DatabaseMetaData dbMetadata = dbConnection.getMetaData();
        try (ResultSet rs = dbMetadata.getTables(null, "APP", "COMPANIES", null)) {
            if (!rs.next()) {
                createCompaniesTable();
            } else {
                log.info("COMPANIES table exist");
            }
        }

        try (ResultSet rs = dbMetadata.getTables(null, "APP", "ADDRESSES", null)) {
            if (!rs.next()) {
                createAddressesTable();
            } else {
                log.info("ADDRESSES table exist");
            }
        }
    }

    private void createCompaniesTable() throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.closeOnCompletion();

        String createStmt = "CREATE TABLE COMPANIES (" +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "URL VARCHAR(2083) NOT NULL," +
                "NAME VARCHAR(50)," +
                "ADDRESS_COUNT INTEGER NOT NULL DEFAULT 1)";
        String alterStmt =  "ALTER TABLE COMPANIES ADD CONSTRAINT COMPANIES_PK Primary Key (ID)";

        statement.execute(createStmt);
        statement.execute(alterStmt);

        log.info("COMPANIES table created");
    }

    private void createAddressesTable() throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.closeOnCompletion();

        String createStmtAddresses = "CREATE TABLE ADDRESSES(" +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "ADDRESS VARCHAR(1000) NOT NULL," +
                "IMPORT_TIMESTAMP TIMESTAMP NOT NULL," +
                "COMPANY_ID INTEGER NOT NULL," +
                "ORDINAL INTEGER NOT NULL DEFAULT 1)";
        String alterStmtAddresses1 =  "ALTER TABLE ADDRESSES ADD CONSTRAINT ADDRESSES_PK Primary Key (ID)";
        String alterStmtAddresses2 =  "ALTER TABLE ADDRESSES " +
                "ADD CONSTRAINT COMPANIES_FK1 Foreign Key (COMPANY_ID) REFERENCES COMPANIES (ID)";


        statement.execute(createStmtAddresses);
        statement.execute(alterStmtAddresses1);
        statement.execute(alterStmtAddresses2);

        log.info("ADDRESSES table created");
    }

    private void populateCompanies() throws SQLException, MalformedURLException {
        Map<Integer, Company> companiesStored = retrieveCompanies();
        companiesStored.forEach(this::cacheCompany);
    }

    private void populateAddresses() throws SQLException, ApplicationException, MalformedURLException {
        Map<Integer, Address> addressesStored = retrieveLatestAddresses();
        addressesStored.forEach(this::cacheAddress);
    }

    private void cacheCompany(Integer id, Company company) {
        cachedCompanies.put(id, company);
    }

    private void cacheAddress(Integer id, Address address) {
        cachedAddresses.put(id, address);
    }

    public final void shutdown() {
        try {
            Connection connection = DriverManager.getConnection(dbShutDownURL);
            log.error("Could get a connection to the database even though the statement should have closed it");
            close(connection);
        } catch (SQLException e) {
            if (!checkIfDBShutdownProperly(e)) {
                log.error("Couldn't shut down DB", e);
            }
        } catch (Throwable e) {
            log.error("Unknown error encountered when shutting down database", e);
        }
    }

    public boolean deleteDatabase() {
        boolean success = deleteDirectory(new File(dbName));
        if (success) {
            log.info("{} successfully deleted", dbName);
        } else {
            log.warn("Unable to delete {}", dbName);
        }
        return success;
    }

    static boolean deleteDirectory(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            for (int i = 0; i < (files != null ? files.length : 0); i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    if (files[i].delete()) {
                        log.info("File '{}' deleted.", files[i].getName());
                    }
                }
            }
        }
        return file.delete();
    }

    private boolean checkIfDBShutdownProperly(SQLException ex) {
        if (ex.getErrorCode() == 50000 && "XJ015".equals(ex.getSQLState())) {
            log.info("DB shutdown properly");
            return true;
        }
        return false;
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Cannot close the connection", e);
        }
    }

}
