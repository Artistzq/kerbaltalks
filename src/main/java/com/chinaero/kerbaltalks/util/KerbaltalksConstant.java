package com.chinaero.kerbaltalks.util;

public interface KerbaltalksConstant {

    /**
     * 激活成功
     */
    public static final int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    public static final int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    public static final int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登陆凭证的超时时间
     */
    public static final int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的凭证
     */
    public static final int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;

    /**
     * 实体类型：帖子的评论
     */
    public static final int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论的评论
     */
    public static final int ENTITY_TYPE_COMMENT = 2;
}
