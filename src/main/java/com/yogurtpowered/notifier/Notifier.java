package com.yogurtpowered.notifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.web.client.RestTemplate;

/**
 * Watch a website for changes and send an email when one is found.
 */
public class Notifier implements Runnable {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private final String endpoint;
    private final String stringToStartWith;
    private final String stringToEndWith;
    private final long delayInMs;
    private final GmailSender emailSender;

    public Notifier(String endpoint, String stringToStartWith, String stringToEndWith, long delayInMs, String emailTo,
            String emailFrom) {
        this.endpoint = endpoint;
        this.stringToStartWith = stringToStartWith;
        this.stringToEndWith = stringToEndWith;
        this.delayInMs = delayInMs;
        this.emailSender = new GmailSender(emailFrom, emailTo);
    }

    public void run() {

        String lastContent = getComparableContent();

        while (true) {

            String currentContent = getComparableContent();

            if (!lastContent.equals(currentContent)) {
                break;
            }

            System.out.print('.');

            try {
                Thread.sleep(delayInMs);
            } catch (InterruptedException e) {
            }
        }

        emailSender.sendEmail("Change detected: " + endpoint, "Change detected on site: " + endpoint);
        System.out.println("Change detected!");
    }

    private String getComparableContent() {
        String content = REST_TEMPLATE.getForObject(endpoint, String.class);
        return content.substring(content.indexOf(stringToStartWith), content.lastIndexOf(stringToEndWith));
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(new File("src/main/resources/notifier.properties")));

        String endpoint = props.getProperty("notifier.endpoint");
        String stringToStartWith = props.getProperty("notifier.comparison.start");
        String stringToEndWith = props.getProperty("notifier.comparison.end");
        long delayInMs = Long.parseLong(props.getProperty("notifier.delay.ms"));
        String emailTo = props.getProperty("notifier.email.to");
        String emailFrom = props.getProperty("notifier.email.from");

        new Notifier(endpoint, stringToStartWith, stringToEndWith, delayInMs, emailTo, emailFrom).run();
    }
}
