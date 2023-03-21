package com.example.reggie.service;

public interface MailService {
    boolean sendMail(String targetMail, String head, String text);
}
