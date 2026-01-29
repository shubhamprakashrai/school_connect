package com.schoolmgmt.service;

import com.schoolmgmt.model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.name:School Management System}")
    private String appName;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    /**
     * Send email verification
     */
    @Async
    public void sendEmailVerification(User user) {
        String subject = "Verify Your Email - " + appName;
        String verificationUrl = frontendUrl + "/verify-email?token=" + user.getEmailVerificationToken();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName());
        variables.put("appName", appName);
        variables.put("verificationUrl", verificationUrl);
        
        sendHtmlEmail(user.getEmail(), subject, "email-verification", variables);
    }
    
    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Password Reset Request - " + appName;
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName());
        variables.put("appName", appName);
        variables.put("resetUrl", resetUrl);
        
        sendHtmlEmail(user.getEmail(), subject, "password-reset", variables);
    }
    
    /**
     * Send password change confirmation
     */
    @Async
    public void sendPasswordChangeConfirmation(User user) {
        String subject = "Password Changed Successfully - " + appName;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName());
        variables.put("appName", appName);
        
        sendHtmlEmail(user.getEmail(), subject, "password-change-confirmation", variables);
    }
    
    /**
     * Send welcome email after successful registration
     */
    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to " + appName;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName());
        variables.put("appName", appName);
        variables.put("loginUrl", frontendUrl + "/login");
        
        sendHtmlEmail(user.getEmail(), subject, "welcome", variables);
    }
    
    /**
     * Send account locked notification
     */
    @Async
    public void sendAccountLockedEmail(User user, int lockDurationMinutes) {
        String subject = "Account Locked - " + appName;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName());
        variables.put("appName", appName);
        variables.put("lockDuration", lockDurationMinutes);
        
        sendHtmlEmail(user.getEmail(), subject, "account-locked", variables);
    }
    
    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    /**
     * Send HTML email using Thymeleaf template.
     * Falls back to plain text if template processing or HTML sending fails.
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent to: {} using template: {}", to, templateName);

        } catch (Exception e) {
            log.warn("Failed to send HTML email to: {} using template: {}. Falling back to plain text.", to, templateName, e);
            sendPlainTextFallback(to, subject, templateName, variables);
        }
    }

    /**
     * Plain text fallback used when Thymeleaf template processing or HTML email sending fails.
     */
    private void sendPlainTextFallback(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            StringBuilder text = new StringBuilder();
            text.append("Dear ").append(variables.get("userName")).append(",\n\n");

            switch (templateName) {
                case "email-verification":
                    text.append("Please verify your email by clicking the link below:\n");
                    text.append(variables.get("verificationUrl"));
                    break;
                case "password-reset":
                    text.append("You requested a password reset. Click the link below to reset your password:\n");
                    text.append(variables.get("resetUrl"));
                    text.append("\n\nIf you didn't request this, please ignore this email.");
                    break;
                case "password-change-confirmation":
                    text.append("Your password has been changed successfully.");
                    text.append("\n\nIf you didn't make this change, please contact support immediately.");
                    break;
                case "welcome":
                    text.append("Welcome to ").append(appName).append("!");
                    text.append("\n\nYour account has been created successfully.");
                    text.append("\n\nYou can login at: ").append(variables.get("loginUrl"));
                    break;
                case "account-locked":
                    text.append("Your account has been locked due to multiple failed login attempts.");
                    text.append("\n\nIt will be automatically unlocked after ").append(variables.get("lockDuration")).append(" minutes.");
                    break;
                default:
                    text.append("You have a new notification from ").append(appName).append(".");
                    break;
            }

            text.append("\n\nBest regards,\n").append(appName).append(" Team");

            sendSimpleEmail(to, subject, text.toString());

        } catch (Exception e) {
            log.error("Failed to send plain text fallback email to: {}", to, e);
        }
    }

    /**
     * Send HTML email with Thymeleaf template.
     *
     * @deprecated Use the public email methods (e.g. sendEmailVerification, sendPasswordResetEmail, etc.) instead.
     *             Those methods now use Thymeleaf templates via {@link #sendHtmlEmail} internally.
     */
    @Deprecated
    @Async
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        sendHtmlEmail(to, subject, templateName, variables);
    }
}
