package com.pinyougou.content.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class test {
    @Autowired
    public static RedisTemplate redisTemplate;
    public static void main(String[] args) {

        redisTemplate.boundHashOps("content").delete(1);
    }
}
