/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.email;

import com.jojos.home.addresscomprehension.util.Util;
import com.jojos.home.addresscomprehension.values.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class responsible for dispatching email reports
 *
 * Created by karanikasg@gmail.com.
 */
public class Email {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Properties props;
    private final String user;
    private final String pass;
    private final String subject;
    private final String mailTo;


    public Email(Properties props) {
        this.user = props.getProperty("mail.user");
        this.pass = props.getProperty("mail.pass");
        this.subject = props.getProperty("mail.subject", "Address Discovery");
        this.mailTo = props.getProperty("mail.to");

        this.props = props;
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", props.getProperty("mail.smtp.host", "smtp.gmail.com"));
        props.put("mail.smtp.port", props.getProperty("mail.smtp.port", "587"));
        props.put("mail.smtp.ssl.trust", props.getProperty("mail.smtp.host", "smtp.gmail.com"));
    }

    public void dispatch(Collection<Address> addresses) {
        if (addresses != null && !addresses.isEmpty()) {
            log.info("Sending email for {}", Util.toString(addresses, Address::toString));
            try {
                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(user, pass);
                            }
                        });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(user));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(mailTo));

                message.setSubject(subject);
                message.setText(createMessageBodyFrom(addresses));

                Transport.send(message);

                log.info("email sent");
            } catch (MessagingException e) {
                log.error("send failed", e);
            }
        } else {
            log.warn("No addresses found. Sending email aborted");
        }
    }

    private static String createMessageBodyFrom(Collection<Address> addresses) {
        AtomicInteger ai = new AtomicInteger();
        return Util.toString(
                addresses,
                (a) -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ai.incrementAndGet());
                    sb.append(". Company ");
                    sb.append(a.getCompany().asString());
                    sb.append(" has a new Address ");
                    sb.append(a.asString());
                    return sb.toString();
                },
                Optional.of(System.lineSeparator() + System.lineSeparator()),
                Optional.of(addresses.size() + " addresses changed in total" + System.lineSeparator()),
                Optional.of(System.lineSeparator() + System.lineSeparator() + "Report generated at " + Util.getTodaysDate()));
    }


}
