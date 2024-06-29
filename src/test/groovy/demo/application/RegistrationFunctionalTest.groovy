package demo.application

import com.zaxxer.hikari.HikariDataSource;
import geb.spock.*;
import groovy.json.JsonSlurper;
import io.beapi.api.properties.ApiProperties;
import io.beapi.api.service.ApiCacheService;
import io.beapi.api.service.PrincipleService;
import io.beapi.api.utils.ApiDescriptor;
import io.beapi.api.service.JwtUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import io.beapi.api.domain.service.UserService;
import io.beapi.api.domain.UserAuthority;
import io.beapi.api.domain.service.UserAuthorityService;
import io.beapi.api.service.PrincipleService
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import spock.lang.*;
import io.beapi.api.service.JwtUserDetailsService;
import java.nio.charset.StandardCharsets;
import io.beapi.api.domain.User;

@TestPropertySource(locations="classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegistrationFunctionalTest extends Specification {

    @Autowired ApplicationContext applicationContext

    @Autowired ApiProperties apiProperties

    @Autowired private JwtUserDetailsService userDetailsService;

    @Autowired private UserAuthorityService uAuthService;

    @Autowired private UserService userService;


    @Shared String controller = 'user'

    /*
    * PROTOCOL SHOULD ALWAYS BE HTTP INTERNALLY AS PROXY/LOAD BALANCER WILL HANDLE
    * CERTIFICATE AND FORWARD TO APP SERVER (WHICH THEN ONLY NEEDS HTTP INTERNALLY)
     */
    @Shared String protocol = "http://"

    @Value("\${server.address}")
    String serverAddress;

    @LocalServerPort private int port
    @Shared String apiVersion = '1'
    @Shared String username = "registeredUser";
    @Shared String password = "testRegistration";
    @Shared String email = "registeredUser@gmail.com"


    HttpClient httpClient = new DefaultHttpClient();


    void "[registration] register user"(){
        setup:"registering user"
            HttpClient httpClient = new DefaultHttpClient();

            String registerUri = "/register"
            String url = "${protocol}${this.serverAddress}:${this.port}/${registerUri}" as String
            String json = "{\"username\":\"${username}\",\"password\":\"${password}\",\"email\":\"${email}\"}"
            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

            HttpPost request = new HttpPost(url)
            request.setEntity(stringEntity);
            HttpResponse response = this.httpClient.execute(request);

            //int statusCode = response.getStatusLine().getStatusCode()
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            Object info = new JsonSlurper().parseText(responseBody)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        when:"info is not null"
            ///this.testUserToken = info.token
        then:"assert user is not null"
            assert userDetails!=null
    }

    void "[registration] login"(){
        setup:"logging in"
        HttpClient httpClient = new DefaultHttpClient();
        LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()

        String loginUri = "/authenticate"
        String url = "${protocol}${this.serverAddress}:${this.port}/${loginUri}" as String
        String json = "{\"username\":\"${username}\",\"password\":\"${password}\"}"
        HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

        HttpPost request = new HttpPost(url)
        request.setEntity(stringEntity);
        HttpResponse response = this.httpClient.execute(request);

        //int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

        Object info = new JsonSlurper().parseText(responseBody)


        when:"info is not null"
        //this.testUserToken = info.token
        then:"assert token is not null"
            assert info.token!=[:]
    }

    void "[bad registration] register user with duplicate email"(){
        setup:"registering user"
            HttpClient httpClient = new DefaultHttpClient();

            String registerUri = "/register"
            String url = "${protocol}${this.serverAddress}:${this.port}/${registerUri}" as String
            String json = "{\"username\":\"${username}\",\"password\":\"${password}\",\"email\":\"email@yourdomain.com \"}"
            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

            HttpPost request = new HttpPost(url)
            request.setEntity(stringEntity);
            HttpResponse response = this.httpClient.execute(request);

            //int statusCode = response.getStatusLine().getStatusCode()
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
println(responseBody)
            //Object info = new JsonSlurper().parseText(responseBody)
            //UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        when:"info is not null"
            ///this.testUserToken = info.token
        then:"assert user is not null"
            assert responseBody.startsWith("Acct with this username/email already exists. Please try again.")
        cleanup:"remove test cases"
            User u = userService.findByUsername(username);
            List<UserAuthority> uAuths = uAuthService.findByUser(u);
            uAuths.each(){
                uAuthService.deleteById(it.id)
            }

            if(u){
                userService.deleteById(u.id)
            }
    }


    private String getVersion() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL incoming = classLoader.getResource("META-INF/build-info.properties")

        String version
        if (incoming != null) {
            Properties properties = new Properties();
            properties.load(incoming.openStream());
            version = properties.getProperty('build.version')
        }
        return version
    }

    private Set getResponseData(String auth, ApiDescriptor apiObject){
        Set returnsList = []
        apiObject?.returns?."${auth}".each() { it2 -> returnsList.add(it2.name) }
        apiObject?.returns?."permitAll".each() { it2 -> returnsList.add(it2.name) }

        return returnsList
    }
}