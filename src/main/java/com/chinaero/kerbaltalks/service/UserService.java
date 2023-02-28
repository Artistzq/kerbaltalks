package com.chinaero.kerbaltalks.service;

import com.chinaero.kerbaltalks.dao.UserMapper;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import com.chinaero.kerbaltalks.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements KerbaltalksConstant {

    private final UserMapper userMapper;

    private final MailClient mailClient;

    private final TemplateEngine templateEngine;

    @Value("${kerbaltalks.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public UserService(MailClient mailClient, UserMapper userMapper, TemplateEngine templateEngine) {
        this.mailClient = mailClient;
        this.userMapper = userMapper;
        this.templateEngine = templateEngine;
    }

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 激活逻辑
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理 （账号，邮箱和密码）
        if (user == null) {
            throw new IllegalArgumentException("Null Args.");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "Username can not be empty.");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "Password can not be empty.");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("mailMsg", "Mail can not be empty.");
            return map;
        }

        // 验证账号有效性（账号，邮箱）
        User anotherUser = userMapper.selectByName(user.getUsername());
        if (anotherUser != null) {
            map.put("usernameMsg", "Username exists already.");
            return map;
        }
        anotherUser = userMapper.selectByEmail(user.getEmail());
        if (anotherUser != null) {
            map.put("mailMsg", "Mail exists already.");
            return map;
        }

        // 注册用户
        user.setSalt(KerbaltalksUtil.generateUUID().substring(0, 5));
        user.setPassword(KerbaltalksUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(KerbaltalksUtil.generateUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "KerbalTalks 账号激活", content);

        // 空map，没有问题
        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1)
            return ACTIVATION_REPEAT;
        else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

}
