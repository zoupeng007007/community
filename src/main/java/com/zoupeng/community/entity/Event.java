package com.zoupeng.community.entity;


import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 触发事件
 * 用户封装事件
 */

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Event {
    private String topic;//事件
    private int userId;//触发事件的用户
    private int entityType;//事件的类型
    private int entityId;    //事件实体的id  entityId
    private int entityUserId;//系统消息发送的对象，entityUserId
    private Map<String, Object> data = new HashMap<>();//为了通用性


    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
