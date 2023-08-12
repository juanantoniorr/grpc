package com.juan.rosas.grpc.client;

import com.juan.rosas.grpc.grpcintro.TransferResponse;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class TransferStreamingResponse implements StreamObserver<TransferResponse> {

    private CountDownLatch countDownLatch;

    public TransferStreamingResponse(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onNext(TransferResponse transferResponse) {
        System.out.println("On next client");
        System.out.println("Status " + transferResponse.getStatus());
        transferResponse.getAccountsList()
                .stream()
                .map(account -> account.getAccountNumber() + ": " + account.getAmount())
                .forEach(System.out::println);
        System.out.println("-------------------------");

    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error in client");
    countDownLatch.countDown();
    }

    //Este metodo del lado del cliente se manda a llamar ya que todos los requests streaming se hayan terminado
    @Override
    public void onCompleted() {
        System.out.println("All transfers done!!");
        countDownLatch.countDown();

    }
}
