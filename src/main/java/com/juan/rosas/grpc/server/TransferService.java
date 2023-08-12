package com.juan.rosas.grpc.server;

import com.juan.rosas.grpc.grpcintro.TransferRequest;
import com.juan.rosas.grpc.grpcintro.TransferResponse;
import com.juan.rosas.grpc.grpcintro.TransferServiceGrpc;
import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {
    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferStreamingRequest(responseObserver);
    }
}
