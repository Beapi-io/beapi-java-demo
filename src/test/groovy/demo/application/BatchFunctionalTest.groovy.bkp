package demo.application

import demo.application.service.AuthorityService
import io.beapi.api.properties.ApiProperties
import spock.lang.*
import geb.spock.*
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource


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
class BatchFunctionalTest extends Specification {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    private ApiProperties apiProperties

    @Autowired
    AuthorityService authService

    @Autowired
    ApiCacheService apiCacheService

    @Autowired
    PrincipleService principle

    @Shared String adminUserToken

    @Shared String controller = 'authority'

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



    void "[superuser] login "(){
        setup:"logging in"

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

    void "[superuser] POST create AUTHORITY"() {
        setup:"api is called"
            String METHOD = "POST"
            String action = 'create'

            // test with batch; test rollback later
            ArrayList batchData = [['authority': 'ROLE_TEST1'],['authority': 'ROLE_TEST2'],['authority': 'ROLE_TEST3'],['authority': 'ROLE_TEST4'],['authority': 'ROLE_TEST5'],['authority': 'ROLE_TEST6']]
            LinkedHashMap data = ['batch':batchData]
            String json = JsonOutput.toJson(data)
            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);


            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()

            // NOTE : (b)atch call
            this.exchangeIntro = "b${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${this.controller}/${action}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPost request = new HttpPost(url)
            //request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setEntity(stringEntity);
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)

            println(info)

        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert batchData.size() == info.size()
        cleanup:"remove test cases"
            info.each(){ it ->
                authService.deleteById(it.id.toLong())
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
}