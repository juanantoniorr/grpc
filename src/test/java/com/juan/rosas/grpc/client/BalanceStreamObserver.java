package com.juan.rosas.grpc.client;

import com.juan.rosas.grpc.grpcintro.Balance;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class BalanceStreamObserver implements StreamObserver<Balance> { //Manejo el balance que es la respuesta del server
    private CountDownLatch countDownLatch;

    public BalanceStreamObserver(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    //Cuando recibo la respuesta del server imprimo el balance
    @Override
    public void onNext(Balance balance) {
        System.out.println("Final balance " + balance.getAmount());
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        System.out.println("Server is done!!");
        countDownLatch.countDown();
    }
}
