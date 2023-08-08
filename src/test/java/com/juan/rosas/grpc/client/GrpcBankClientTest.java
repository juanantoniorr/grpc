package com.juan.rosas.grpc.client;

import com.juan.rosas.grpc.grpcintro.Balance;
import com.juan.rosas.grpc.grpcintro.BalanceCheckRequest;
import com.juan.rosas.grpc.grpcintro.BankServiceGrpc;
import com.juan.rosas.grpc.server.BankService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GrpcBankClientTest {
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;

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

    }

    @Test
    public void balanceTest(){
       Balance balance =  blockingStub.getBalance(BalanceCheckRequest.newBuilder()
                .setAccountNumber(5)
                .build());
        System.out.println(balance);
       assert balance.getAmount() == 50;
    }

}
