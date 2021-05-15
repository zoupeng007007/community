package com.zoupeng.community.util;

import com.sun.istack.internal.FinalArrayList;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    final int ACTIVATION_SUCCESS = 0;
    /**
     * 重复激活
     */
    final int ACTIVATION_REPEATED = 1;
    /**
     * 激活失败
     */
    final int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     * 12h
     */

    final int DEFAULT_EXPIRED_SECONDS = 3600*12;

    /**
     * 记住状态下的登录凭证超时时间
     * 100day
     */
    final int REMEMBER_EXPIRED_SECONDS = 3600 *24 *100;
}
