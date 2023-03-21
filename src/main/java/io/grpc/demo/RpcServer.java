package io.grpc.demo;

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.demo.service.UserService;
import io.grpc.demo.service.UserServiceImpl;
import io.grpc.gen.v1.service.*;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class RpcServer {

    final static ConcurrentMap<Class<?>, Object> beanFactory = new ConcurrentHashMap<>();

    static {
        beanFactory.put(UserService.class, new UserServiceImpl());
    }

    public static void main(String[] args) {

        Server server = ServerBuilder.forPort(5258)
                .permitKeepAliveTime(300, TimeUnit.SECONDS)
                .maxInboundMessageSize(4194304)
                .addService(new RpcServiceGrpc.RpcServiceImplBase() {
                    @Override
                    public void request(ServiceRequest request, StreamObserver<ServiceResponse> responseObserver) {
                        ServiceRequestMetadata metadata = request.getMetadata();
                        try {
                            ProtocolStringList parameterTypesList = metadata.getParameterTypesList();
                            List<ByteString> requestBodyList = request.getRequestBodyList();
                            Class<?>[] parameterTypes = new Class<?>[parameterTypesList.size()];
                            Object[] parameters = new Object[parameterTypesList.size()];
                            for (int i = 0; i < parameterTypesList.size(); i++) {
                                parameterTypes[i] = Class.forName(parameterTypesList.get(i));
                                ByteString bytes = requestBodyList.get(i);
                                if (bytes.isEmpty()) {
                                    parameters[i] = null;
                                } else {
                                    ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
                                    Object object = objectInputStream.readObject();
                                    parameters[i] = object;
                                }
                            }
                            Class<?> invokeClass = Class.forName(metadata.getClassName());
                            Object invokeObject = beanFactory.get(invokeClass);
                            Method method = invokeObject.getClass().getMethod(metadata.getMethodName(), parameterTypes);

                            Object returnObject = method.invoke(invokeObject, parameters);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                            objectOutputStream.writeObject(returnObject);
                            responseObserver.onNext(ServiceResponse.newBuilder()
                                    .setReturnBody(ByteString.copyFrom(outputStream.toByteArray()))
                                    .build());
                            responseObserver.onCompleted();
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .build();
        try {
            server.start();
            TimeUnit.SECONDS.sleep(60);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        server.shutdown();
    }
}
