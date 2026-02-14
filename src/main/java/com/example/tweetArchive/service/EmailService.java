package com.example.tweetArchive.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendTweetUploadFeedback(
            String toEmail,
            String userName,
            String template
    ) throws MessagingException {

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("appUrl", "http://localhost:3000/dashboard");
        context.setVariable("updateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        String htmlContent;
        if ("success".equals(template)) {
             htmlContent = templateEngine.process("successful-batch-notification", context);
        }else {
             htmlContent = templateEngine.process("unsuccessful-batch-notification", context);
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Uploaded Update!!!!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
    public void sendTweetAnalysisFeedback(
            String toEmail,
            long flaggedTweets,
            long totalTweetCount,
            String userName,
            String template
    ) throws MessagingException {

        Context context1 = new Context();
        context1.setVariable("userName",     userName);
        context1.setVariable("totalTweets",  totalTweetCount);
        context1.setVariable("flaggedCount", flaggedTweets);
        context1.setVariable("reviewUrl",    "/dashboard/review");
        context1.setVariable("analysisDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        context1.setVariable("appUrl",       "https://yourdomain.com/analysis");

        Context context2 = new Context();
        context2.setVariable("userName",     userName);
        context2.setVariable("dashboardUrl", "\"http://localhost:3000/dashboard");
        context2.setVariable("attemptDate",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        context2.setVariable("appUrl",       "http://localhost:3000/dashboard?tab=flagged");

        String htmlContent;
        if ("success".equals(template)) {
            htmlContent = templateEngine.process("successful-analysis-notification", context1);
        }else {
            htmlContent = templateEngine.process("unsuccessful-batch-notification", context2);
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Analysis Update!!!!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
