/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.email;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.jojos.home.addresscomprehension.values.Address;
import com.jojos.home.addresscomprehension.values.Company;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

/**
 * Test class for email sending. Contains tests for both a mock sending and a real email sending
 * although the latter is currently disabled.
 *
 * Created by karanikasg@gmail.com.
 */
public class EmailTest {
    private static Collection<Address> addresses;
    private static SimpleSmtpServer server;

    @BeforeClass
    public static void setUp() throws MalformedURLException {
        addresses = new HashSet<>();
        server = SimpleSmtpServer.start();

        Company company1 = new Company("http://www.regis24.de/impressum/", Optional.of("Regis 24"), false);

        addresses.add(new Address("Bernburger str 16, 10963, Berlin", LocalDateTime.now(), company1));
    }

    // uncomment this test case if you want to test real email sending.
    // Don't forget to fill in the user, pass and to
    // @Test
    public void testEmailDispatch() throws MalformedURLException {
        Properties properties = new Properties();

        properties.setProperty("mail.user", "mock_sender@gmail.com");
        properties.setProperty("mail.pass", "mockpass");
        properties.setProperty("mail.to", "mock_receiver@gmail.com");
        properties.setProperty("mail.subject", "Address Discovery [testing]");
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");

        Email email = new Email(properties);
        email.dispatch(addresses);
    }

    @Test
    public void testMockEmailDispatch() throws MalformedURLException {
        Properties properties = new Properties();

        String subject = "Address Discovery [testing]";
        properties.setProperty("mail.user", "sender@here.com");
        properties.setProperty("mail.pass", "mock_pass");
        properties.setProperty("mail.to", "receiver@there.com");
        properties.setProperty("mail.subject", subject);
        properties.setProperty("mail.smtp.host", "localhost");
        properties.setProperty("mail.smtp.port", "25");

        Email email = new Email(properties);

        try {
            // Submits an email using javamail to the email server listening on port 25
            // (method not shown here). Replace this with a call to your app logic.
            email.dispatch(addresses);
        } catch(Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception: " + e);
        }
        server.stop();

        Assert.assertTrue(server.getReceivedEmailSize() == 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage receivedEmail = (SmtpMessage)emailIter.next();
        Assert.assertTrue(receivedEmail.getHeaderValue("Subject").equals(subject));
        // SimpleSmtpServer truncates some line separators and doesn't handle correctly some of the characters
        // assert that some of the addresses are contained in the body of the received email
        // instead of testing for complete equality
        Optional<Address> firstAddress = addresses.stream().findFirst();
        if (firstAddress.isPresent()) {
            String address = firstAddress.get().getValue();
            Assert.assertTrue(receivedEmail.getBody().contains(address));
        } else {
            Assert.fail("No address found which is weird");
        }

    }

    @AfterClass
    public static void cleanUp() {
        server.stop();
    }


}
