package io.grpc.demo.echo;

import io.grpc.gen.v1.service.EchoRequest;
import io.grpc.gen.v1.service.EchoResponse;
import io.grpc.stub.StreamObserver;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

public class GrpcSession implements Closeable {

    private final UUID sessionId;
    private boolean connected;
    private StreamObserver<EchoResponse> outputStream;

    public GrpcSession(StreamObserver<EchoResponse> outputStream) {
        this.sessionId = UUID.randomUUID();
        this.outputStream = outputStream;
    }

    public void onRequest(EchoRequest request) {
        this.connected = true;
        System.out.println("接收到数据：" + request.getMessage());
        outputStream.onNext(EchoResponse.newBuilder()
                .setMessage(request.getMessage()).build());
        outputStream.onCompleted();
    }

    public void send(String message) {
        if (connected) {
            outputStream.onNext(EchoResponse.newBuilder()
                    .setMessage(message).build());
            outputStream.onCompleted();
        } else {
            throw new RuntimeException("not connected!");
        }
    }

    public UUID getSessionId() {
        return sessionId;
    }

    @Override
    public void close() throws IOException {
        connected = false;
        if (outputStream != null) {
            outputStream.onCompleted();
        }
    }
}
