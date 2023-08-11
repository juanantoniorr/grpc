package com.juan.rosas.grpc.client;

import com.google.common.util.concurrent.Uninterruptibles;
import com.juan.rosas.grpc.grpcintro.*;
import com.juan.rosas.grpc.server.BankService;
import com.juan.rosas.grpc.server.CashDepositStreamingRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GrpcBankClientTest {
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    //Non blocking use for streaming
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    public void setUp() throws IOException, InterruptedException {
        //Creating server
        Server server =  ServerBuilder.forPort(6565)
                //Important to add the service that extends grpc base impl
                .addService(new BankService())
                .build();

        server.start();
        //Connectivity between client and server
       ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();

       //Blocking stub
       blockingStub =  BankServiceGrpc.newBlockingStub(managedChannel);

       //Non-blocking stub. ASYNC
        bankServiceStub = BankServiceGrpc.newStub(managedChannel);

    }

    @Test
    public void balanceTest(){
       Balance balance =  blockingStub.getBalance(BalanceCheckRequest.newBuilder()
                .setAccountNumber(5)
                .build());
        System.out.println(balance);
       assert balance.getAmount() == 50;
    }

    @Test
    public void withdrawTest(){

        //Given: Account 7 has 70 dollars as default
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(7)
                .setAmount(40)
                .build();

        //When: We withdraw 40 -> WithdrawRequest
        blockingStub.withdraw(withdrawRequest)
               .forEachRemaining(money -> {
                   //Every chunk 10 dollars
                   assert money.getValue() == 10;
               });
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(7)
                .build();
        //Then we should have 30 dollars left
        assert blockingStub.getBalance(balanceCheckRequest).getAmount() == 30;
    }

    @Test
    public void withdrawAsyncTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(7)
                .setAmount(30)
                .build();
        bankServiceStub.withdraw(withdrawRequest,new MoneyStreamingResponse(countDownLatch));
        //IT IS ASYNC SO WE WAIT TO BE COMPLETED
        countDownLatch.await();

    }

    @Test
    public void cashDepositStreamingRequest() throws InterruptedException {
        //As client is streaming the request, client needs to provide handler (BalanceStreamObserver)
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //It gives you an observer object that you can use to create your streaming request
        //El cliente implementa la manera en que va a manejar el response del server (BalanceStreamObserver)
        // El metodo cashDeposit te regresa un streamObserver del request lo que nos indica que vamos a manejar streaming de request (multiple requests)
        // Este metodo (cliente) tiene la responsabilidad de avisar al server cuando acabe de hacer todos los requests (call onCompleted line: 110)
        // Y tiene la responsabilidad de llamar onNext cuando quiera mandar otro request (linea 107)
       StreamObserver<DepositRequest> depositRequestStreamObserver = bankServiceStub.cashDeposit(new BalanceStreamObserver(countDownLatch));
       //We send 10 times a deposit of 10 dollars to account 8
       for (int i=0; i<10; i++){
           DepositRequest depositRequest = DepositRequest.newBuilder()
                   .setAccountNumber(8)
                   .setAmount(10)
                   .build();

           depositRequestStreamObserver.onNext(depositRequest);

       }
       //When the 10 times are done the task is completed so client notify the server that it is done
       depositRequestStreamObserver.onCompleted();

       countDownLatch.await();

       //Account 8 by default has 80 dollars, after deposit of 100 should have 180
       assert blockingStub.getBalance(BalanceCheckRequest.newBuilder()
               .setAccountNumber(8)
               .build()).getAmount() == 180;

    }



}
