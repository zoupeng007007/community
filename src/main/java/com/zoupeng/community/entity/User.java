package com.zoupeng.community.entity;

import lombok.*;
import java.util.Date;


@EqualsAndHashCode
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String username = null;
    private String password = null;
    private String salt = null;
    private String email = null;
    private int type;
    private int status;//激活/或者未激活
    private String activationCode = null;
    private String headerUrl = null;
    private Date createTime = null;
}
