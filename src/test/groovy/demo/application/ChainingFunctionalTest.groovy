package demo.application


import demo.application.domain.Company
import demo.application.domain.Branch
import demo.application.domain.Dept

import demo.application.service.CompanyService
import demo.application.service.BranchService
import demo.application.service.DeptService

import io.beapi.api.utils.ApiDescriptor
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
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.CookieStore
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.client.protocol.HttpClientContext

@TestPropertySource(locations="classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChainingFunctionalTest extends Specification {

    @Autowired ApplicationContext applicationContext
    @Autowired private ApiProperties apiProperties
    @Autowired ApiCacheService apiCacheService
    @Autowired CompanyService compService
    @Autowired BranchService branchService
    @Autowired DeptService deptService
    @Autowired PrincipleService principle

    @Shared String adminUserToken
    @Shared Cookie tuCookie
    @Shared Cookie suCookie
    

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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}" as String

            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPost request = new HttpPost(url)
            //request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setEntity(stringEntity);
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

        when:"info is not null"
            assert info!=[:]
            this.compId = info.id
        then:"get user"
            assert statusCode == 200
            assert infoList == infoList.intersect(returnsList)
    }

    // company/show
    void "[superuser] GET show COMPANY"() {
        setup:"api is called"
            println(" ")
            println("[superuser] GET show COMPANY")
            String METHOD = "GET"
            String controller = 'company'
            String action = 'show'

            println("companyId : "+this.compId)
            LinkedHashMap cache = apiCacheService.getApiCache(controller)
            this.appVersion = getVersion()
            this.exchangeIntro = "v${this.appVersion}"

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}?id=${this.compId}" as String

            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == infoList.intersect(returnsList)
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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}" as String

            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPost request = new HttpPost(url)
            //request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setEntity(stringEntity);
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

        when:"info is not null"
            assert info!=[:]
            this.branchId = info.id
        then:"get user"
            assert statusCode == 200
            assert infoList == infoList.intersect(returnsList)
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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}?id=${this.branchId}" as String

            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()
        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == infoList.intersect(returnsList)
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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}" as String


            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPost request = new HttpPost(url)
            //request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setEntity(stringEntity);
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()

        when:"info is not null"
            assert info!=[:]
            this.deptId = info.id
        then:"get user"
            assert statusCode == 200
            assert infoList == infoList.intersect(returnsList)
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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}?id=${this.deptId}" as String

            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpGet request = new HttpGet(url)
            request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            ArrayList infoList = info.keySet()


        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert infoList == infoList.intersect(returnsList)
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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}?id=${this.deptId}" as String


            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();

            // NOTE : we have to use HttpPost because it won't allow sending formdata in HttpGet
            HttpPost request = new HttpPost(url)
            //request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setEntity(stringEntity);
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            //println(info)

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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}?id=${this.deptId}" as String

            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPut request = new HttpPut(url)
            //request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setEntity(stringEntity);
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()

            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Object info = new JsonSlurper().parseText(responseBody)
            //println(info)

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

            def apiObject = cache?."${this.apiVersion}"?."${action}"
            String adminAuth = apiProperties.getSecurity().getSuperuserRole()
            Set returnsList = getResponseData(adminAuth, apiObject)

            String url = "${protocol}${this.serverAddress}:${this.port}/${this.exchangeIntro}/${controller}/${action}?id=${this.deptId}" as String

            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(suCookie);

            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            //URL uri = new URL(url);
            HttpPut request = new HttpPut(url)
            //request.setHeader(new BasicHeader("Content-Type","application/json"));
            request.setHeader(new BasicHeader("Authorization","Bearer "+this.adminUserToken));
            request.setEntity(stringEntity);
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse response = client.execute(request,localContext);

            int statusCode = response.getStatusLine().getStatusCode()
            println("statusCode:"+statusCode)
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            println("ResponseBody:"+responseBody)

            Object info = new JsonSlurper().parseText(responseBody)
            //println(info)

        when:"info is not null"
            assert info!=[:]
        then:"get user"
            assert statusCode == 200
            assert info.size() == 3
    }

    
    void "Cleaning up data"() {
        setup:
            deptService.deleteById(this.deptId);
            branchService.deleteById(this.branchId);
            compService.deleteById(this.compId);
        when: "test if they exist"
            assert true
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

    private Set getResponseData(String auth, ApiDescriptor apiObject){
        Set returnsList = []
        String adminAuth = apiProperties.getSecurity().getSuperuserRole()

        apiObject?.returns?."${auth}".each() { it2 -> returnsList.add(it2.name) }
        apiObject?.returns?."permitAll".each() { it2 -> returnsList.add(it2.name) }

        return returnsList
    }
}