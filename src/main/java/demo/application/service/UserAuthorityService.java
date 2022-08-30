package demo.application.service;

import demo.application.domain.Authority;
import demo.application.repositories.UserAuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import demo.application.domain.User;
import demo.application.domain.UserAuthority;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserAuthorityService implements IUserAuthority{

    UserAuthorityRepository userauthrepo;

    @Autowired
    public UserAuthorityService(UserAuthorityRepository userauthrepo) {
        this.userauthrepo = userauthrepo;
    }

    @Override
    public List<UserAuthority> findByUser(User user){
        return userauthrepo.findByUser(user);
    }

    @Override
    public List<UserAuthority> findByAuthority(Authority auth){
        return userauthrepo.findByAuthority(auth);
    }

    @Override
    public UserAuthority save(UserAuthority userAuthority){
        return userauthrepo.save(userAuthority);
    }

}
