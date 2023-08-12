package com.juan.rosas.grpc.client;

import com.juan.rosas.grpc.grpcintro.BankServiceGrpc;
import com.juan.rosas.grpc.grpcintro.TransferRequest;
import com.juan.rosas.grpc.grpcintro.TransferServiceGrpc;
import com.juan.rosas.grpc.server.AccountDatabase;
import com.juan.rosas.grpc.server.BankService;
import com.juan.rosas.grpc.server.TransferService;
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
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransferClientTest {

    private TransferServiceGrpc.TransferServiceStub transferServiceStub;

    @BeforeAll
    public void setUp() throws IOException, InterruptedException {
        //Creating server
        Server server =  ServerBuilder.forPort(6565)
                //Important to add the service that extends grpc base impl
                .addService(new TransferService())
                .build();

        server.start();
        //Connectivity between client and server
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();

        //Non-blocking stub. ASYNC
       transferServiceStub = TransferServiceGrpc.newStub(managedChannel);

    }

    @Test
    public void transferTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        TransferStreamingResponse response = new TransferStreamingResponse(countDownLatch);
        StreamObserver<TransferRequest> streamRequestObserver= transferServiceStub.transfer(response);
        //We are going to send 80 transfers of 1 dolar
        //from account 9 = 90 dollars to account 1 = 10 dollars
        //at the end account 9 will have 10 dollars and account 1 will have 90
        for (int i=0; i< 80; i++){
            //Hacemos 100 requests
          TransferRequest transferRequest = TransferRequest.newBuilder()
                    .setFromAccount(9)
                    .setToAccount(1)
                    .setAmount(1)
                    .build();
            streamRequestObserver.onNext(transferRequest);
        }

        //We inform to the server that we are done with the requests
        streamRequestObserver.onCompleted();
        countDownLatch.await();

        assert AccountDatabase.getBalance(9) == 10;
        assert AccountDatabase.getBalance(1) == 90;


    }
}
