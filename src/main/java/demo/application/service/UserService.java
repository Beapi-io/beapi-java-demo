package demo.application.service;

import demo.application.domain.Branch;
import demo.application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import demo.application.domain.User;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@Service
public class UserService implements IUser {

    UserRepository userrepo;

    @Autowired
    public UserService(UserRepository userrepo) {
        this.userrepo = userrepo;
    }

    @Override
    public List<User> getAllUsers() { return userrepo.findAll(); }

    //@Override
    public Optional<User> findById(Long id) {
        return userrepo.findById(id);
    }

    @Override
    public Optional<User> findById(int id) {
        return userrepo.findById(Long.valueOf(id));
    }

    @Override
    public User findByEmail(String email) {
        return userrepo.findByEmail(email);
    }
    @Override
    public User findByUsername(String username) {
        return userrepo.findByUsername(username);
    }

    @Override
    public User save(User usr) {
        userrepo.save(usr);
        userrepo.flush();
        return usr;
    }

    //@Override
    public void deleteById(Long id) {
        userrepo.deleteById(id);
        userrepo.flush();
    }

    @Override
    public void deleteById(int id) {
        userrepo.deleteById(Long.valueOf(id));
        userrepo.flush();
    }

    @Override
    public User bootstrapUser(User usr) {
        userrepo.save(usr);
        userrepo.flush();
        return usr;
    }

}
