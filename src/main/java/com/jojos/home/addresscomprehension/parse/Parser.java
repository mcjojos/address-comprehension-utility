/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.parse;

import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Interface defining the extraction action.
 *
 * Created by karanikasg@gmail.com.
 */
public interface Parser {

    /**
     * Interface method to extract all addresses from a given {@link String}.
     * @implNote never return a null {@link Set}, always return an empty collection in case nothing is found
     * @param page the string from which the addresses shall be extracted from
     * @param company the company we are referring to.
     * @param parserCtx encapsulates additional information
     * @return a set of addresses found or an empty set if nothing was found. NEVER return a null value.
     */
    Set<Address> extractAddressesFromString(String page, Company company, Optional<ParserCtx> parserCtx);

    /**
     * Interface method to extract all addresses from a given {@link File}.
     * @implNote never return a null {@link Set}, always return an empty collection in case nothing is found
     * @param file the file from which the addresses shall be extracted from
     * @param company the company we are referring to
     * @param parserCtx optional context which encapsulates additional information.
     * @return a set of addresses found or an empty set if nothing was found. NEVER return a null value.
     * @throws IOException
     */
    Set<Address> extractAddressesFromFile(File file, Company company, Optional<ParserCtx> parserCtx)
            throws IOException;

}
