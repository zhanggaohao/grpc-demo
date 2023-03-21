package io.grpc.demo.service;

public class UserServiceImpl implements UserService {

    @Override
    public User getById(Long id) {
        return new User(id, "zcatch");
    }
}
