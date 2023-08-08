package com.juan.rosas.grpc.server;
import com.juan.rosas.grpc.grpcintro.Balance;
import com.juan.rosas.grpc.grpcintro.BalanceCheckRequest;
import com.juan.rosas.grpc.grpcintro.BankServiceGrpc;
import io.grpc.stub.StreamObserver;

//Provide implementation to the protobuf method
public class BankService extends BankServiceGrpc.BankServiceImplBase {
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();

    }
}
