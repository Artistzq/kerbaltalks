package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.annotation.LoginRequired;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.FollowService;
import com.chinaero.kerbaltalks.service.LikeService;
import com.chinaero.kerbaltalks.service.UserService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
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
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements KerbaltalksConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${kerbaltalks.path.domain}")
    private String domain;
    @Value("${kerbaltalks.path.upload-path}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${server.tomcat.basedir}")
    private String basedir;

    private final HostHolder hostHolder;
    private final UserService userService;
    private final LikeService likeService;
    private final FollowService followService;

    public UserController(HostHolder hostHolder, UserService userService, LikeService likeService, FollowService followService) {
        this.hostHolder = hostHolder;
        this.userService = userService;
        this.likeService = likeService;
        this.followService = followService;
    }

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload_header", method = RequestMethod.POST)
    public String uploadPath(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "还未选择图片");
            return "site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        assert filename != null;
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "site/setting";
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
        String prefix = basedir + "/work/Tomcat/localhost/kerbaltalks/";
        fileName = prefix + uploadPath + "/" + fileName;
//        System.out.println(fileName);

        // 声明格式
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
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

    @RequestMapping(path = "/update_password", method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword, String newPassword, String confirmPassword) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword, confirmPassword);
        if (map == null || map.isEmpty()) {
            // 修改成功
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
            return "site/setting";
        }
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProgilePage(Model model, @PathVariable("userId") int userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        model.addAttribute("user", user);
        model.addAttribute("likeCount", likeService.findUserLikeCount(userId));


        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "site/profile";
    }

}
