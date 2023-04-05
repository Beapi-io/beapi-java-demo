package demo.application

import io.beapi.api.properties.ApiProperties
import io.beapi.api.utils.ApiDescriptor
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
import org.apache.http.impl.client.HttpClientBuilder
import sun.nio.fs.UnixUserPrincipals;

import java.nio.charset.StandardCharsets
import org.springframework.beans.factory.ListableBeanFactory
import demo.application.service.UserService
import demo.application.service.UserAuthorityService
import demo.application.domain.User
import demo.application.domain.UserAuthority


@TestPropertySource(locations="classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AutomatedFunctionalTest extends Specification {


    @Autowired
    private ListableBeanFactory listableBeanFactory;

    @Autowired
    ApiCacheService apiCacheService

    @Autowired
    UserService userService

    @Autowired
    UserAuthorityService uAuthService

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    ApiProperties apiProperties

    @Shared String testUserToken

    @Value("\${server.address}")
    String serverAddress;

	@Value("\${api.autoTest}")
	String autoTest

	@Value("\${api.protocol}")
	String protocol

    @LocalServerPort private int port

    @Autowired
    private demo.application.service.JwtTokenUtil jwtTokenUtil;

    HttpClient client = new DefaultHttpClient();

    Object currentObject


    void "[testuser] login"(){
        setup:"logging in"
        //HttpClient httpClient = new DefaultHttpClient();
        LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser()


        String loginUri = "/authenticate"
        String url = "${protocol}://${this.serverAddress}:${this.port}/${loginUri}" as String
        String json = "{\"username\":\"${testUser['login']}\",\"password\":\"${testUser['password']}\"}"
        HttpEntity stringEntity = new StringEntity(json,ContentType.APPLICATION_JSON);

        HttpPost request = new HttpPost(url)
        request.setEntity(stringEntity);
        HttpResponse response = this.client.execute(request);

        //int statusCode = response.getStatusLine().getStatusCode()
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        Object info = new JsonSlurper().parseText(responseBody)

        when:"info is not null"
            this.testUserToken = info.token
            String username = jwtTokenUtil.getUsernameFromToken(this.testUserToken);
        then:"assert token is not null"
            assert info.token!=[:]
            // todo : also check that ROLE_ADMIN response vars are not in keyset
    }

    void "[testuser] automated test"() {
        setup:"apidocs called"
            LinkedHashMap testList = [:]
            Set contList = []
            Object obj
            int objId

            // have to first order the calls by put/get/post/delete
            String username = jwtTokenUtil.getUsernameFromToken(this.testUserToken);
            User user = userService.findByUsername(username)
            ArrayList<UserAuthority> uAuth = uAuthService.findByUser(user)
            String authority = uAuth[0].getAuthority().getAuthority()

            ArrayList ignoreList = ['deprecated', 'defaultAction', 'testOrder']
            LinkedHashMap<String, Object> cont = listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
            String version = getVersion()

            cont.each(){ k, v ->

                LinkedHashMap cache = apiCacheService.getApiCache(k)
                if(cache!=null && !cache?.isEmpty()) {
                    if(cache['networkGrpRoles'].contains(authority)) {
                        contList.add(k)
                        String apiVersion = cache['currentstable']
                        ArrayList keyList = cache[apiVersion].keySet()
                        ignoreList.intersect(keyList).each() { it -> keyList.remove(it) }
                        keyList.each() { action ->
                            ApiDescriptor apiObject = cache[apiVersion][action]

                            String method = apiObject.method
                            if(!testList["${method}"]){ testList["${apiObject.method}"] = [:] }
                            if(!testList["${method}"]["${k}"]) { testList["${method}"]["${k}"] = [:] }
                            if(!testList["${method}"]["${k}"]["${action}"]){ testList["${method}"]["${k}"]["${action}"] = [:] }

                            LinkedHashMap tempReceives = getMockParameters(apiObject.receives, authority)
                            LinkedHashMap tempReturns = getMockParameters(apiObject.returns, authority)
                            if(!tempReceives.isEmpty()){ testList["${method}"]["${k}"]["${action}"]['receives'] = tempReceives }
                            if(!tempReturns.isEmpty()){ testList["${method}"]["${k}"]["${action}"]['returns'] =  tempReturns }
                        }
                    }
                }

                String exchangeIntro = "v${version}"
                ArrayList order = ['POST','GET','PUT','DELETE']
                order.each() { method ->
                    testList["${method}"].each() { controller, v2 ->
                        v2.each() { action, v3 ->
                            String url = "${protocol}://${this.serverAddress}:${this.port}/${exchangeIntro}/${controller}/${action}" as String
                            //println("[${method}] ${controller}/${action} > ${v3['receives']}")
                            //println("[${method}] ${controller}/${action} > ${v3['returns']}")
                            apiCall(method, url, v3['receives'], v3['returns'])
                            // if(obj==null && ['POST','GET'].contains(method)){ create obj }
                        }
                    }
                }
                testList = [:]
            }

        when:"info is not null"
            assert true==true
        then:"get authority"
            assert false==true
    }

    private LinkedHashMap getMockParameters(LinkedHashMap input, String role){
        LinkedHashMap params = [:]
        input.each(){ k, v ->
            if(k=='permitAll' || k==role){
                v.each() { it ->
                    if (it.name) {
                        if (it.mockData != null && !it.mockData.isEmpty()) {
                            params["${it.name}"] = it.mockData
                        } else {
                            assert it.mockData != null
                            assert !it.mockData.isEmpty()
                        }
                    }
                }
            }
        }
        return params
    }

    private void apiCall(String method, String url, LinkedHashMap receives, LinkedHashMap returns) {
        def request
        println("### URL : ${url}")
        switch (method) {
            case 'GET':
                String params = urlEncode(receives)
                if (params != null && !params.isEmpty()) {
                    String newUrl = url + "?" + params
                    request = new HttpGet(newUrl)
                } else {
                    request = new HttpGet(url)
                }
                break;
            case 'PUT':
                //request = new HttpPut(url)
                break;
            case 'POST':
                //request = new HttpPost(url)
                break
            case 'DELETE':
                String params = urlEncode(receives)
                if (params != null && !params.isEmpty()) {
                    String newUrl = url + "?" + params
                    //request = new HttpDelete(newUrl)
                } else {
                    //request = new HttpDelete(url)
                }
                break
        }
        if (request != null) {
            request.setHeader(new BasicHeader("Content-Type", "application/json"));
            request.setHeader(new BasicHeader("Authorization", "Bearer " + this.testUserToken));
            if (['PUT', 'POST'].contains(method)) {
                String json = JsonOutput.toJson(receives)
                HttpEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
                request.setEntity(stringEntity);
            }
            HttpResponse response = this.client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode()
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            println("status: "+statusCode)
            if(responseBody!=null && !responseBody.isEmpty()) {
                String info = new JsonSlurper().parseText(responseBody)
                println(info)
            }
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

    private String urlEncode(LinkedHashMap data){
        if(data?.keySet()) {
            String encodedURL = data.keySet().collect(key -> key + "=" + encodeValue(data.get(key))).join("&");
            return encodedURL
        }else{
            return null
        }
    }

    private String encodeValue(String value) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error encoding parameter {}", e.getMessage(), e);
            println("Error encoding parameter {}"+ e.getMessage());
        }
        return encoded;
    }

}
