package com.sarmich.timetable.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class EmailService {

  private JavaMailSender mailSender;

  /**
   * Foydalanuvchiga verifikatsiya kodini HTML formatida yuboradi.
   *
   * @param to Foydalanuvchi email manzili
   */
  public void sendVerificationEmail(String to, Integer verificationCode) {
    String subject = "Verification code for email";
    String htmlContent = buildHtmlForVerification(verificationCode);
    try {
      sendHtmlEmail(to, subject, htmlContent);
    } catch (MessagingException e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * HTML-kontentli email yuborish uchun yordamchi metod.
   *
   * @param to Qabul qiluvchi manzili
   * @param subject Xabar mavzusi
   * @param htmlBody Xabar tanasi (HTML)
   * @throws MessagingException Email yuborishda xatolik yuzaga kelsa
   */
  private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true); // `true` parametri kontent HTML ekanligini bildiradi

    mailSender.send(mimeMessage);
  }

  /**
   * Verifikatsiya kodi uchun HTML shablonni String formatida yaratadi.
   *
   * @param code Yuboriladigan verifikatsiya kodi.
   * @return To'liq HTML-sahifa ko'rinishidagi String.
   */
  private String buildHtmlForVerification(Integer code) {
    // HTML shablonni String sifatida yig'amiz
    return "<!DOCTYPE html>"
        + "<html lang='en'>"
        + "<head>"
        + "<meta charset='UTF-8'>"
        + "<title>Verify your account</title>"
        + "<style>"
        + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }"
        + ".container { max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }"
        + ".header { background-color: #007bff; color: #ffffff; padding: 10px; border-radius: 8px 8px 0 0; text-align: center; }"
        + ".content { padding: 30px; text-align: center; }"
        + ".code { font-size: 32px; font-weight: bold; color: #333; letter-spacing: 5px; padding: 15px; border: 2px dashed #007bff; display: inline-block; margin: 20px 0; }"
        + ".footer { margin-top: 20px; font-size: 12px; color: #777; text-align: center; }"
        + "</style>"
        + "</head>"
        + "<body>"
        + "<div class='container'>"
        + "<div class='header'><h1>Verify your account</h1></div>"
        + "<div class='content'>"
        + "<p>Hello!</p>"
        + "<p>Please use the following verification code to activate your account:</p>"
        + "<div class='code'>"
        + code
        + "</div>"
        + "<p>If you did not register, please ignore this message.</p>"
        + "</div>"
        + "<div class='footer'><p>&copy; 2026 WIUT BISP</p></div>"
        + "</div>"
        + "</body>"
        + "</html>";
  }
}
