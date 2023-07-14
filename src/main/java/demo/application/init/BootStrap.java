package demo.application.init;

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.UserAuthority;

import io.beapi.api.domain.service.AuthorityService;
import io.beapi.api.domain.service.UserService;
import io.beapi.api.domain.service.UserAuthorityService;

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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.stream.StreamSupport;
import javax.crypto.spec.GCMParameterSpec;
import io.beapi.api.service.ApiCacheService;

@Component
public class BootStrap {

    @Autowired
    public PasswordEncoder passwordEncoder;

    // TODO : set this is properties
    ArrayList<String> roles = new ArrayList<String>(Arrays.asList("ROLE_ADMIN","ROLE_USER"));

    @Autowired
    ApiProperties apiProperties;

    @Autowired
    ApiCacheService apiCacheService;


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
        roles.add(apiProperties.getSecurity().getTestRole());

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


    public static byte[] gcmEncrypt(String input, SecretKey key, byte[] IV) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(96, IV);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

        byte[] cipherText = cipher.doFinal(input.getBytes());
        return cipherText;
    }

    public static String gcmDecrypt(byte[] cipherText, SecretKey key, byte[] IV) throws Exception {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(96, IV);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        byte[] decryptedText = cipher.doFinal(cipherText);
        return new String(decryptedText);
    }

}
