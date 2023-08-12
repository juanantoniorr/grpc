package com.juan.rosas.grpc.server;

import com.juan.rosas.grpc.grpcintro.Account;
import com.juan.rosas.grpc.grpcintro.TransferRequest;
import com.juan.rosas.grpc.grpcintro.TransferResponse;
import com.juan.rosas.grpc.grpcintro.TransferStatus;
import io.grpc.stub.StreamObserver;

public class TransferStreamingRequest implements StreamObserver<TransferRequest> {

    private StreamObserver<TransferResponse> transferResponseStreamObserver;

    public TransferStreamingRequest(StreamObserver<TransferResponse> transferResponseStreamObserver) {
        this.transferResponseStreamObserver = transferResponseStreamObserver;
    }

    //Por cada request voy a responder porque es streaming, no me espero al onCompleted
    @Override
    public void onNext(TransferRequest transferRequest) {
        int fromAccountNumber = transferRequest.getFromAccount();
        int toAccountNumber = transferRequest.getToAccount();
        int amountToTransfer = transferRequest.getAmount();
        int balanceFromAccount = AccountDatabase.getBalance(fromAccountNumber);
        TransferStatus status = TransferStatus.FAILED;
        if (balanceFromAccount>= amountToTransfer && fromAccountNumber != toAccountNumber){
            AccountDatabase.deductBalance(fromAccountNumber, amountToTransfer);
            AccountDatabase.addBalance(toAccountNumber, amountToTransfer);
            status = TransferStatus.SUCCESS;
        }
        int newBalance = AccountDatabase.getBalance(toAccountNumber);
        Account fromAccount = Account.newBuilder()
                .setAccountNumber(fromAccountNumber)
                .setAmount(balanceFromAccount)
                .build();

        Account toAccount = Account.newBuilder()
                .setAccountNumber(toAccountNumber)
                .setAmount(newBalance)
                .build();


        //Tengo que responder despues de cada request porque es streaming
        TransferResponse transferResponse = TransferResponse.newBuilder()
                .setStatus(status)
                .addAccounts(fromAccount)
                .addAccounts(toAccount)
                .build();
        //We send the response to the client for every request
        transferResponseStreamObserver.onNext(transferResponse);

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        //We notify to client that we are done processing
        transferResponseStreamObserver.onCompleted();

        System.out.println("Server is done processing");

        AccountDatabase.printAccountDetails();
    }
}
