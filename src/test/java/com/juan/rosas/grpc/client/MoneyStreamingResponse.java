package com.juan.rosas.grpc.client;

import com.juan.rosas.grpc.grpcintro.Money;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class MoneyStreamingResponse implements StreamObserver<Money> {

    private final CountDownLatch countDownLatch;

    public MoneyStreamingResponse(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onNext(Money money) {
        System.out.println("Received async: " + money.getValue());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Done!!");
        countDownLatch.countDown();

    }
}
