package demo.application

import io.beapi.api.properties.ApiProperties
import spock.lang.*
import geb.spock.*
import groovy.json.JsonSlurper
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource

import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.io.IOUtils
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpResponse
import org.apache.http.message.BasicHeader
import org.apache.http.entity.StringEntity
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.nio.charset.StandardCharsets


@TestPropertySource(locations="classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiFunctionalTest extends Specification {

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

    @Autowired
    HikariDataSource dataSource;

    @Shared String controller = 'user'

    /*
    * PROTOCOL SHOULD ALWAYS BE HTTP INTERNALLY AS PROXY/LOAD BALANCER WILL HANDLE
    * CERTIFICATE AND FORWARD TO APP SERVER (WHICH THEN ONLY NEEDS HTTP INTERNALLY)
     */
    @Shared String protocol = "http://"

    @Value("\${server.address}")
    String serverAddress;

    @LocalServerPort private int port

    @Shared String exchangeIntro
    @Shared String appVersion
    @Shared String apiVersion = '1'


    HttpClient httpClient = new DefaultHttpClient();

    void "test datasource connection"(){
        setup:"logging in"
println("catalog:" + dataSource.getConnection().getCatalog());

    }

    void "[testuser] login"(){
        setup:"logging in"
            HttpClient httpClient = new DefaultHttpClient();
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
            Object info = new JsonSlurper().parseText(responseBody)

        when:"info is not null"
            this.testUserToken = info.token
        then:"assert token is not null"
            assert info.token!=[:]
            // todo : also check that ROLE_ADMIN response vars are not in keyset
    }


    void "[testuser] GET api call (with good data)"() {
        setup:"api is called"
            println(" ")
            println("[testuser] GET api call (with good data)")
            String action = 'show'

            LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject.returns.permitAll.each(){ it ->
                returnsList.add(it.name)
            }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.testUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            println('statusCode : '+statusCode)

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

            println(info)

        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
            // todo : also check that ROLE_ADMIN response vars are not in keyset
    }




    void "[superuser] login "(){
        setup:"logging in"
            println(" ")
            LinkedHashMap suUser = apiProperties.getBootstrap().getSuperUser()
            String loginUri = "/authenticate"
            String url = "${protocol}${this.serverAddress}:${this.port}/${loginUri}" as String
            String json = "{\"username\":\"${suUser['login']}\",\"password\":\"${suUser['password']}\"}"
            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost(url)
            request.setEntity(stringEntity);
            HttpResponse response = httpClient.execute(request);

            //int statusCode = response.getStatusLine().getStatusCode()
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)

        when:"info is not null"
            this.adminUserToken = info.token
        then:"assert token is not null"
            assert info.token!=[:]
    }

    void "[superuser] GET USER api call without ID param"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET USER api call without ID param")
            String METHOD = "GET"
            String action = 'show'

            LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

            println(info)

        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
    }


    void "[superuser] GET USER api call with ID param"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET USER api call with ID param")
            String METHOD = "GET"
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

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}/${id}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

            println(info)

        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
    }

    void "[superuser] GET USER api call with ID param (BAD DATA)"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET USER api call with ID param (BAD DATA)")
            String METHOD = "GET"
            String action = 'show'

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}/19" as String

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            println(info)

        when:"error message has been thrown"
            assert !info.isEmpty() && info.status != 200
        then:"status equals 422"
            assert info.status == '404'
    }


    void "[superuser] GET list api call : [domain objects]"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET list api call : [domain objects]")
            String METHOD = "GET"
            String action = 'list'

            LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)

            println(info)

        when:"info is not null"
            assert info!=[:]
        then:"get user"

            info.each(){ it ->
                ArrayList infoList = []
                infoList = it?.keySet()
                assert infoList == returnsList.intersect(infoList)
            }
    }

/*
    void "[superuser] APIDOC TEST"() {
        setup:"api is called"
        println(" ")
        println("[superuser] APIDOC TEST")
        String METHOD = "GET"
        String controller = 'apidoc'
        String action = 'show'

        LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()

        LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
        this.appVersion = getVersion()
        this.exchangeIntro = "v${this.appVersion}"

        ArrayList returnsList = []
        def apiObject = cache?."${this.apiVersion}"?."${action}"
        apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

        String adminAuth = apiProperties.getSecurity().getSuperuserRole()
        apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

        String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}" as String

        HttpClient client = new DefaultHttpClient();
        //URL uri = new URL(url);
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
        HttpResponse response = client.execute(request);

        int statusCode = response.getStatusLine().getStatusCode()

        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        Object info = new JsonSlurper().parseText(responseBody)
        ArrayList infoList = info.keySet()

        println(info)

        when:"info is not null"
        assert info!=[:]
        then:"get user"
        assert statusCode == 200
        assert infoList == returnsList.intersect(infoList)
    }

 */

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