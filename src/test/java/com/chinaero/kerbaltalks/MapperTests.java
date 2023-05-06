package com.chinaero.kerbaltalks;


import com.alibaba.fastjson2.JSONObject;
import com.chinaero.kerbaltalks.dao.*;
import com.chinaero.kerbaltalks.entity.DiscussPost;
import com.chinaero.kerbaltalks.entity.LoginTicket;
import com.chinaero.kerbaltalks.entity.Message;
import com.chinaero.kerbaltalks.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.HtmlUtils;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = KerbaltalksApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setSalt("abv");
        user.setEmail("a@qq.com");
        user.setHeaderUrl("http://www.com");
        user.setPassword("123");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());

    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "aaa.com");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "helloworld");
        System.out.println(rows);

    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 200000, 10);
        list.forEach(System.out::println);

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        messages.forEach(System.out::println);

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        List<Message> list = messageMapper.selectLetters("111_112", 0, 10);
        list.forEach(System.out::println);

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectUnreadLetterCount(131, "111_131");
        System.out.println(count);
    }

    @Autowired
    CountMapper countMapper;

    @Test
    public void testCount() {
        countMapper.incCount("discuss_post");
        countMapper.setCount("discuss_post", 300154);
    }

    @Test
    public  void testNotice() {
        Message message = messageMapper.selectLatestNotice(234, "like");
//        Message message = messageService.findLatestNotice(userId, topic);
        Map<String, Object> messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {

            // content是存在转义字符的json字符串
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
//            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

//            int count = messageService.findNoticeCount(userId, topic);
//            messageVO.put("count", count);
//            int unreadCount = messageService.findUnreadNoticeCount(userId, topic);
//            messageVO.put("unreadCount", unreadCount);
        }
//        return messageVO;
        messageVO.forEach((k, v) -> System.out.println(k + ": " + v));
    }
}
