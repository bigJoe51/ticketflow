package com.kilimo.ticket.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

@Configuration
@Profile("exam")
public class ExamMailConfig {

    @Bean
    @Primary
    public JavaMailSender examMailSender(@Value("${app.exam.mail-preview-dir}") String previewDir) {
        Path outputDir = Path.of(previewDir);
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage((Session) null);
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                try {
                    return new MimeMessage((Session) null, contentStream);
                } catch (MessagingException ex) {
                    throw new MailParseException("Unable to parse MIME message", ex);
                }
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                throw new UnsupportedOperationException("MIME mail is not supported in exam mode");
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
                throw new UnsupportedOperationException("MIME mail is not supported in exam mode");
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
                throw new UnsupportedOperationException("MIME mail is not supported in exam mode");
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
                throw new UnsupportedOperationException("MIME mail is not supported in exam mode");
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {
                writePreview(outputDir, simpleMessage);
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {
                Arrays.stream(simpleMessages).forEach((message) -> writePreview(outputDir, message));
            }
        };
    }

    private void writePreview(Path outputDir, SimpleMailMessage message) {
        try {
            Files.createDirectories(outputDir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String recipient = sanitize((message.getTo() != null && message.getTo().length > 0) ? message.getTo()[0] : "unknown");
            Path outputFile = outputDir.resolve(timestamp + "-" + recipient + ".txt");
            String content = ""
                + "From: " + nullSafe(message.getFrom()) + System.lineSeparator()
                + "To: " + String.join(", ", message.getTo() == null ? new String[0] : message.getTo()) + System.lineSeparator()
                + "Subject: " + nullSafe(message.getSubject()) + System.lineSeparator()
                + System.lineSeparator()
                + nullSafe(message.getText()) + System.lineSeparator();
            Files.writeString(outputFile, content, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not write local mail preview", ex);
        }
    }

    private String sanitize(String value) {
        return nullSafe(value).replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
