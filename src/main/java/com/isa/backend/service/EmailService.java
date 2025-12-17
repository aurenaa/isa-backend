package com.isa.backend.service;

import com.isa.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Environment env;

    @Async
    public void sendActivationEmail(User user) {
        String activationLink = "http://localhost:8080/auth/activate?code=" + user.getActivationCode();
        String subject = "Account activation";
        String body = "Hello " + user.getFirstName() + ",\n\n"
                + "Click on the link to activate your account :\n"
                + activationLink;

        sendEmail(user.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setFrom(env.getProperty("spring.mail.username"));
        mail.setSubject(subject);
        mail.setText(body);
        javaMailSender.send(mail);
    }
}
