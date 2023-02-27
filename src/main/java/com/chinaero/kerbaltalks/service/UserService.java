package com.chinaero.kerbaltalks.service;

import com.chinaero.kerbaltalks.dao.UserMapper;
import com.chinaero.kerbaltalks.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

}
