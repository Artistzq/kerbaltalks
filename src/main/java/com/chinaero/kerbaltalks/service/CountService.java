package com.chinaero.kerbaltalks.service;

import com.chinaero.kerbaltalks.dao.CountMapper;
import com.chinaero.kerbaltalks.dao.DiscussPostMapper;
import org.springframework.stereotype.Service;

/**
 * @Author : Artis Yao
 */
@Service
public class CountService {

    private final CountMapper countMapper;

    private final DiscussPostMapper discussPostMapper;

    public CountService(CountMapper countMapper, DiscussPostMapper discussPostMapper) {
        this.countMapper = countMapper;
        this.discussPostMapper = discussPostMapper;
    }

    public void incPostCount() {
        countMapper.incCount("discuss_post");
    }

    public void syncPostCount() {
        int counts = discussPostMapper.selectDiscussPostRows(0);
        countMapper.setCount("discuss_post", counts);
    }

}
