package com.isgc.portal.mail;

import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService {
  private static final Logger log = LoggerFactory.getLogger(MailService.class);
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final MailProperties props;

  public MailService(JavaMailSender mailSender, TemplateEngine templateEngine, MailProperties props) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
    this.props = props;
  }

  @Async
  public void sendTemplate(String subject, List<String> to, List<String> cc, String template, Map<String, Object> model) {
    try {
      // Skip if no recipients
      if ((to == null || to.isEmpty()) && (cc == null || cc.isEmpty())) {
        log.debug("Skipping mail send: no recipients, subject={}", subject);
        return;
      }

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
      helper.setFrom(props.from());
      helper.setSubject(subject);
      
      if (to != null && !to.isEmpty()) {
        helper.setTo(to.toArray(new String[0]));
      }
      if (cc != null && !cc.isEmpty()) {
        helper.setCc(cc.toArray(new String[0]));
      }

      Context ctx = new Context();
      ctx.setVariables(model);
      String html = templateEngine.process(template, ctx);
      helper.setText(html, true);
      mailSender.send(message);
      log.debug("Mail sent successfully: subject={}, toCount={}, ccCount={}", subject, to != null ? to.size() : 0, cc != null ? cc.size() : 0);
    } catch (Exception e) {
      log.warn("Mail send failed: subject={}, toCount={}, ccCount={}, template={}",
          subject,
          to != null ? to.size() : 0,
          cc != null ? cc.size() : 0,
          template,
          e
      );
    }
  }

  public List<String> parseEmails(String csvOrSemicolon) {
    if (csvOrSemicolon == null || csvOrSemicolon.isBlank()) return List.of();
    return Arrays.stream(csvOrSemicolon.split("[,;]"))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .toList();
  }
}


