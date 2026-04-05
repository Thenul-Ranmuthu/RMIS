package com.rmis.rmis.services.impl;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.domain.entities.ServiceTicket;
import com.rmis.rmis.repositories.MinistryOfficerRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.interfaces.EmailService;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${app.mail.resend.api}")
    private String resendApiKey;    

    private MinistryOfficerRepository ministryOfficerRepository;
    private QuotaRequestRepository quotaRequestRepository;

    public EmailServiceImpl(MinistryOfficerRepository ministryOfficerRepository, QuotaRequestRepository quotaRequestRepository) {
        this.ministryOfficerRepository = ministryOfficerRepository;
        this.quotaRequestRepository = quotaRequestRepository;
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("RMIS Support <rmis.reset@rmis.space>")
                .to(email)
                .subject("Password Reset Request - RMIS")
                .html("""
                            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                                <h2 style="color: #333;">Password Reset Request</h2>
                                <p>Hello,</p>
                                <p>You requested to reset your password for your RMIS account. Click the button below to reset it:</p>
                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="%s" style="background-color: #007bff; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">Reset Password</a>
                                </div>
                                <p>This link will expire in 24 hours.</p>
                                <p>If you did not request a password reset, please ignore this email.</p>
                                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                                <p style="font-size: 12px; color: #777;">RMIS Team</p>
                            </div>
                        """
                        .formatted(resetLink))
                .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            // Log the error and rethrow to allow the caller (Controller) to handle it
            System.err.println("Failed to send password reset email to " + email + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendNotificationNewRequestSubmission(QuotaRequestAddQuotaDto quotaRequestHeaderDto) {
        List<MinistryOfficer> officers = ministryOfficerRepository.findAll();

        Resend resend = new Resend(resendApiKey);

        for(MinistryOfficer o: officers){
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from("RMIS Support <rmis.qouta@rmis.space>")
                .to(o.getEmail())
                .subject("New Quota Request Available - RMIS")
                .html("""
                        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                            <h2 style="color: #333;">New Quota Request Submitted</h2>
                            <p>Hello,</p>
                            <p>A new environmental quota request has been submitted by <strong>%s</strong> and is awaiting your review.</p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s/ministry/quota-requests" style="background-color: #1a6b3c; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">Review Request</a>
                            </div>
                            <p>Please log in to the Ministry Portal to approve or reject this request.</p>
                            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                            <p style="font-size: 12px; color: #777;">RMIS Team</p>
                        </div>
                    """
                    .formatted(quotaRequestHeaderDto.getCompanyEmail(),"http://localhost:3000")
                ).build();

            try {
                resend.emails().send(params);
            } catch (Exception e) {
                // Log the error and rethrow to allow the caller (Controller) to handle it
                System.err.println("Failed to send password reset email to " + o.getEmail() + ": " + e.getMessage());
                throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void sendNotificationRequestApproval(UUID id) {
        QuotaRequest quotaRequest = quotaRequestRepository.findByIdWithCompany(id)
            .orElseThrow(() -> new RuntimeException("No Quota request!!"));

        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from("RMIS Support <rmis.qouta@rmis.space>")
            .to(quotaRequest.getCompany().getEmail())
            .subject("Quota Request Approved - RMIS")
            .html("""
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                        <h2 style="color: #1a6b3c;">Quota Request Approved ✓</h2>
                        <p>Hello,</p>
                        <p>Your environmental quota request with ID <strong>%s</strong> has been <strong style="color: #1a6b3c;">approved</strong> by the Ministry of Environment.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s/company/dashboard" style="background-color: #1a6b3c; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">View Dashboard</a>
                    </div>
                        <p>You can log in to the Company Portal to view your updated quota balance.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="font-size: 12px; color: #777;">RMIS Team</p>
                    </div>
                    """
                .formatted(quotaRequest.getRequestId(), "http://localhost:3000"))
        .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            // Log the error and rethrow to allow the caller (Controller) to handle it
            System.err.println("Failed to send password reset email to " + quotaRequest.getCompany().getEmail() + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendNotificationRequestRejection(UUID id) {
        QuotaRequest quotaRequest = quotaRequestRepository.findByIdWithCompany(id)
            .orElseThrow(() -> new RuntimeException("No Quota request!!"));

        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from("RMIS Support <rmis.qouta@rmis.space>")
            .to(quotaRequest.getCompany().getEmail())
            .subject("Quota Request Rejected!! - RMIS")
            .html("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #c0392b;">Quota Request Rejected ✗</h2>
                    <p>Hello,</p>
                    <p>Your environmental quota request with ID <strong>%s</strong> has been <strong style="color: #c0392b;">rejected</strong> by the Ministry of Environment.</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s/company/dashboard" style="background-color: #c0392b; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">View Dashboard</a>
                </div>
                    <p>You can log in to the Company Portal to view your quota request history.</p>
                    <p>If you believe this decision was made in error, please contact the Ministry of Environment for further assistance.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #777;">RMIS Team</p>
                </div>
                """
            .formatted(quotaRequest.getRequestId(), "http://localhost:3000"))
        .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            // Log the error and rethrow to allow the caller (Controller) to handle it
            System.err.println("Failed to send password reset email to " + quotaRequest.getCompany().getEmail() + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override

    public void sendBookingStatusUpdateEmail(ServiceTicket ticket) {
        String customerEmail = resolveCustomerEmail(ticket);
        String customerName  = resolveCustomerName(ticket);
        if (customerEmail == null) return;

        String statusLabel = switch (ticket.getStatus()) {
            case ACCEPTED    -> "Accepted";
            case COMPLETED   -> "Completed";
            default          -> ticket.getStatus().name();
        };

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;
                            padding: 24px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #047857;">Booking Status Update</h2>
                    <p>Dear %s,</p>
                    <p>Your service booking <strong>%s</strong> has been updated.</p>
                    <table style="width:100%%; border-collapse:collapse; margin: 16px 0;">
                        <tr>
                            <td style="padding:8px; color:#64748b; width:40%%;">Ticket Number</td>
                            <td style="padding:8px; font-weight:bold;">%s</td>
                        </tr>
                        <tr style="background:#f8fafc;">
                            <td style="padding:8px; color:#64748b;">Service</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:8px; color:#64748b;">New Status</td>
                            <td style="padding:8px;">
                                <span style="background:#ecfdf5; color:#047857; padding:3px 10px;
                                             border-radius:99px; font-weight:bold; font-size:13px;">%s</span>
                            </td>
                        </tr>
                        <tr style="background:#f8fafc;">
                            <td style="padding:8px; color:#64748b;">Scheduled Date</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                    </table>
                    <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                    <p style="font-size:12px; color:#94a3b8;">RMIS · Ministry of Environment</p>
                </div>
                """.formatted(
                customerName,
                ticket.getTicketNumber(),
                ticket.getTicketNumber(),
                ticket.getServiceType(),
                statusLabel,
                ticket.getAvailability().getDate().toString()
        );

        sendEmail(customerEmail, "Your Service Booking is " + statusLabel + " - RMIS", html);
    }

    @Override

    public void sendBookingCancellationEmail(ServiceTicket ticket) {
        String customerEmail = resolveCustomerEmail(ticket);
        String customerName  = resolveCustomerName(ticket);
        if (customerEmail == null) return;

        String reason = ticket.getCancellationReason() != null
                ? ticket.getCancellationReason()
                : "No reason provided.";

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;
                            padding: 24px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #dc2626;">Booking Cancelled</h2>
                    <p>Dear %s,</p>
                    <p>Your service booking <strong>%s</strong> has been cancelled.</p>
                    <table style="width:100%%; border-collapse:collapse; margin: 16px 0;">
                        <tr>
                            <td style="padding:8px; color:#64748b; width:40%%;">Ticket Number</td>
                            <td style="padding:8px; font-weight:bold;">%s</td>
                        </tr>
                        <tr style="background:#f8fafc;">
                            <td style="padding:8px; color:#64748b;">Service</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:8px; color:#64748b;">Scheduled Date</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr style="background:#fef2f2;">
                            <td style="padding:8px; color:#64748b;">Cancellation Reason</td>
                            <td style="padding:8px; color:#dc2626;">%s</td>
                        </tr>
                    </table>
                    <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                    <p style="font-size:12px; color:#94a3b8;">RMIS · Ministry of Environment</p>
                </div>
                """.formatted(
                customerName,
                ticket.getTicketNumber(),
                ticket.getTicketNumber(),
                ticket.getServiceType(),
                ticket.getAvailability().getDate().toString(),
                reason
        );

        sendEmail(customerEmail, "Your Service Booking Has Been Cancelled - RMIS", html);

    }

    @Override
    public void sendBookingCancellationTechnicianEmail(ServiceTicket ticket) {
        String technicianEmail = ticket.getTechnician().getEmail();
        String technicianName  = ticket.getTechnician().getFirstName() + " " + ticket.getTechnician().getLastName();
        String customerName    = resolveCustomerName(ticket);

        String reason = ticket.getCancellationReason() != null
                ? ticket.getCancellationReason()
                : "No reason provided by customer.";

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;
                            padding: 24px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #dc2626;">Booking Cancelled by Customer</h2>
                    <p>Dear %s,</p>
                    <p>The service booking <strong>%s</strong> scheduled for <strong>%s</strong> has been cancelled by the customer (%s).</p>
                    <table style="width:100%%; border-collapse:collapse; margin: 16px 0;">
                        <tr>
                            <td style="padding:8px; color:#64748b; width:40%%;">Ticket Number</td>
                            <td style="padding:8px; font-weight:bold;">%s</td>
                        </tr>
                        <tr style="background:#f8fafc;">
                            <td style="padding:8px; color:#64748b;">Service</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:8px; color:#64748b;">Scheduled Time</td>
                            <td style="padding:8px;">%s - %s</td>
                        </tr>
                        <tr style="background:#fef2f2;">
                            <td style="padding:8px; color:#64748b;">Cancellation Reason</td>
                            <td style="padding:8px; color:#dc2626;">%s</td>
                        </tr>
                    </table>
                    <p>The time slot has been released and is now available for other customers.</p>
                    <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                    <p style="font-size:12px; color:#94a3b8;">RMIS · Ministry of Environment</p>
                </div>
                """.formatted(
                technicianName,
                ticket.getTicketNumber(),
                ticket.getAvailability().getDate().toString(),
                customerName,
                ticket.getTicketNumber(),
                ticket.getServiceType(),
                ticket.getAvailability().getStartTime().toString(),
                ticket.getAvailability().getEndTime().toString(),
                reason
        );

        sendEmail(technicianEmail, "URGENT: Booking " + ticket.getTicketNumber() + " Cancelled by Customer - RMIS", html);
    }

    private void sendEmail(String to, String subject, String html) {
        Resend resend = new Resend(resendApiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("RMIS Support <rmis.bookings@rmis.space>")
                .to(to)
                .subject(subject)
                .html(html)
                .build();
        try {
            resend.emails().send(params);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    private String resolveCustomerEmail(ServiceTicket ticket) {
        if (ticket.getPublicUser() != null) return ticket.getPublicUser().getEmail();
        if (ticket.getCompany()    != null) return ticket.getCompany().getEmail();
        return null;
    }

    private String resolveCustomerName(ServiceTicket ticket) {
        if (ticket.getPublicUser() != null)
            return ticket.getPublicUser().getFirstName() + " " + ticket.getPublicUser().getLastName();
        if (ticket.getCompany() != null) return ticket.getCompany().getName();
        return "Customer";
    }
}

