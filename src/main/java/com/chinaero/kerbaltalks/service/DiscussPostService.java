package com.chinaero.kerbaltalks.service;

import com.chinaero.kerbaltalks.controller.DiscussPostController;
import com.chinaero.kerbaltalks.dao.CountMapper;
import com.chinaero.kerbaltalks.dao.DiscussPostMapper;
import com.chinaero.kerbaltalks.entity.DiscussPost;
import com.chinaero.kerbaltalks.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    private final DiscussPostMapper discussPostMapper;

    private final CountMapper countMapper;

    private final SensitiveFilter sensitiveFilter;

    private final TransactionTemplate transactionTemplate;

    public DiscussPostService(DiscussPostMapper discussPostMapper, CountMapper countMapper, SensitiveFilter sensitiveFilter, PlatformTransactionManager platformTransactionManager) {
        this.discussPostMapper = discussPostMapper;
        this.countMapper = countMapper;
        this.sensitiveFilter = sensitiveFilter;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private  int expireSeconds;

    // caffeine核心接口：Cache，LoadingCache：同步缓存，AsyncLoadingCache：异步缓存

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;
    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("缓存键错误！");
                        }

                        String[] params = key.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("缓存键错误！");
                        }

                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);

                        // 二级缓存，redis

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit);
                    }
                });
        // 初始化帖子数量缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
//        return postListCache.get(offset + ":" + limit);
        if (userId == 0) {
            return postListCache.get(offset + ":" + limit);
        }

        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
//        return postRowsCache.get(userId);
        if (userId == 0) {
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows form DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        AtomicInteger result = new AtomicInteger();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    result.set(discussPostMapper.insertDiscussPost(post));
                    countMapper.incCount("discuss_post");
                } catch (Exception e) {
                    logger.error("commit error");
                    status.setRollbackOnly();
                }
            }
        });

        postListCache.invalidateAll();
        postRowsCache.invalidateAll();

        return result.get();
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }
}
