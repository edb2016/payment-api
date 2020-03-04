package com.eb.revolut.payments.db.repository;

public interface RetryStrategy {

    public void waitAndRetry();

    public int getRetryCount();
}
