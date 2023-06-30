package demo.application

import geb.spock.*
import io.beapi.api.properties.ApiProperties
import demo.application.service.UserService;
import demo.application.service.HookService;
import demo.application.domain.User
import demo.application.domain.Hook
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.*

import java.nio.charset.StandardCharsets

@TestPropertySource(locations="classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JpaTest extends Specification {

    @Autowired
    private ApiProperties apiProperties

    @Autowired
    UserService userService;

    @Autowired
    HookService hookService;

/*
    def "throw exception for invalid format of email address"() {
        LinkedHashMap superUser = apiProperties.getBootstrap().getSuperUser();
        User admin = userService.findByEmail(superUser.get("email").toString());

        Hook hook = new Hook();
        hook.setUser(admin);
        hook.setService("a1b2c3");
        hook.setFormat("JSON");
        hook.setCallback("a1b2c3");
        hook.setAuthorization("a1b2c3");
        hook.setAttempts(0);
        hook.setEnabled(true);
        hook.setDateCreated(234567);
        hook.setLastModified(123456);

        when: "create invalid email address"
            assert Objects.nonNull(hookService.save(hook));
            User admin2 = userService.findByEmail(superUser.get("email").toString());
        then: "throw exception for invalid format of email address"
            admin2.getHooks().each(){
                hookService.deleteById(it.id)
            }
        //where:

    }

 */

}