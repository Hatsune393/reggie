package com.example.reggie.service.impl;

import com.example.reggie.config.EmailProperties;
import com.example.reggie.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    EmailProperties emailProperties;

    @Override
    public boolean sendMail(String to, String head, String text) {
        // 1.构造消息内容
        SimpleMailMessage message = new SimpleMailMessage();
        // 1.1设置发送方，需要与配置中的用户名相同
        message.setFrom(emailProperties.getUsername());
        // 1.2设置接收方
        message.setTo(to);
        // 1.3设置标题与正文
        message.setSubject(head);
        message.setText(text);

        // 2.发送简单短信
        mailSender.send(message);
        return true;
    }
}
