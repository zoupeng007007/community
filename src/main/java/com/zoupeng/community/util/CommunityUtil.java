package com.zoupeng.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    // 生成随机的字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密,只能加密不能解密
    // hello -> abc123def5
    // hello + 3e48a -> abc123def55a56
    public static String md5(String key){
        /*是否为空 包括null 空格*/
        if (StringUtils.isBlank(key)){
            return null;
        }else {
            return DigestUtils.md5DigestAsHex(key.getBytes());
        }
    }
}
