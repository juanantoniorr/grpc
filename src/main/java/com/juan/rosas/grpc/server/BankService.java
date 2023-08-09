package com.juan.rosas.grpc.server;
import com.juan.rosas.grpc.grpcintro.*;
import io.grpc.stub.StreamObserver;

//Provide implementation to the protobuf method
public class BankService extends BankServiceGrpc.BankServiceImplBase {
    //UNARY REQUEST
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();

    }

    //STREAMING REQUEST
    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount();
        System.out.println("Amount " + amount);
        int balance = AccountDatabase.getBalance(accountNumber);

        for (int i = 0; i< (amount/10); i++){
            Money money = Money.newBuilder()
                    .setValue(10)
                    .build();
            //Le voy pasando de 10 en 10 
            responseObserver.onNext(money);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        AccountDatabase.deductBalance(accountNumber, amount);

        responseObserver.onCompleted();
    }
}
