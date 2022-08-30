package demo.application.controller;

import demo.application.domain.Authority;
import demo.application.service.AuthorityService;
import demo.application.service.JwtUserDetailsService;
import demo.application.service.UserAuthorityService;
import demo.application.service.UserService;
import io.beapi.api.controller.BeapiController;
import io.beapi.api.properties.ApiProperties;
import io.beapi.api.service.PrincipleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import io.beapi.api.utils.ErrorCodes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Controller
public class AuthorityController extends BeapiController{

	@Autowired
	private AuthorityService authService;

	public List<Authority> list(HttpServletRequest request, HttpServletResponse response){
		List<Authority> auth = authService.findAll();
		return auth;
	}

	public Authority create(HttpServletRequest request, HttpServletResponse response){
			String authority = this.params.get("authority");

			Authority auth = authService.findByAuthority(authority);
			if(!Objects.nonNull(auth)){
                Authority newAuth = new Authority();;
                newAuth.setAuthority(authority);
                return authService.save(newAuth);
			}
			return null;
	}

/*
	LinkedHashMap update(){
		try{
			User user
			if(isSuperuser() && params?.id){
				user = User.get(params?.id?.toLong())
			}else{
				user = User.get(springSecurityService.principal.id)
			}
			if(user){
				user.username = params.username
				user.password = params.password
				user.email = params.email

				if(isSuperuser()){
					user.enabled = params.enabled
				}

				if(!user.save(flush:true,failOnError:true)){
					user.errors.allErrors.each { println(it) }
				}
				return [person:user]
			}else{
				render(status: 500,text:"Id does not match record in database.")
			}
		}catch(Exception e){
			throw new Exception("[PersonController : update] : Exception - full stack trace follows:",e)
		}
	}
*/

	/**
	 * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap

	void writeErrorResponse(HttpServletResponse response, String statusCode, String uri, String msg){
		response.setContentType("application/json");
		response.setStatus(Integer.valueOf(statusCode));
		if(msg.isEmpty()){
			msg = ErrorCodes.codes.get(statusCode).get("long");
		}
		String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}";
		response.getWriter().write(message);
		//response.writer.flush();
	}
	*/

}
