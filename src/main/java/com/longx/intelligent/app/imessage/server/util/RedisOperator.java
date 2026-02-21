package com.longx.intelligent.app.imessage.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisOperator {
    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOperations;
    private final SetOperations<String, Object> setOperations;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisOperator(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.setOperations = redisTemplate.opsForSet();
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setWithExpiration(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void expireForValue(String key, long timeout, TimeUnit unit){
        redisTemplate.opsForValue().getOperations().expire(key, timeout, unit);
    }

    public void expireForSet(String key, long timeout, TimeUnit unit){
        redisTemplate.opsForSet().getOperations().expire(key, timeout, unit);
    }

    public void expireForHash(String key, long timeout, TimeUnit unit){
        redisTemplate.opsForHash().getOperations().expire(key, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void hSet(String key, String field, Object value) {
        hashOperations.put(key, field, value);
    }

    public Object hGet(String key, String field) {
        return hashOperations.get(key, field);
    }

    public Map<String, Object> hGetAll(String key) {
        return hashOperations.entries(key);
    }

    public void sAdd(String key, Object... values) {
        setOperations.add(key, values);
    }

    public Set<Object> sMembers(String key) {
        return setOperations.members(key);
    }

    public void sRemove(String key, Object... values) {
        setOperations.remove(key, values);
    }

    public void increment(String key){
        redisTemplate.opsForValue().increment(key);
    }

    public Long getExpire(String key, TimeUnit timeUnit){
        return redisTemplate.getExpire(key, timeUnit);
    }

    public Set<String> keys(String pattern){
        return redisTemplate.keys(pattern);
    }

    public Set<String> stringKeys(String pattern){
        return stringRedisTemplate.keys(pattern);
    }

    public Boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }

    public void renameKey(String oldKey, String newKey) {
        Boolean success = stringRedisTemplate.renameIfAbsent(oldKey, newKey);
        if (Boolean.FALSE.equals(success)) {
            throw new RuntimeException("新 key 已存在或旧 key 不存在");
        }
    }

    public boolean sContains(String key, Object value) {
        return Boolean.TRUE.equals(setOperations.isMember(key, value));
    }

}
