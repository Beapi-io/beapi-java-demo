![alt text](https://github.com/orubel/logos/blob/master/beapi_logo_large.png)
# spring-boot-starter-beapi Java Demo

**Springboot Version** - 2.6.2 (or greater)

**JVM** - 17 

**Build**
```
gradle clean;gradle build
 ```

**Getting a Token** - 

This will get you your BEARER token to use in your calls/app:
```
curl -v -H "Content-Type: application/json" -X POST -d '{"username":"admin","password":"@6m!nP@s5"}' http://localhost:8080/authenticate
```

**Calling your API**

Then call your api normally:
```
curl -v -H "Content-Type: application/json" -H "Authorization: Bearer {your_token_here}" --request GET "http://localhost:8080/v{appVersion}/user/show/5"
```
NOTE:  appVersion can be found in your gradle.properties file as 'version'

**Configuration Files** - https://github.com/orubel/spring-boot-starter-beapi-config 
Follow instructions in documentation for configuration

**Documentation** - [https://beapi-io.github.io/spring-boot-starter-beapi/](https://beapi-io.github.io/spring-boot-starter-beapi/)

