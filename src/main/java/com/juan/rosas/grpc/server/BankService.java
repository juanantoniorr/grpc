package com.juan.rosas.grpc.server;
import com.juan.rosas.grpc.grpcintro.*;
import io.grpc.Status;
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

    //STREAMING REQUEST SERVER SIDE -> SERVER RETURNS CHUNKS OF DATA
    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount();
        System.out.println("Amount " + amount);
        int balance = AccountDatabase.getBalance(accountNumber);

        //Adding validation
        if (balance<amount) {
          Status status = Status.FAILED_PRECONDITION.withDescription("Not enough money in the account. You only have: " + balance );
          responseObserver.onError(status.asRuntimeException());

        }

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

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashDepositStreamingRequest(responseObserver);
    }
}
