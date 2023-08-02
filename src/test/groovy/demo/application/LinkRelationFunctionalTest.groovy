package demo.application


import demo.application.service.BranchService
import demo.application.service.CompanyService
import demo.application.service.DeptService
import geb.spock.*
import groovy.json.JsonOutput
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
class LinkRelationFunctionalTest extends Specification {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    private ApiProperties apiProperties


    @Autowired ApiCacheService apiCacheService

    @Autowired CompanyService compService
    @Autowired BranchService branchService
    @Autowired DeptService deptService

    @Autowired PrincipleService principle

    @Shared String adminUserToken

    

    @Value("\${server.address}")
    String serverAddress;

    /*
    * PROTOCOL SHOULD ALWAYS BE HTTP INTERNALLY AS PROXY/LOAD BALANCER WILL HANDLE
    * CERTIFICATE AND FORWARD TO APP SERVER (WHICH THEN ONLY NEEDS HTTP INTERNALLY)
     */
    @Shared String protocol = "http://"

    @LocalServerPort private int port


    @Shared String exchangeIntro
    @Shared String appVersion
    @Shared String apiVersion = '1'

    @Shared Long compId
    @Shared Long branchId
    @Shared Long deptId


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

    // ########################################################################

    // company/create
    void "[superuser] POST create COMPANY"() {
        setup:"api is called"
            println(" ")
            println("[superuser] POST create COMPANY")
            String METHOD = "POST"
            String controller = 'company'
            String action = 'create'

            LinkedHashMap data = ['name':'Spamazon']
            String json = JsonOutput.toJson(data)

            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);


            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}" as String

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
            ArrayList infoList = info.keySet()


        when:"info is not null"
            assert info!=[:]
            this.compId = info.id
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
    }

    // company/show
    void "[superuser] GET show COMPANY"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET show COMPANY : ${this.compId}")
            String METHOD = "GET"
            String controller = 'company'
            String action = 'show'

            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.compId}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setHeader(new BasicHeader("X-LINK-RELATIONS","true"));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
    }



    // ########################################################################

    // branch/create
    void "[superuser] POST create BRANCH"() {
        setup:"api is called"
            println(" ")
            println(" [superuser] POST create BRANCH ")
            String METHOD = "POST"
            String controller = 'branch'
            String action = 'create'

            LinkedHashMap data = ['name':'Spamazon Branch','companyId':this.compId]
            String json = JsonOutput.toJson(data)

            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);


            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}" as String

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
            ArrayList infoList = info.keySet()

        when:"info is not null"
            assert info!=[:]
            this.branchId = info.id
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
    }

    // branch/show
    void "[superuser] GET show BRANCH"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET show BRANCH")
            String METHOD = "GET"
            String controller = 'branch'
            String action = 'show'

            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.branchId}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setHeader(new BasicHeader("X-LINK-RELATIONS","true"));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info[0].keySet()
        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
            assert info[1].path!=null
    }


    void "[superuser] GET show BRANCH (from cache)"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET show BRANCH")
            String METHOD = "GET"
            String controller = 'branch'
            String action = 'show'

            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.branchId}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setHeader(new BasicHeader("X-LINK-RELATIONS","true"));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info[0].keySet()

        when:"info is not null"
            assert info!=[:]

        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
            assert info[1].path!=null
    }



    // ########################################################################

    // dept/create
    void "[superuser] POST create DEPT"() {
        setup:"api is called"
            println(" ")
            println("[superuser] POST create DEPT")
            String METHOD = "POST"
            String controller = 'dept'
            String action = 'create'

            LinkedHashMap data = ['name':"Spam Dept",'branchId':"${this.branchId}"]
            String json = JsonOutput.toJson(data)

            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);


            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}" as String


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
            ArrayList infoList = info.keySet()

        when:"info is not null"
            assert info!=[:]
            this.deptId = info.id
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
    }

    // dept/show
    void "[superuser] GET show DEPT"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET show DEPT")
            String METHOD = "GET"
            String controller = 'dept'
            String action = 'show'

            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.deptId}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setHeader(new BasicHeader("X-LINK-RELATIONS","true"));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info[0].keySet()


        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == returnsList.intersect(infoList)
            assert info[1].path!=null
    }

    void "[superuser] GET show DEPT (from cache)"() {
        setup:"api is called"
        println(" ")
        println("[superuser] GET show DEPT")
        String METHOD = "GET"
        String controller = 'dept'
        String action = 'show'

        LinkedHashMap cache = apiCacheService.getApiCache(controller)
        this.appVersion = getVersion()
        this.exchangeIntro = "v${this.appVersion}"

        ArrayList returnsList = []
        def apiObject = cache?."${this.apiVersion}"?."${action}"
        apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

        String adminAuth = apiProperties.getSecurity().getSuperuserRole()
        apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

        String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.deptId}" as String

        HttpClient client = new DefaultHttpClient();
        //URL uri = new URL(url);
        HttpGet request = new HttpGet(url)
        request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
        request.setHeader(new BasicHeader("X-LINK-RELATIONS","true"));
        HttpResponse response = client.execute(request);

        int statusCode = response.getStatusLine().getStatusCode()

        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        Object info = new JsonSlurper().parseText(responseBody)
        ArrayList infoList = info[0].keySet()


        when:"info is not null"
        assert info!=[:]
        then:"get user"
        assert statusCode == 200
        assert infoList == returnsList.intersect(infoList)
        assert info[1].path!=null
    }







    void "Cleaning up data"() {
        setup:
            println("")
            println("Cleaning up data")
            println("dept : "+this.deptId)
            println("branch : "+this.branchId)
            println("comp : "+this.compId)
            deptService.deleteById(this.deptId);
            branchService.deleteById(this.branchId);
            compService.deleteById(this.compId);

            //Company comp = compService.findById(this.compId);
            //Branch branch = branchService.findById(this.branchId);
            //Dept dept = deptService.findById(this.deptId);
        when: "test if they exist"
            assert true
            //assert comp==null
            //assert branch==null
            //assert dept==null
        then: "delete all"
            assert true
    }




    // ########################################################################

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