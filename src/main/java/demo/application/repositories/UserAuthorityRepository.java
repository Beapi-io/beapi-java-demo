package demo.application.repositories;

import demo.application.domain.Authority;
import demo.application.domain.User;
import demo.application.domain.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import javax.validation.constraints.NotBlank;
//import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.util.*;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long> {

    public UserAuthority save(UserAuthority userAuthority);
    List<UserAuthority> findByUser(User user);
    List<UserAuthority> findByAuthority(Authority authority);

}