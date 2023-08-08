package com.juan.rosas.grpc.server;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AccountDatabase {

    private static final Map<Integer, Integer> MAP =
            IntStream.rangeClosed(1,10)
                    //Returns a stream of integer
                    .boxed()
                    //toMap receives 2 functions
                    //Function.identity receives the integer from previous stream and return the same integer
                    //Second function multiplies key by 10
                    .collect(Collectors.toMap(Function.identity(), k -> k*10));

    public static int getBalance(int accountId){
        return MAP.get(accountId);
    }

    public static int addBalance(int accountId, int amount) {
        return MAP.computeIfPresent(accountId, (k,v) -> v + amount);
    }

    public static int deductBalance(int accountId, int amount){
        return MAP.computeIfPresent(accountId, (k,v) -> v - amount);

    }
}
