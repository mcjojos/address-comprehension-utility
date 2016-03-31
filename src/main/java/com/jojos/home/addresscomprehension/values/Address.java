/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.values;

import java.time.LocalDateTime;

/**
 * Object representing an address.
 *
 * Created by karanikasg@gmail.com.
 */
public class Address {
    private final String value;
    private final LocalDateTime dateTime;
    private final Company company;
    private final int ordinal;

    public Address(String value, LocalDateTime dateTime, Company company) {
        this.value = value;
        this.dateTime = dateTime;
        this.company = company;
        this.ordinal = 1;
    }

    public Address(String value, LocalDateTime dateTime, Company company, int ordinal) {
        this.value = value;
        this.dateTime = dateTime;
        this.company = company;
        this.ordinal = ordinal;
    }

    public String getValue() {
        return value;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Company getCompany() {
        return company;
    }

    public int getOrdinal() {
        return ordinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (ordinal != address.ordinal) return false;
        if (value != null ? !value.equals(address.value) : address.value != null) return false;
        if (dateTime != null ? !dateTime.equals(address.dateTime) : address.dateTime != null) return false;
        return !(company != null ? !company.equals(address.company) : address.company != null);

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
        result = 31 * result + (company != null ? company.hashCode() : 0);
        result = 31 * result + ordinal;
        return result;
    }

    public String asString() {
        return "{" +
                "'" + value + "\', " +
                "fetched on '" + dateTime + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return "Address{" +
                "value='" + value + '\'' +
                ", dateTime=" + dateTime +
                ", company=" + company +
                ", ordinal=" + ordinal +
                '}';
    }
}
