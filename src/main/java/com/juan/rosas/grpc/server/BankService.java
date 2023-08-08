package com.juan.rosas.grpc.server;
import com.juan.rosas.grpc.grpcintro.Balance;
import com.juan.rosas.grpc.grpcintro.BalanceCheckRequest;
import com.juan.rosas.grpc.grpcintro.BankServiceGrpc;
import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(accountNumber * 10)
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();

    }
}
