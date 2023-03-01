package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.UserService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${kerbaltalks.path.domain}")
    private String domain;
    @Value("${kerbaltalks.path.upload-path}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    private final HostHolder hostHolder;
    private final UserService userService;

    public UserController(HostHolder hostHolder, UserService userService) {
        this.hostHolder = hostHolder;
        this.userService = userService;
    }


    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @RequestMapping(path = "/upload_header", method = RequestMethod.POST)
    public String uploadPath(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "还未选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        assert filename != null;
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = KerbaltalksUtil.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器写入异常", e);
        }

        // 更新头像路径（web访问路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;

        // 声明格式
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片，字节流
        response.setContentType("image/" + suffix);
        try (OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(fileName)) {
            byte[] buffer = new byte[1025];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer,0, b);
            }
        } catch (IOException e) {
            logger.error("读取图像失败：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}