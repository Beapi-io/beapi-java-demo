package demo.application

import io.beapi.api.utils.ApiDescriptor
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

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.CookieStore
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.client.protocol.HttpClientContext



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
    @Shared Long userId

    @Shared Cookie suCookie
    @Shared Cookie tuCookie


    HttpClient httpClient = new DefaultHttpClient();

    void "[testuser] login"(){
        setup:"logging in"

        LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()

        String loginUri = "/authenticate"
        String url = "${protocol}${this.serverAddress}:${this.port}/${loginUri}" as String
        String json = "{\"username\":\"${testUser['login']}\",\"password\":\"${testUser['password']}\"}"

        HttpClient httpClient = new DefaultHttpClient();
        HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);
        HttpPost request = new HttpPost(url)
        request.setEntity(stringEntity);
        HttpResponse response = httpClient.execute(request);

        //int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        Object info = new JsonSlurper().parseText(responseBody)
        //println("info : "+info)

        final List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        cookies.each(){ it ->
            if(it.getName()=='JSESSIONID'){
                tuCookie = it
            }
        }

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

        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(tuCookie);

        HttpContext localContext = new BasicHttpContext();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","Bearer "+this.testUserToken));
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = client.execute(request,localContext);

        int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        println("status:"+statusCode)
        println("response:"+responseBody)

        Object info = new JsonSlurper().parseText(responseBody)
        ArrayList infoList = info.keySet()


        when:"info is not null"
        assert info!=[:]
        then:"get user"
        assert statusCode == 200
        assert infoList == infoList.intersect(returnsList)
        // todo : also check that ROLE_ADMIN response vars are not in keyset
    }


    void "[superuser] login "(){
        setup:"logging in"
        println("")
        println("[superuser] Logging in")
        LinkedHashMap suUser = apiProperties.getBootstrap().getSuperUser()
        String loginUri = "/authenticate"
        String url = "${protocol}${this.serverAddress}:${this.port}/${loginUri}" as String
        String json = "{\"username\":\"${suUser['login']}\",\"password\":\"${suUser['password']}\"}"

        HttpClient httpClient = new DefaultHttpClient();
        HttpEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        HttpPost request = new HttpPost(url)
        request.setEntity(stringEntity);
        HttpResponse response = httpClient.execute(request);

        //int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        Object info = new JsonSlurper().parseText(responseBody)
        //println("info : "+info)

        final List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        cookies.each(){ it ->
            if(it.getName()=='JSESSIONID'){
                suCookie = it
            }
        }

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

        String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(suCookie);

        HttpContext localContext = new BasicHttpContext();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type", "application/json"));
        request.setHeader(new BasicHeader("Authorization", "Bearer " + this.adminUserToken));
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = client.execute(request,localContext);

        int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        println("status:"+statusCode)
        println("response:"+responseBody)


        Object info = new JsonSlurper().parseText(responseBody)
        Set infoList = info.keySet()



        when:"info is not null"
        assert info!=[:]
        then:"get user"
        assert statusCode == 400
        //assert infoList == infoList.intersect(returnsList)
    }


    void "[superuser] GET USER api call without ID param (TEST)"() {
        setup:"api is called"
        println(" ")
        println("[superuser] GET USER api call without ID param (TEST)")
        String METHOD = "GET"
        String action = 'show'

        LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()
        String id = testUser['login']

        LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
        this.appVersion = getVersion()
        this.exchangeIntro = "v${this.appVersion}"

        //ArrayList returnsList = []
        String adminAuth = apiProperties.getSecurity().getSuperuserRole()
        def apiObject = cache?."${this.apiVersion}"?."${action}"
        ArrayList returnsList = getResponseData(adminAuth, apiObject)

        //String adminAuth = apiProperties.getSecurity().getSuperuserRole()
        //apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

        String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(suCookie);

        HttpContext localContext = new BasicHttpContext();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = client.execute(request,localContext);

        int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        println("status:"+statusCode)
        println("response:"+responseBody)


        Object info = new JsonSlurper().parseText(responseBody)
        ArrayList infoList = info.keySet()


        when:"info is not null"
        assert info!=[:]
        then:"get user"
        assert statusCode == 400
        //assert infoList == infoList.intersect(returnsList)
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

        //ArrayList returnsList = []
        def apiObject = cache?."${this.apiVersion}"?."${action}"
        //apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }
        String adminAuth = apiProperties.getSecurity().getSuperuserRole()
        //apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

        String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}?id=9999999" as String

        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(suCookie);

        HttpContext localContext = new BasicHttpContext();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = client.execute(request,localContext);

        int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = (response.getEntity()?.getContent())?IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8):null
        println("status:"+statusCode)
        println("response:"+responseBody)


        Object info = (responseBody)?new JsonSlurper().parseText(responseBody):null

        when:"error message has been thrown"
        assert info==null && statusCode != 200
        then:"status equals 204"
        assert statusCode == 204
    }


    void "[superuser] GET USER api call with ID param"() {
        setup:"api is called"
        println(" ")
        println("[superuser] GET USER api call with ID param ")
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

        String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}?id=test" as String

        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(suCookie);

        HttpContext localContext = new BasicHttpContext();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = client.execute(request,localContext);

        int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        println("status:"+statusCode)
        println("response:"+responseBody)


        Object info = new JsonSlurper().parseText(responseBody)

        when:"error message has been thrown"
        assert !info.isEmpty()
        then:"status equals 422"
        assert statusCode == 200
    }


    void "[superuser] GET list api call : [domain objects]"() {
        setup:"api is called"
        System.out.println(" ")
        System.out.println("[superuser] GET list api call : [domain objects]")
        String METHOD = "GET"
        String action = 'list'

        LinkedHashMap cache = apiCacheService.getApiCache(this.controller)
        this.appVersion = getVersion()
        this.exchangeIntro = "v${this.appVersion}"

        //ArrayList returnsList = []
        def apiObject = cache?."${this.apiVersion}"?."${action}"
        apiObject.returns.permitAll.each(){ it -> returnsList.add(it.name) }

        String adminAuth = apiProperties.getSecurity().getSuperuserRole()
        Set returnsList = getResponseData(adminAuth, apiObject)

        String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(suCookie);

        HttpContext localContext = new BasicHttpContext();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = client.execute(request,localContext);

        int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        println("status:"+statusCode)
        println("response:"+responseBody)

        Object info = new JsonSlurper().parseText(responseBody)

        when:"info is not null"
        assert info!=[:]
        then:"get user"
        info.each(){ it ->
            ArrayList infoList = []
            infoList = it?.keySet()
            assert infoList == infoList.intersect(returnsList)
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