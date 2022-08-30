package demo.application.repositories;

import demo.application.domain.Authority;
import demo.application.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;


@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    public List<Authority> findAll();
    public Authority save(Authority auth);
    public Authority findByAuthority(String authority);
    public void deleteById(Long id);


}