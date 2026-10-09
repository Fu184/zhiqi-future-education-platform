package com.zhiqi.platform.auth;

import static com.zhiqi.platform.common.ApiException.check;

import com.zhiqi.platform.common.ApiException;
import com.zhiqi.platform.common.OperationLocks;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RateLimiter {
    private final RateLimitRepository repository;
    private final OperationLocks locks;

    public RateLimiter(RateLimitRepository repository, OperationLocks locks) {
        this.repository = repository;
        this.locks = locks;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, noRollbackFor = ApiException.class)
    public void consume(String key, int maximum, long duration) {
        locks.acquire("rate:" + key);
        long now = System.currentTimeMillis();
        var state = repository.find(key);
        if (state != null && state.resetAt() > now) {
            check(state.count() < maximum, 429, "请求过于频繁，请稍后重试");
            repository.increment(key);
        } else {
            repository.reset(key, now + duration);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void clear(String key) {
        locks.acquire("rate:" + key);
        repository.clear(key);
    }
}
