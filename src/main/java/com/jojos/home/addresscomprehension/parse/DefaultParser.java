/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.parse;

import com.jojos.home.addresscomprehension.util.Util;
import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Our default parser implementation.
 *
 * Everything should have this implementation as a fallback
 *
 * Created by karanikasg@gmail.com.
 */
public class DefaultParser implements Parser {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // TODO: more illegal strings here. should deprecated this in the future
    private static final Set<String> ILLEGAL_CHARSEQS = new HashSet<>(Arrays.asList("<br>"));

    private final Pattern pattern;

    public DefaultParser() {
        pattern = Pattern.compile(".*(\\b\\p{L}+\\s?(?:[Ss]traße|[Ss]trasse)\\b.*(?:Deutschland|berlin))(.*)", Pattern.CASE_INSENSITIVE);
    }
    @Override
    public List<Address> extractAddressesFromString(String page, Company company, Optional<ParserCtx> parserCtx) {
        log.info("Parsing {}", company.getUrlStr());
        page = normalize(page);
        List<Address> addresses = new ArrayList<>();
        List<String> values = new ArrayList<>();

        String remaining = page;

        while (remaining != null && !remaining.isEmpty()) {
            CurrentMatchAndRemainingStr curAndRemain = matchStr(remaining);
            if (curAndRemain.isMatched) {
                values.add(curAndRemain.current);
            }
            remaining = curAndRemain.remaining;
        }

        if (!values.isEmpty()) {
            // if the address found in this run didn't match what have found in the past
            // clone the company with different address count and user that for the addresses.
            if (company.getAddressCount() != values.size()) {
                company = company.cloneWith(values.size());
            }
            LocalDateTime dateTime = LocalDateTime.now();
            for (int i = 0; i < values.size(); i++) {
                Address address = new Address(values.get(i), dateTime, company);
                addresses.add(address);
            }
        }

        log.info("Company {} Parsed addresses {}", company.getUrlStr(), Util.toString(addresses, Address::getValue));
        return addresses;
    }

    @Override
    public List<Address> extractAddressesFromFile(File file, Company company, Optional<ParserCtx> parserCtx)
            throws IOException {
        List<Address> addresses = new ArrayList<>();
        List<String> values = new ArrayList<>();

        Document document = Jsoup.parse(file, "UTF-8");
        Elements elements = document.select("p");

        for (Element element : elements) {
            String remaining = element.text();
//            System.out.println(remaining);

            while (remaining != null && !remaining.isEmpty()) {
                CurrentMatchAndRemainingStr curAndRemain = matchStr(remaining);
                if (curAndRemain.isMatched) {
                    values.add(curAndRemain.current);
                }
                remaining = curAndRemain.remaining;
            }

        }

        if (!values.isEmpty()) {
            // if the address found in this run didn't match what have found in the past
            // clone the company with different address count and user that for the addresses.
            if (company.getAddressCount() != values.size()) {
                company = company.cloneWith(values.size());
            }
            LocalDateTime dateTime = LocalDateTime.now();
            for (int i = 0; i < values.size(); i++) {
                Address address = new Address(values.get(i), dateTime, company);
                addresses.add(address);
            }
        }

        log.info("Company {} Parsed addresses {}", company.getUrlStr(), Util.toString(addresses, Address::getValue));
        return addresses;
    }

    /**
     * normalizes the string by making it a single-lined string as well as
     * removing all strings in the {@link #ILLEGAL_CHARSEQS}.
     * @param text the text to be normalized.
     * @return a normalized text.
     */
    String normalize(String text) {
        text = text.replaceAll("\\r\\n|\\r|\\n", " ");

        String mergedReplaceableStrings = ILLEGAL_CHARSEQS.stream().map(String::toString).collect(Collectors.joining("|"));

        text = text.replaceAll(mergedReplaceableStrings, " ");

        return text;
    }

    private CurrentMatchAndRemainingStr matchStr(String str) {
        Matcher matcher = pattern.matcher(str);
        String match = "";
        String remaining = "";
        if (matcher.matches()) {
            match = matcher.group(1);
            remaining = matcher.group(2);
        }

        return new CurrentMatchAndRemainingStr(match, remaining);
    }


    private final class CurrentMatchAndRemainingStr {
        private final String current;
        private final String remaining;
        private final boolean isMatched;

        private CurrentMatchAndRemainingStr(String current, String remaining) {
            this.current = current;
            this.isMatched = (current != null && !current.isEmpty());
            this.remaining = remaining;
        }

    }
}
