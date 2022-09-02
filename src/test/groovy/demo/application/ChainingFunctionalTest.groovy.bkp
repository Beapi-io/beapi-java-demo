package demo.application

import demo.application.domain.Authority
import demo.application.domain.Company
import demo.application.domain.Branch
import demo.application.domain.Dept
import demo.application.service.AuthorityService
import demo.application.service.CompanyService
import demo.application.service.BranchService
import demo.application.service.DeptService

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
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpDelete
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.entity.StringEntity
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpResponse
import org.apache.http.message.BasicHeader

import java.nio.charset.StandardCharsets

@TestPropertySource(locations="classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChainingFunctionalTest extends Specification {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    private ApiProperties apiProperties

    @Autowired AuthorityService authService

    @Autowired ApiCacheService apiCacheService

    @Autowired CompanyService compService
    @Autowired BranchService branchService
    @Autowired DeptService deptService

    @Autowired PrincipleService principle

    @Shared String adminUserToken



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
            println("[superuser] GET show COMPANY")
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
            println(" [superuser] POSTcreate BRANCH ")
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

    // dept/create
    void "[superuser] POST create DEPT"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET create DEPT")
            String METHOD = "POST"
            String controller = 'dept'
            String action = 'create'

            LinkedHashMap data = ['name':'Spam Dept','branchId':this.branchId]
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






    void "[superuser] API CHAINING(R) blankchain test"() {
        setup:"api is called"
            println(" ")
            println("[superuser] API CHAINING(R) blankchain test with '${this.deptId}'")
            String controller = 'dept'
            String action = 'show'

            LinkedHashMap data = ['chain':['initdata':'branchId','chaintype':'blankchain','order':['branch/show':'companyId','company/show':'return']]]
            String json = JsonOutput.toJson(data)
            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "c${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.deptId}" as String

            HttpClient client = new DefaultHttpClient();

            // NOTE : we have to use HttpPost because it won't allow sending formdata in HttpGet
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
            assert info.size() == 3
    }



    void "[superuser] API CHAINING(R) postchain test"() {
        setup:"api is called"
            println(" ")
            println("[superuser] API CHAINING(R) postchain test")
            String controller = 'dept'
            String action = 'show'
            LinkedHashMap chainData = ['initdata':'branchId','chaintype':'postchain','order':['branch/show':'companyId','company/update':'return']]
            LinkedHashMap data = ['id':this.compId,'name':'YabbaDabbaDooazon','chain': chainData]
            String json = JsonOutput.toJson(data)

            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "c${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.deptId}" as String

            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPut request = new HttpPut(url)
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
            assert info.size() == 3
    }


    void "[superuser] API CHAINING(R) prechain test"() {
        setup:"api is called"
            println(" ")
            println("[superuser] API CHAINING(R) prechain test")
            String controller = 'dept'
            String action = 'update'
            LinkedHashMap chainData = ['initdata':'branchId','chaintype':'prechain','order':['branch/show':'companyId','company/show':'return']]
            LinkedHashMap data = ['id':this.deptId,'name':'YabbaDabbaDoo Dept','chain': chainData]
            String json = JsonOutput.toJson(data)

            HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "c${this.appVersion}"

            ArrayList returnsList = []
            def apiObject = cache?."${this.apiVersion}"?."${action}"
            apiObject?.returns?.permitAll?.each(){ it -> returnsList.add(it.name) }

            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            apiObject?.returns?."${adminAuth}".each() { it2 -> returnsList.add(it2.name) }

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}/${this.deptId}" as String


            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPut request = new HttpPut(url)
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
            assert info.size() == 3
    }


    // ########## CLEANUP ########################################################################

    // dept/delete
    void "[superuser] DELETE delete DEPT"() {
        setup:"api is called"
            println(" ")
            String METHOD = "DELETE"
            String controller = 'dept'
            String action = 'delete'

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
            HttpDelete request = new HttpDelete(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)


        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert info.id.toLong() == this.deptId
    }

    // branch/delete
    void "[superuser] DELETE delete BRANCH"() {
        setup:"api is called"
            println(" ")
            String METHOD = "DELETE"
            String controller = 'branch'
            String action = 'delete'

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
            HttpDelete request = new HttpDelete(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)


        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert info.id.toLong() == this.branchId
    }

    // company/delete
    void "[superuser] DELETE delete COMPANY"() {
        setup:"api is called"
            println(" ")
            String METHOD = "DELETE"
            String controller = 'company'
            String action = 'delete'

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
            HttpDelete request = new HttpDelete(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)


        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert info.id.toLong() == this.compId
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