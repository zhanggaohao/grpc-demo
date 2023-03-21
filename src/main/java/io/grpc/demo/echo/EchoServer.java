package io.grpc.demo.echo;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.gen.v1.service.EchoRequest;
import io.grpc.gen.v1.service.EchoResponse;
import io.grpc.gen.v1.service.EchoServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EchoServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(5257)
                .permitKeepAliveTime(300, TimeUnit.SECONDS)
                .maxInboundMessageSize(4194304)
                .addService(new EchoServiceGrpc.EchoServiceImplBase() {
                    @Override
                    public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
                        System.out.println("接收到数据：" + request.getMessage());

                        EchoResponse echoResponse = EchoResponse.newBuilder()
                                .setMessage(request.getMessage()).build();

                        responseObserver.onNext(echoResponse);
                        responseObserver.onCompleted();
                    }
                })
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.start();
        server.awaitTermination();
    }
}
