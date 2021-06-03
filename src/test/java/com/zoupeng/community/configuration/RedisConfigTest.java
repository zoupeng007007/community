package com.zoupeng.community.configuration;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisConfigTest {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testStrings() {
        String redisKey = "";
        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "id", "1");
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
    }

    @Test
    public void testList() {
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);
        redisTemplate.opsForList().leftPush(redisKey, 104);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
    }

    @Test
    public void testSet() {
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "张三");
        redisTemplate.opsForSet().add(redisKey, "李四");
        redisTemplate.opsForSet().add(redisKey, "王二");
        redisTemplate.opsForSet().add(redisKey, "麻子");
        redisTemplate.opsForSet().add(redisKey, "麻子", "狗子", "狮子");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        //随机弹出
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));


    }
    @Test
    public void sortSet(){
        String redisKey = "test:student";
        redisTemplate.opsForZSet().add(redisKey,"张三",20);
        redisTemplate.opsForZSet().add(redisKey,"李四",30);
        redisTemplate.opsForZSet().add(redisKey,"王二",40);
        redisTemplate.opsForZSet().add(redisKey,"麻子",90);
        redisTemplate.opsForZSet().add(redisKey,"麻子",100);

        System.out.println(redisTemplate.opsForZSet().size(redisKey));
        //随机弹出
        System.out.println(redisTemplate.opsForZSet().range(redisKey,20,60));
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"麻子"));
        //反过来数
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"麻子"));
        //最大的三个
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));

    }
    @Test
    public void testKeys(){
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:student",10, TimeUnit.SECONDS);
    }

    //多次访问一个key
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.decrement();
        System.out.println(operations.get());
    }


    //编程式事务
    @Test
    public void testTransaction(){
        Object object = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                //启动事务
                redisOperations.multi();
                redisOperations.opsForSet().add(redisKey,"zhangsan","haha");
                redisOperations.opsForSet().add(redisKey,"zhang");
                redisOperations.opsForSet().add(redisKey,"zhangsi");
                redisOperations.opsForSet().add(redisKey,"lisan");
                System.out.println(redisOperations.opsForSet().members(redisKey));


                return redisOperations.exec();//返回的最终key的对应的value结果,也包括每次操作加入的行数
            }
        });

        System.out.println(object);
    }

    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";
        for (int i = 1;i<=100000;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for (int i = 1;i<= 100000 ;i++){
            int r = (int) (Math.random()*100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    //将三组数据合并，再统计去重后的数据总数
    @Test
    public void testHyperLogLogUnion(){
        String rediskey2 = "test:ll:02";
        for (int i = 1;i<=10000;i++){
            redisTemplate.opsForHyperLogLog().add(rediskey2,i);
        }
        String rediskey3 = "test:ll:03";
        for (int i = 5000;i<=15000;i++){
            redisTemplate.opsForHyperLogLog().add(rediskey3,i);
        }
        String rediskey4 = "test:ll:04";
        for (int i = 10001;i<=20000;i++){
            redisTemplate.opsForHyperLogLog().add(rediskey4,i);
        }

        //合并
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,rediskey2,rediskey3,rediskey4);
        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }



    //统计数据的bool值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";

        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));

        //统计
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(execute);
    }
    //统计三组数据的布尔值，并对这三组数据左OR运算
    @Test
    public void testBitMapOperation(){
        String redisKey2="test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2,0,true);
        redisTemplate.opsForValue().setBit(redisKey2,1,true);
        redisTemplate.opsForValue().setBit(redisKey2,2,true);

        String redisKey3="test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey2,2,true);
        redisTemplate.opsForValue().setBit(redisKey2,3,true);
        redisTemplate.opsForValue().setBit(redisKey2,4,true);

        String redisKey4="test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey2,4,true);
        redisTemplate.opsForValue().setBit(redisKey2,5,true);
        redisTemplate.opsForValue().setBit(redisKey2,6,true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
            }
        });

        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,5));

    }
}
