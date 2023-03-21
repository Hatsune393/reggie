package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.R;
import com.example.reggie.common.util.ValidateCodeUtils;
import com.example.reggie.entity.User;
import com.example.reggie.exception.ExceptionDef;
import com.example.reggie.exception.UserException;
import com.example.reggie.service.MailService;
import com.example.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final String emailSubject = "瑞吉外卖验证码提醒";
    private final String emailTemplate = "【瑞吉外卖】您好，您的验证码为%s，有效期五分钟";
    private final Integer validateCodeLen = 4;
    private final String VALIDATE_CODE = "validate_code";
    private final String EXPIRE_TIME = "expire_time";
    @Value("${validate-code.expire-time}")
    private final Integer validate_code_expire_minutes = 5;

    @Autowired
    MailService mailService;

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody Map<String, String> map, HttpSession session) {
        String email = map.get("email");
        // 1.随机生成四位数验证码
        Integer code = ValidateCodeUtils.generateValidateCode(validateCodeLen);

        // 2.将验证码保存到redis，供登陆时验证，并设置过期时间
        String key = "userCache_" + email;
        redisTemplate.opsForValue().set(key, code.toString());
        redisTemplate.expire(key, validate_code_expire_minutes, TimeUnit.MINUTES);

        // 3.拼凑邮件正文
        String text = String.format(emailTemplate, code.toString());

        // 4.发送邮件
        mailService.sendMail(email, emailSubject, text);
        return R.success("发送验证码成功");
    }

    @PostMapping("/login")
    public R<String> login(HttpSession session, @RequestBody Map map) {
        // 1.获取用户验证码，与redis进行比对
        String code = (String) map.get("code");
        String email = (String) map.get("email");
        String tCode = redisTemplate.opsForValue().get("userCache_" + email);
        if (!code.equals(tCode) ) {
            throw new UserException(ExceptionDef.VALIDATE_CODE_DIFFER_ERR);
        }

        // 2.判断当前用户是否在数据库已存在，不存在则创建用户
        // 2.1构造查询条件，根据邮件查询
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        // 2.2查询用户是否存在
        User user = userService.getOne(queryWrapper);
        // 2.3用户不存在，则根据邮箱创建新用户
        if (user == null) {
            User newUser = new User();
            newUser.setEmail(email);
            userService.save(newUser);
        }

        // 3.获取当前用户id，并将id保存到用户session域
        Long userId = userService.getOne(queryWrapper).getId();
        session.setAttribute("user_id", userId);

        // 4.返还用户邮箱信息
        return R.success(email);
    }
}
