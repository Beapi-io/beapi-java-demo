package demo.application

import geb.spock.*
import groovy.json.JsonSlurper
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicHeader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource
import spock.lang.*

import java.nio.charset.StandardCharsets

@TestPropertySource(locations="classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BadApiFunctionalTest extends Specification {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    ApiProperties apiProperties

    @Autowired
    ApiCacheService apiCacheService

    @Autowired
    PrincipleService principle

    @Shared String adminUserToken

    @Shared String testUserToken


    @Shared String controller = 'apidoc'


    @Value("\${server.address}")
    String serverAddress;

    /*
    * PROTOCOL SHOULD ALWAYS BE HTTP INTERNALLY AS PROXY/LOAD BALANCER WILL HANDLE
    * CERTIFICATE AND FORWARD TO APP SERVER (WHICH THEN ONLY NEEDS HTTP INTERNALLY)
     */
    @Shared String protocol

    @LocalServerPort private int port

    @Shared String exchangeIntro
    @Shared String appVersion
    @Shared String apiVersion = '1'

    org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();

    void "[testuser] login"(){
        setup:"logging in"
            println(" ")
            println("[testuser] login")
            this.protocol = "${apiProperties.getTestingProtocol()}://"

            org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
            LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()

            String loginUri = "/authenticate"
            String url = "${protocol}${this.serverAddress}:${this.port}/${loginUri}" as String
            String json = "{\"username\":\"${testUser['login']}\",\"password\":\"${testUser['password']}\"}"
            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

            HttpPost request = new HttpPost(url)
            request.setEntity(stringEntity);
            HttpResponse response = this.httpClient.execute(request);

            //int statusCode = response.getStatusLine().getStatusCode()
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            //println("responseBody:"+responseBody)
            Object info = new JsonSlurper().parseText(responseBody)
            //println("info : "+info)

        when:"info is not null"
            this.testUserToken = info.token
            //println(info.token)
        then:"assert token is not null"
            assert info.token!=[:]
            // todo : also check that ROLE_ADMIN response vars are not in keyset
    }

    void "[testuser] GET api call (with BAD METHOD)"() {
        setup:"api is called"
            println(" ")
            println("[testuser] GET api call (with BAD METHOD)")
            String METHOD = "PUT"
            String action = 'show'
            LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()
            String id = testUser['login']

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            //String url = "curl -v -H 'Content-Type: application/json' -H  'Authorization: Bearer  ${this.adminUserToken}' --request GET ${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}/${id}"

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/user/${action}" as String

            //HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPut request = new HttpPut(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+testUserToken));
            HttpResponse response = this.httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)

        when:"info is not null"
            assert responseBody!=[:]
        then:"get user"
            assert statusCode == 405
    }

    void "[testuser] GET api call (with BAD METHOD -2)"() {
        setup:"api is called"
            println(" ")
            println("[testuser] GET api call (with BAD METHOD - 2)")
            String METHOD = "PUT"
            String action = 'show'

            LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()
            String id = testUser['login']

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            //String url = "curl -v -H 'Content-Type: application/json' -H  'Authorization: Bearer  ${this.adminUserToken}' --request GET ${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}/${id}"

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

            //HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPut request = new HttpPut(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+testUserToken));
            HttpResponse response = this.httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()
            //println(statusCode)

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)

        when:"info is not null"
            assert responseBody!=[:]
        then:"get user"
            assert statusCode == 405
    }


    void "[testuser] GET api call (with NO ACTION)"() {
        setup:"api is called"
            println("")
            println("[testuser] GET api call (with NO ACTION)")
            String METHOD = "PUT"

            LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()
            String id = testUser['login']

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/user" as String

            //HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+testUserToken));
            HttpResponse response = this.httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()
            //println(statusCode)

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info
            //println("RESPONSE : "+responseBody)

            if(responseBody) {
                info = new JsonSlurper().parseText(responseBody)
            }

        when:"info is not null"
            assert info != null
        then:"get user"
            assert statusCode == 400
    }


    void "[testuser] GET api call (with BAD data)"() {
        setup:"api is called"
            println(" ")
            println("[testuser] GET api call (with BAD data)")
            String METHOD = "GET"
            String action = 'show'

            LinkedHashMap suUser = apiProperties.getBootstrap().getSuperUser()
            String id = suUser['login']

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}/${id}" as String

            //HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+testUserToken));
            HttpResponse response = this.httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)

        when:"info is not null"
            assert responseBody!=[:]
        then:"get user"
            assert statusCode == 400
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
}