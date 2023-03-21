package io.grpc.demo.echo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.gen.v1.service.EchoRequest;
import io.grpc.gen.v1.service.EchoResponse;
import io.grpc.gen.v1.service.EchoServiceGrpc;

public class EchoClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 5257)
                .usePlaintext().build();
        EchoServiceGrpc.EchoServiceBlockingStub blockingStub = EchoServiceGrpc.newBlockingStub(channel);
        EchoRequest request = EchoRequest.newBuilder()
                .setMessage("Hello,Grpc").build();
        EchoResponse response = blockingStub.echo(request);
        System.out.println("Grpc响应：" + response.getMessage());
    }
}
