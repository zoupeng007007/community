package com.zoupeng.community.entity;

import lombok.*;

import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Message {

    private int id;
    private int fromId;
    private int toId;
    //会话Id，要求Id小的在前  id1_id2
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}
