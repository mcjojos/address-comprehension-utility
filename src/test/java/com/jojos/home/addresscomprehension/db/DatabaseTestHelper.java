/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.db;

import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Helper class tht provides static methods for companies and addresses creation
 *
 * Created by karanikasg@gmail.com.
 */
public class DatabaseTestHelper {

    static CompaniesAndAddresses mockCompaniesAndAddresses() throws MalformedURLException {
        List<Company> companies = new LinkedList<>();
        List<Address> addresses = new LinkedList<>();

        companies.add(new Company("http://www.regis24.de/impressum/", Optional.of("Regis 24"), false));
        companies.add(new Company("https://www.savage-wear.com/de/content/6_impressum", Optional.of("Savage Wear"), 2));
        companies.add(new Company("http://www.idealo.de/preisvergleich/AGB.html", Optional.of("idealo"), false));
        // add one company without a name
        companies.add(new Company("http://www.powerflasher.de/#/de/kontakt", Optional.empty(), 2));

        addresses.add(new Address("Wallstraße 58 10179 Berlin", LocalDateTime.now(), companies.get(0)));
        addresses.add(new Address("Gubener Straße 29 10243 Berlin", LocalDateTime.now(), companies.get(1), 1));
        addresses.add(new Address("Grünberger Straße 16 10243 berlin", LocalDateTime.now(), companies.get(1), 2));
        addresses.add(new Address("Ritterstraße 11 10969 Berlin, Deutschland", LocalDateTime.now(), companies.get(2)));

        // add one company without a name
        addresses.add(new Address("An der Alster 47, D-20099 Hamburg", LocalDateTime.now(), companies.get(3), 1));
        addresses.add(new Address("Rua Jose Getulio, 579, CJ 55/56, Aclimacao CEP 01509-001, Sao Paolo - SP - Brasil",
                LocalDateTime.now(), companies.get(3), 2));

        return new CompaniesAndAddresses(companies, addresses);
    }

    static CompaniesAndAddresses mockCompaniesAndFewerAddresses() throws MalformedURLException {
        List<Company> companies = new LinkedList<>();
        List<Address> addresses = new LinkedList<>();

        companies.add(new Company("http://www.regis24.de/impressum/", Optional.of("Regis 24"), false));
        companies.add(new Company("https://www.savage-wear.com/de/content/6_impressum", Optional.of("Savage Wear"), 1));
        companies.add(new Company("http://www.idealo.de/preisvergleich/AGB.html", Optional.of("idealo"), false));
        // add one company without a name
        companies.add(new Company("http://www.powerflasher.de/#/de/kontakt", Optional.<String>empty(), 1));

        addresses.add(new Address("Wallstraße 58 10179 Berlin", LocalDateTime.now(), companies.get(0)));
        addresses.add(new Address("Gubener Straße 29 10243 Berlin", LocalDateTime.now(), companies.get(1), 1));
        addresses.add(new Address("Ritterstraße 11 10969 Berlin, Deutschland", LocalDateTime.now(), companies.get(2)));

        // add one company without a name
        addresses.add(new Address("An der Alster 47, D-20099 Hamburg", LocalDateTime.now(), companies.get(3), 1));

        return new CompaniesAndAddresses(companies, addresses);
    }

    static final class CompaniesAndAddresses {
        private final List<Company> companies;
        private final List<Address> addresses;

        public CompaniesAndAddresses(List<Company> companies, List<Address> addresses) {
            this.companies = companies;
            this.addresses = addresses;
        }

        public List<Company> getCompanies() {
            return companies;
        }

        public List<Address> getAddresses() {
            return addresses;
        }
    }
}
