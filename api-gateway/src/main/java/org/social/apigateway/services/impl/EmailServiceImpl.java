package org.social.apigateway.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.apigateway.services.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void guiEmailKichHoat(String toEmail, String maKichHoat) {
        String linkKichHoat = "http://localhost:5173/kich-hoat/" + maKichHoat;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Kích hoạt tài khoản của bạn");
        message.setText(
                "Chào bạn,\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản.\n" +
                "Vui lòng nhấn vào đường dẫn bên dưới để kích hoạt tài khoản:\n\n" +
                linkKichHoat + "\n\n" +
                "Đường dẫn có hiệu lực trong 24 giờ.\n\n" +
                "Trân trọng,\nWeb TMĐT Team"
        );
        mailSender.send(message);
    }
}
