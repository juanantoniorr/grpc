package com.juan.rosas.grpc.client;

import com.google.common.util.concurrent.Uninterruptibles;
import com.juan.rosas.grpc.grpcintro.Balance;
import com.juan.rosas.grpc.grpcintro.BalanceCheckRequest;
import com.juan.rosas.grpc.grpcintro.BankServiceGrpc;
import com.juan.rosas.grpc.grpcintro.WithdrawRequest;
import com.juan.rosas.grpc.server.BankService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GrpcBankClientTest {
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
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

}
