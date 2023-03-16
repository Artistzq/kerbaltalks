package com.chinaero.kerbaltalks.service;

import com.chinaero.kerbaltalks.dao.LoginTicketMapper;
import com.chinaero.kerbaltalks.dao.UserMapper;
import com.chinaero.kerbaltalks.entity.LoginTicket;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import com.chinaero.kerbaltalks.util.MailClient;
import com.chinaero.kerbaltalks.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements KerbaltalksConstant {

    private final UserMapper userMapper;

    private final MailClient mailClient;

    private final TemplateEngine templateEngine;

    private final LoginTicketMapper loginTicketMapper;

    private final RedisTemplate redisTemplate;

    @Value("${kerbaltalks.path.domain}")
    private  String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    boolean useCache = true;

    public UserService(MailClient mailClient, UserMapper userMapper, TemplateEngine templateEngine, LoginTicketMapper loginTicketMapper, RedisTemplate redisTemplate) {
        this.mailClient = mailClient;
        this.userMapper = userMapper;
        this.templateEngine = templateEngine;
        this.loginTicketMapper = loginTicketMapper;
        this.redisTemplate = redisTemplate;
    }

    public User findUserById(int id) {
        if (useCache) {
            return findUserByIdFromCache(id);
        } else {
            return findUserByIdFromMysql(id);
        }
    }

    private User findUserByIdFromMysql(int id) {
        return userMapper.selectById(id);
    }

    private User findUserByIdFromCache(int id) {
        User user = getUserFromCache(id);
        if (user == null) {
            user = getUserFromCacheAndSetCache(id);
        }
        return user;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
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
            if (useCache)
                flushUserCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "Username can not be empty");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "Password can not be empty");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "Account do not exist.");
            return map;
        }

        // 验证账号
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "Account not activated.");
            return map;
        }

        // 验证密码
        password = KerbaltalksUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "Password is not correct.");
            return map;
        }

        // 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(KerbaltalksUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        // 用Redis存登陆凭证
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket)redisTemplate.opsForValue().get(redisKey);
        return loginTicket;
    }

    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        if (useCache)
            flushUserCache(userId);
        return rows;
    }

    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword, String confirmPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "Old password can not be empty");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "New password can not be empty");
            return map;
        }
        if (StringUtils.isBlank(confirmPassword)) {
            map.put("confirmPasswordMsg", "Confirm password can not be empty");
            return map;
        }

        // 确认处理
        if (! newPassword.equals(confirmPassword)) {
            map.put("newPasswordMsg", "New password and confirm password must be same.");
            return map;
        }

        // 检查原密码
        User user = userMapper.selectById(userId);
        if (user == null) {
            map.put("usernameMsg", "Account do not exist.");
            return map;
        }
        if (! KerbaltalksUtil.md5(oldPassword + user.getSalt()).equals(user.getPassword())) {
            map.put("oldPasswordMsg", "原密码错误！");
            return map;
        }

        // 设置新密码
        userMapper.updatePassword(userId, KerbaltalksUtil.md5(newPassword + user.getSalt()));
        if (useCache)
            flushUserCache(userId);
        return map;
    }

    // 从缓存中取User
    private User getUserFromCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 取不到时从数据库取，并初始化缓存
    private User getUserFromCacheAndSetCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 数据变动时删除缓存
    private void flushUserCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Map<String, Object> login_deprecated(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "Username can not be empty");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "Password can not be empty");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "Account do not exist.");
            return map;
        }

        // 验证账号
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "Account not activated.");
            return map;
        }

        // 验证密码
        password = KerbaltalksUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "Password is not correct.");
            return map;
        }

        // 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(KerbaltalksUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout_deprecated(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket_deprecated(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

}
