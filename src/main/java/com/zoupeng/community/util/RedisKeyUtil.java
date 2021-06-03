package com.zoupeng.community.util;


//获取Redis的key
public class RedisKeyUtil {
    //分隔符
    private static final String SPLIT = ":";
    //点赞的key的前缀  实体：帖子，评论
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    //点赞key前缀   用户
    private static final String PREFIX_USER_LIKE = "like:user";
    //某个实体(评论，回复...)的赞，key
    //关注功能的实体
    private static final String PREFIX_FOLLOWEE = "followee";//用户的关注
    private static final String PREFIX_FOLLOWER = "follower";//某个用户的粉丝
    //验证码key前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //登录凭证 key前缀
    private static final String PREFIX_TICKET = "ticket";
    //用户登录信息key前缀
    private static final String PREFIX_USER = "user:";
    //UV前缀
    private static final String PREFIX_UV = "uv";
    //DAU
    private static final String PREFIX_DAU = "dau";
    //帖子前缀。
    private static final String PREFIX_POST = "post";


    //like:entity:entityType:entityId  -> set(userId),set中存的是userId，虽然可以用String,但为了以后可以知道谁点了赞，考虑用set
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户收到的赞
    //like:user:userId
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的某个实体类型  的某些实体   以当前时间作为排序
    //followee:userId:entityType  -> zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的用户粉丝
    //follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登录验证码key
    //kaptcha:owner
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录凭证
    //ticket:LoginTicket
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    //单日访问量UV
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    //区间UV
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日活跃用户DAU
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    //区间DAU
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //统计帖子分数的key
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
