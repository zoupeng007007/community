package com.zoupeng.community.entity;

import lombok.*;

import java.util.Date;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LoginTicket {

    private int id;
    private int userId;
    private String ticket;
    private int status;//0表示有效，1表示失效
    private Date expired;//失效时间
}
