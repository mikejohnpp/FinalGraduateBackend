package org.social.authservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.authservice.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.activation-url}")
    private String activationUrl;

    @Async
    @Override
    public void guiEmailKichHoat(String toEmail, String maKichHoat) {
        String linkKichHoat = activationUrl + "/" + maKichHoat;


        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Kích hoạt tài khoản của bạn");
        message.setText(
                "Chào bạn,\n\n" +
                        "Cảm ơn bạn đã đăng ký tài khoản.\n" +
                        "Vui lòng nhấn vào đường dẫn bên dưới để kích hoạt tài khoản:\n\n" +
                        linkKichHoat + "\n\n" +
                        "Đường dẫn có hiệu lực trong 24 giờ.\n\n" +
                        "Trân trọng,\nWeb TMĐT Team");
        mailSender.send(message);
    }

    @Async
    @Override
    public void guiEmailQuenMatKhau(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Yêu cầu đặt lại mật khẩu");
        message.setText(
                "Chào bạn,\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.\n" +
                        "Mã xác nhận (OTP) của bạn là: " + otp + "\n\n" +
                        "Mã này có hiệu lực trong 24 giờ.\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\nWeb TMĐT Team");
        mailSender.send(message);
    }
}

