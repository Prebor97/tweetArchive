package com.example.tweetArchive.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendTweetUploadConfirmation(
            String toEmail,
            String userName,
            String date
    ) throws MessagingException {

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("appUrl", "https://yourapp.com/dashboard");
        context.setVariable("uploadDate",date);

        String htmlContent = templateEngine.process("successful-batch-notification", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Your Tweets Have Been Successfully Uploaded!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendTweetUploadFailure(
            String toEmail,
            String userName,
            String attemptDate
    ) throws MessagingException {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("appUrl", "https://yourapp.com/dashboard");
        context.setVariable("attemptDate", attemptDate);

        String htmlContent = templateEngine.process("unsuccessful-batch-notification", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Issue with Your Recent Tweet Upload");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
