package com.chinaero.kerbaltalks;


import com.chinaero.kerbaltalks.dao.DiscussPostMapper;
import com.chinaero.kerbaltalks.dao.LoginTicketMapper;
import com.chinaero.kerbaltalks.dao.MessageMapper;
import com.chinaero.kerbaltalks.dao.UserMapper;
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

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

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
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        list.forEach(System.out::println);

        int rows = discussPostMapper.selectDiscussPostRows(149);
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


}
