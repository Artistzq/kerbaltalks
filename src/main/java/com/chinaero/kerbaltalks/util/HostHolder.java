package com.chinaero.kerbaltalks.util;

import com.chinaero.kerbaltalks.entity.User;
import org.springframework.stereotype.Component;

/**
 * 容器，持有用户信息，用于代替session信息.
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }

}
