package com.eb.revolut.payments.db.repository.impl;

import com.eb.revolut.payments.db.repository.RetryStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RetryStrategyImpl implements RetryStrategy {

    private AtomicInteger retryCount;

    private long sleepTime;

    public RetryStrategyImpl(int retryCount, long sleepTime) {
        this.retryCount = new AtomicInteger(retryCount);
        this.sleepTime = sleepTime;
    }

    @Override
    public void waitAndRetry() {
        retryCount.decrementAndGet();
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error("Error occurred during waitAndRetry", e);
        }
    }

    @Override
    public int getRetryCount() {
        return retryCount.get();
    }

}
