package org.social.authservice.services;

public interface EmailService {
    void guiEmailKichHoat(String toEmail, String maKichHoat);

    void guiEmailQuenMatKhau(String toEmail, String otp);
}
