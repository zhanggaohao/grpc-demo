package io.grpc.demo;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.gen.v1.service.*;

import java.io.*;

public class RpcClient {

    public static void main(String[] args) throws IOException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 5258)
                .usePlaintext().build();

        RpcServiceGrpc.RpcServiceBlockingStub blockingStub = RpcServiceGrpc.newBlockingStub(channel);

        ServiceRequestMetadata metadata = ServiceRequestMetadata.newBuilder()
                .setClassName("io.grpc.demo.service.UserService")
                .setMethodName("getById")
                .addParameterTypes("java.lang.Long")
                .build();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(1L);

        ServiceRequest serviceRequest = ServiceRequest.newBuilder()
                .setMetadata(metadata)
                .addRequestBody(ByteString.copyFrom(byteArrayOutputStream.toByteArray()))
                .build();
        ServiceResponse response = blockingStub.request(serviceRequest);
        ByteString bytes = response.getReturnBody();
        if (bytes.isEmpty()) {
            System.out.println("请求结果：null");
        } else {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
                Object returnObject = objectInputStream.readObject();
                System.out.println(returnObject);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
