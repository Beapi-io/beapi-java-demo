package demo.application.service;

import java.util.Optional;
import demo.application.domain.User;
import java.util.*;

public interface IUser {
    List<User> getAllUsers();
    Optional<User> findById(int id);
    User findByEmail(String email);
    User findByUsername(String username);
    User save(User usr);
    void deleteById(int id);
    User bootstrapUser(User usr);
}
