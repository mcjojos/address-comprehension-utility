/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.values;

import com.jojos.home.addresscomprehension.parse.DefaultParser;
import com.jojos.home.addresscomprehension.parse.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Object representing a company
 *
 * Created by karanikasg@gmail.com.
 */
public class Company {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final URL url;
    private final String urlStr;
    private final Optional<String> name;
    private final int addressCount;
    private final List<Parser> registeredParsers;
    private final boolean retainDownloadData;

    public Company(String urlStr, Optional<String> name, boolean retainDownloadData, String... parsers)
            throws MalformedURLException {
        this.urlStr = urlStr;
        this.url = new URL(urlStr);
        this.name = name;
        this.retainDownloadData = retainDownloadData;
        this.registeredParsers = registerParsers(parsers);
        this.addressCount = 1;
    }

    public Company(String urlStr, Optional<String> name, int addressCount, String... parsers)
            throws MalformedURLException {
        this.urlStr = urlStr;
        this.url = new URL(urlStr);
        this.name = name;
        this.retainDownloadData = false;
        this.registeredParsers = registerParsers(parsers);
        this.addressCount = addressCount;
    }

    private Company(URL url, String urlStr, Optional<String> name, boolean retainDownloadData, int addressCount, List<Parser> parsers) {
        this.url = url;
        this.urlStr = urlStr;
        this.name = name;
        this.addressCount = addressCount;
        this.registeredParsers = parsers;
        this.retainDownloadData = retainDownloadData;
    }

    private List<Parser> registerParsers(String... parsers) {
        List<Parser> discoveredParsers = new ArrayList<>();
        Class<Parser> superType = Parser.class;
        for (String parser : parsers) {
            if (!parser.equals(DefaultParser.class.getCanonicalName())) {
                try {
                    Class<?> c = Class.forName(parser);
                    Object obj = c.newInstance();
                    if (superType.isInstance(obj)) {
                        discoveredParsers.add((Parser)obj);
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    log.error("An error occurred while trying to register parser ", e);
                }
            }
        }

        discoveredParsers.add(new DefaultParser());

        return discoveredParsers;
    }

    public String getUrlStr() {
        return urlStr;
    }

    public Optional<String> getName() {
        return name;
    }

    public int getAddressCount() {
        return addressCount;
    }

    public URL getUrl() {
        return url;
    }

    // don't be part in the hashCode and equals
    public List<Parser> getRegisteredParsers() {
        return registeredParsers;
    }

    // don't be part in the hashCode and equals
    public boolean isRetainDownloadData() {
        return retainDownloadData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;

        if (addressCount != company.addressCount) return false;
        if (urlStr != null ? !urlStr.equals(company.urlStr) : company.urlStr != null) return false;
        return !(name != null ? !name.equals(company.name) : company.name != null);

    }

    @Override
    public int hashCode() {
        int result = urlStr != null ? urlStr.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + addressCount;
        return result;
    }

    @Override
    public String toString() {
        return "Company{" +
                "urlStr='" + urlStr + '\'' +
                ", name=" + (name.isPresent() ? name.get() : name) +
                ", addressCount=" + addressCount +
                '}';
    }

    public String asString() {
        return "{" +
                "'" + (name.isPresent() ? name.get() : "N/A") + "\', " +
                "'" + urlStr + '\'' +
                '}';
    }

    public Company cloneWith(int addressCount) {
        return new Company(url, urlStr, name, retainDownloadData, addressCount, registeredParsers);
    }
}
