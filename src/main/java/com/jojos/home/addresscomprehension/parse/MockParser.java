/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.parse;

import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A mock parser that never returns anything. Always returning an empty collection
 *
 * Created by karanikasg@gmail.com.
 */
public class MockParser implements Parser {

    @Override
    public Set<Address> extractAddressesFromString(String page, Company company, Optional<ParserCtx> parserCtx) {
        // Always return an empty set
        return new HashSet<>();
    }

    @Override
    public Set<Address> extractAddressesFromFile(File file, Company company, Optional<ParserCtx> parserCtx) {
        // Never return a null.
        return new HashSet<>();
    }
}
