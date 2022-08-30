package demo.application.service;

import demo.application.domain.Authority;
import demo.application.domain.User;
import java.util.*;

public interface IAuthority {

    List<Authority> findAll();

    Authority save(Authority Role);

    void deleteById(Long id);

    Optional<Authority> findById(int id);

}
