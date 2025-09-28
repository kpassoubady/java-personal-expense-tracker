package com.expensetracker.app.e2e.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

/**
 * Email notification service for test execution results
 */
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    private final String smtpHost;
    private final String smtpPort;
    private final String username;
    private final String password;
    private final String fromAddress;
    private final String[] toAddresses;
    
    public EmailNotificationService(String smtpHost, String smtpPort, String username, 
                                  String password, String fromAddress, String[] toAddresses) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
        this.toAddresses = toAddresses;
    }
    
    /**
     * Send test execution summary email
     */
    public void sendTestExecutionSummary(TestExecutionSummary summary) {
        try {
            Properties props = getEmailProperties();
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            
            // Set recipients
            for (String toAddress : toAddresses) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            }
            
            message.setSubject(getSubject(summary));
            message.setContent(getEmailContent(summary), "text/html; charset=utf-8");
            
            Transport.send(message);
            logger.info("Test execution summary email sent successfully");
            
        } catch (Exception e) {
            logger.error("Failed to send test execution summary email", e);
        }
    }
    
    private Properties getEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }
    
    private String getSubject(TestExecutionSummary summary) {
        String status = summary.getFailedTests() > 0 ? "FAILED" : "PASSED";
        return String.format("[Expense Tracker Tests] %s - %d/%d tests passed", 
                           status, summary.getPassedTests(), summary.getTotalTests());
    }
    
    private String getEmailContent(TestExecutionSummary summary) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif;'>");
        content.append("<h2 style='color: #3498db;'>Expense Tracker Test Execution Report</h2>");
        
        // Test summary
        content.append("<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 10px 0;'>");
        content.append("<h3>Test Execution Summary</h3>");
        content.append("<table style='border-collapse: collapse; width: 100%;'>");
        content.append(String.format("<tr><td><strong>Total Tests:</strong></td><td>%d</td></tr>", summary.getTotalTests()));
        content.append(String.format("<tr><td><strong>Passed:</strong></td><td style='color: green;'>%d</td></tr>", summary.getPassedTests()));
        content.append(String.format("<tr><td><strong>Failed:</strong></td><td style='color: red;'>%d</td></tr>", summary.getFailedTests()));
        content.append(String.format("<tr><td><strong>Skipped:</strong></td><td style='color: orange;'>%d</td></tr>", summary.getSkippedTests()));
        content.append(String.format("<tr><td><strong>Success Rate:</strong></td><td>%.2f%%</td></tr>", summary.getSuccessRate()));
        content.append(String.format("<tr><td><strong>Execution Time:</strong></td><td>%s</td></tr>", summary.getExecutionTime()));
        content.append("</table>");
        content.append("</div>");
        
        // Environment information
        content.append("<div style='background-color: #e9ecef; padding: 20px; border-radius: 5px; margin: 10px 0;'>");
        content.append("<h3>Environment Information</h3>");
        content.append("<table style='border-collapse: collapse; width: 100%;'>");
        content.append(String.format("<tr><td><strong>Environment:</strong></td><td>%s</td></tr>", summary.getEnvironment()));
        content.append(String.format("<tr><td><strong>Browser:</strong></td><td>%s</td></tr>", summary.getBrowser()));
        content.append(String.format("<tr><td><strong>Build Number:</strong></td><td>%s</td></tr>", summary.getBuildNumber()));
        content.append(String.format("<tr><td><strong>Git Branch:</strong></td><td>%s</td></tr>", summary.getGitBranch()));
        content.append("</table>");
        content.append("</div>");
        
        // Failed tests details (if any)
        if (summary.getFailedTests() > 0) {
            content.append("<div style='background-color: #f8d7da; padding: 20px; border-radius: 5px; margin: 10px 0;'>");
            content.append("<h3 style='color: #721c24;'>Failed Tests</h3>");
            content.append("<ul>");
            for (String failedTest : summary.getFailedTestNames()) {
                content.append(String.format("<li style='color: #721c24;'>%s</li>", failedTest));
            }
            content.append("</ul>");
            content.append("</div>");
        }
        
        // Report links
        content.append("<div style='background-color: #d1ecf1; padding: 20px; border-radius: 5px; margin: 10px 0;'>");
        content.append("<h3>Report Links</h3>");
        content.append(String.format("<p><a href='%s'>ExtentReports HTML Report</a></p>", summary.getExtentReportPath()));
        content.append(String.format("<p><a href='%s'>Allure Report</a></p>", summary.getAllureReportPath()));
        content.append(String.format("<p><a href='%s'>JaCoCo Coverage Report</a></p>", summary.getJacocoReportPath()));
        content.append("</div>");
        
        content.append("<p style='color: #6c757d; font-size: 12px;'>This email was generated automatically by the Expense Tracker test automation framework.</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
}