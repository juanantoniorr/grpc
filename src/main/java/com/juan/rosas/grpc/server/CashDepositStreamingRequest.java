package com.juan.rosas.grpc.server;

import com.juan.rosas.grpc.grpcintro.Balance;
import com.juan.rosas.grpc.grpcintro.DepositRequest;
import io.grpc.stub.StreamObserver;

//Toda esta logica contempla que recibo un request y voy a responder un Balance (es streamObserver aunque el response no sea stream)
//Esto es  del lado del servidor sin importar como sea el request ya estoy implementando el response
public class CashDepositStreamingRequest implements StreamObserver<DepositRequest> { //Como servidor debo manejar un request
    //This is the response in proto file
    private StreamObserver<Balance> balanceStreamObserver;
    private int accountBalance;

    public CashDepositStreamingRequest(StreamObserver<Balance> balanceStreamObserver) {
        this.balanceStreamObserver = balanceStreamObserver;
    }

    //Agrego el dinero que recibo en el request a la cuenta
    @Override
    public void onNext(DepositRequest depositRequest) {
        int accountNumber = depositRequest.getAccountNumber();
        int amount = depositRequest.getAmount();
       accountBalance =  AccountDatabase.addBalance(accountNumber, amount);


    }

    @Override
    public void onError(Throwable throwable) {

    }

    //La respuesta se tiene que enviar hasta que acabe de recibir todos los requests(streaming)
    @Override
    public void onCompleted() {
      Balance updatedBalance = Balance.newBuilder()
                .setAmount(accountBalance)
                .build();

      //Send response and complete -> Response is not streaming
        //Respondo con el balance y la tarea esta completa.
      balanceStreamObserver.onNext(updatedBalance);
      balanceStreamObserver.onCompleted();
    }
}
