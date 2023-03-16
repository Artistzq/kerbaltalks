package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.FollowService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 构造器注入的好处
 * 单一职责: 当使用构造函数注入的时候，你会很容易发现参数是否过多，这个时候需要考虑你这个类的职责是否过大，考虑拆分的问题；而当使用@Autowired注入field的时候，不容易发现问题
 *
 * 依赖不可变: 只有使用构造函数注入才能注入final
 *
 * 依赖隐藏:使用依赖注入容器意味着类不再对依赖对象负责，获取依赖对象的职责就从类抽离出来，IOC容器会帮你自动装备。这意味着它应该使用更明确清晰的公用接口方法或者构造器，这种方式就能很清晰的知道类需要什么和到底是使用setter还是构造器
 *
 * 降低容器耦合度: 依赖注入框架的核心思想之一是托管类不应依赖于所使用的DI容器。换句话说，它应该只是一个普通的POJO，只要您将其传递给所有必需的依赖项，就可以独立地实例化。这样，您可以在单元测试中实例化它，而无需启动IOC容器并单独进行测试（使用一个可以进行集成测试的容器）。如果没有容器耦合，则可以将该类用作托管或非托管类，甚至可以切换到新的DI框架。
 */

@Controller
public class FollowController {

    private final FollowService followService;
    private final HostHolder hostHolder;


    public FollowController(FollowService followService, HostHolder hostHolder) {
        this.followService = followService;
        this.hostHolder = hostHolder;
    }

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        return KerbaltalksUtil.getJSONString(1, "已关注!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return KerbaltalksUtil.getJSONString(1, "已取消关注!");
    }


}
