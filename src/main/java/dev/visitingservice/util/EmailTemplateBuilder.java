package dev.visitingservice.util;

import java.time.Year;

public class EmailTemplateBuilder {

    public static String wrap(String contentBody, String subject) {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>%s</title>
        </head>
        <body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;background-color:#f4f4f4;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f4;padding:40px 0;">
                <tr>
                    <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:10px;box-shadow:0 4px 12px rgba(0,0,0,0.1);overflow:hidden;">
                            <tr>
                                <td style="background-color:#2b5adc;padding:20px;text-align:center;">
                                    <h1 style="color:#ffffff;margin:0;">ZenNest</h1>
                                    <p style="color:#dce3f4;font-size:14px;margin:5px 0 0;">%s</p>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:30px 40px;color:#333333;font-size:16px;line-height:1.6;">
                                    %s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:20px;text-align:center;color:#888888;font-size:12px;">
                                    <p style="margin:0;">You're receiving this email from ZenNest because of a scheduled property visit.</p>
                                    <p style="margin:5px 0 0;">© %d ZenNest Africa</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(subject, subject, contentBody, Year.now().getValue());
    }
}