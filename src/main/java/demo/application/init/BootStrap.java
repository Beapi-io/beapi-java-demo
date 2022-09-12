package demo.application.init;


import demo.application.domain.User;
import demo.application.domain.Authority;
import demo.application.domain.UserAuthority;
import demo.application.service.AuthorityService;
import demo.application.service.UserAuthorityService;
import demo.application.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.info.BuildProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.beapi.api.properties.ApiProperties;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


@Component
public class BootStrap {


    @Autowired
    public PasswordEncoder passwordEncoder;

    // TODO : set this is properties
    ArrayList<String> roles = new ArrayList<String>(Arrays.asList("ROLE_ADMIN","ROLE_USER"));

    @Autowired
    ApiProperties apiProperties;


    @Autowired
    private AuthorityService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthorityService uAuthService;

    LinkedHashMap testLoadOrder = new LinkedHashMap();


    public void init(ApplicationContext applicationContext) {

        // START BOOTSTRAP AUTHORITIES
        ArrayList<String> roles = new ArrayList();
        roles.add(apiProperties.getSecurity().getSuperuserRole());
        roles.add(apiProperties.getSecurity().getUserRole());

        List<Authority> auth = authService.findAll();

        ArrayList<String> authroles = new ArrayList();
        for(Authority it:auth){
            authroles.add(it.getAuthority());
        }

        for(String role:roles){
            if(!authroles.contains(role)){
                Authority newAuth = new Authority();
                newAuth.setAuthority(role);
                authService.save(newAuth);
            }
        }

        // START BOOTSTRAP SUPERUSER
        LinkedHashMap superUser = apiProperties.getBootstrap().getSuperUser();

        Authority adminAuth = authService.findByAuthority(apiProperties.getSecurity().getSuperuserRole());
        Authority testAuth = authService.findByAuthority(apiProperties.getSecurity().getUserRole());

        User sUser = userService.findByEmail(superUser.get("email").toString());
        if(null==sUser){ sUser = userService.findByUsername(superUser.get("login").toString()); }
        if(Objects.nonNull(sUser)){
            // UPDATE SUPERUSER
            sUser.setUsername(superUser.get("login").toString());
            sUser.setEmail(superUser.get("email").toString());
            sUser.setPassword(passwordEncoder.encode(superUser.get("password").toString()));
            userService.save(sUser);
        }else{
            // CREATE NEW SUPERUSER
            sUser = new User();
            ArrayList<Authority> auths1 = new ArrayList();
            auths1.add(adminAuth);

            sUser.setUsername(superUser.get("login").toString());
            sUser.setEmail(superUser.get("email").toString());
            sUser.setPassword(passwordEncoder.encode(superUser.get("password").toString()));

            // todo : need rollback upon fail
            if(Objects.nonNull(userService.save(sUser))) {
                //auths1.each() {
                for(Authority it : auths1){
                    UserAuthority uAuth1 = new UserAuthority();
                    uAuth1.setUser(sUser);
                    uAuth1.setAuthority(it);
                    uAuthService.save(uAuth1);
                }
            }
        }
        // END BOOTSTRAP SUPERUSER


        // START BOOTSTRAP TESTUSER
        LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser();
        User tUser = userService.findByEmail(testUser.get("email").toString());
        if(null==tUser){ tUser = userService.findByUsername(testUser.get("login").toString()); }

        if(Objects.nonNull(tUser)){
            // UPDATE TESTUSER
            tUser.setUsername(testUser.get("login").toString());
            tUser.setEmail(testUser.get("email").toString());
            tUser.setPassword(passwordEncoder.encode(testUser.get("password").toString()));
            userService.save(tUser);
        }else{
            // CREATE NEW TESTUSER
            tUser = new User();
            ArrayList<Authority> auths2 = new ArrayList();
            auths2.add(testAuth);

            tUser.setUsername(testUser.get("login").toString());
            tUser.setEmail(testUser.get("email").toString());
            tUser.setPassword(passwordEncoder.encode(testUser.get("password").toString()));

            // todo : need rollback upon fail
            if(Objects.nonNull(userService.save(tUser))){
                //auths2.each() {
                for(Authority it: auths2){
                    UserAuthority uAuth = new UserAuthority();
                    uAuth.setUser(tUser);
                    uAuth.setAuthority(it);
                    uAuthService.save(uAuth);
                }
            }
        }
    }

}
