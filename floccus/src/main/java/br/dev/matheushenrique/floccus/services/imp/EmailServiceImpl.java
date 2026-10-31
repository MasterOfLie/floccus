package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Override
    public void enviarRecuperacaoSenha(String destinatario, String link) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject("Recuperação de senha - Floccus");

            String html = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:32px 12px;background-color:#fcfcfc;
                         font-family:Arial,Helvetica,sans-serif;color:#18181b;">

                <table role="presentation" width="100%%" cellspacing="0"
                       cellpadding="0">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="100%%" cellspacing="0"
                                   cellpadding="0"
                                   style="max-width:520px;background:#ffffff;
                                          border:1px solid #e4e4e7;
                                          border-radius:8px;">

                                <tr>
                                    <td style="padding:24px 28px;background:#18181b;
                                               border-radius:7px 7px 0 0;">
                                        <div style="font-size:24px;font-weight:600;
                                                    color:#ffffff;">
                                            floccus
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td style="padding:28px;">
                                        <h1 style="font-size:21px;font-weight:600;
                                                   margin:0 0 20px;color:#18181b;">
                                            Redefinição de senha
                                        </h1>

                                        <p style="font-size:14px;line-height:1.7;
                                                  margin:0 0 12px;">
                                            Olá!
                                        </p>

                                        <p style="font-size:14px;line-height:1.7;
                                                  color:#71717a;margin:0 0 12px;">
                                            Recebemos uma solicitação para redefinir
                                            a senha da sua conta Floccus.
                                        </p>

                                        <p style="font-size:14px;line-height:1.7;
                                                  color:#71717a;margin:0 0 24px;">
                                            Clique no botão abaixo para cadastrar
                                            uma nova senha.
                                        </p>

                                        <table role="presentation" cellspacing="0"
                                               cellpadding="0" style="margin-bottom:24px;">
                                            <tr>
                                                <td bgcolor="#18181b"
                                                    style="border-radius:4px;">
                                                    <a href="%s"
                                                       style="display:inline-block;
                                                              padding:13px 22px;
                                                              color:#ffffff;
                                                              font-size:14px;
                                                              font-weight:600;
                                                              text-decoration:none;">
                                                        Redefinir minha senha
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>

                                        <div style="padding:14px 16px;
                                                    background:#fafafa;
                                                    border:1px solid #e4e4e7;
                                                    border-radius:6px;">
                                            <p style="font-size:13px;font-weight:600;
                                                      margin:0 0 6px;color:#18181b;">
                                                Atenção
                                            </p>
                                            <p style="font-size:13px;line-height:1.6;
                                                      margin:0;color:#71717a;">
                                                O link expira em 15 minutos e só
                                                pode ser utilizado uma vez.
                                            </p>
                                        </div>

                                        <p style="font-size:13px;line-height:1.7;
                                                  color:#71717a;margin:24px 0 0;">
                                            Se você não solicitou a recuperação,
                                            ignore este e-mail.
                                        </p>

                                        <p style="font-size:12px;line-height:1.6;
                                                  color:#a1a1aa;margin:24px 0 0;
                                                  padding-top:18px;
                                                  border-top:1px solid #e4e4e7;">
                                            © Floccus<br>
                                            Mensagem automática, não responda
                                            a este e-mail.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(link);

            helper.setText(html, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar o e-mail de recuperação", e);
        }
    }
}
