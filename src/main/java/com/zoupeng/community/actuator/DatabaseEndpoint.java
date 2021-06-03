package com.zoupeng.community.actuator;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "database")
@Slf4j
public class DatabaseEndpoint {

    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;

    //表示只能通过get请求访问
    @ReadOperation
    public Map<String,Object> checkConnection() {
        Map<String,Object> map = new HashMap<>();
        try (
                Connection conn = dataSource.getConnection();
        ) {
            map.put("code",0);
            map.put("msg","获取连接成功");
            return  map;
        } catch (SQLException e) {
            log.error("获取连接失败");
            map.put("code",1);
            map.put("msg","获取连接失败");
            return map;
        }
    }

}
