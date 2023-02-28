package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.config.KaptchaConfig;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.UserService;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import com.google.code.kaptcha.Producer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements KerbaltalksConstant {

    public static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserService userService;

    private final Producer kaptchaProducer;

    @Value("server.servlet.context-path")
    private String contextPath;

    public LoginController(UserService userService, Producer kaptchaProducer) {
        this.userService = userService;
        this.kaptchaProducer = kaptchaProducer;
    }

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功！我们已经向您的邮箱发送了一封激活邮件，请激活后使用账号。");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("mailMsg", map.get("mailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable int userId, @PathVariable String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功！您的帐号已经可以正常使用。");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作！该账号已经激活。");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败！错误的激活码。");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        session.setAttribute("kaptcha", text);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try (OutputStream outputStream = response.getOutputStream()){
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code,  boolean rememberMe,
                        Model model, HttpSession session, HttpServletResponse response) {
        // 首先判断验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }

        // 检查账号密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS: DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }

}
