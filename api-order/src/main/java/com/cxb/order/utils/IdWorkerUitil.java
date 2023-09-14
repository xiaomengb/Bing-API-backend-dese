package com.cxb.order.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * redis 生成全局唯一id
 *
 * @author cxb
 */
@Component
public class IdWorkerUitil {


    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 开始时间戳 2023.9.1
     */
    private static final long BEGIN_TIMESTAMP = 1693526400L;

    /**
     * 序列号位数
     */
    private static final int COUNT_BITS = 32;

    public IdWorkerUitil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPreFix) {
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        //2.利用redis自增生成序列号
        //2.1 当前日期
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //2.2 redis自增
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPreFix + ":" + date);

        //3.拼接生成id
        return timeStamp << COUNT_BITS | count;

    }


    /*public static void main(String[] args) {
        LocalDateTime dateTime = LocalDateTime.of(2023, 9, 1, 0, 0, 0);
        //second为现在时间与纪元时间相差秒数，ZoneOffset.UTC 0时区
        long second = dateTime.toEpochSecond(ZoneOffset.UTC);
        System.out.println(second);
    }*/

}
