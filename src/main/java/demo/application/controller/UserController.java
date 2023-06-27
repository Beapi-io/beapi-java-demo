package demo.application.controller;

import demo.application.domain.Authority;
import demo.application.domain.UserAuthority;
import demo.application.service.AuthorityService;
import demo.application.service.UserAuthorityService;
import io.beapi.api.service.PrincipleService;
import io.beapi.api.controller.BeapiRequestHandler;
import demo.application.domain.User;
import demo.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller("user")
public class UserController extends BeapiRequestHandler{

	@Autowired
	public PasswordEncoder passwordEncoder;

	@Autowired
	PrincipleService principle;

	@Autowired
	UserService userService;

	@Autowired
	private AuthorityService authService;

	@Autowired
	private UserAuthorityService uAuthService;



	public List<User> list(HttpServletRequest request, HttpServletResponse response){
			List<User> users = userService.getAllUsers();
			return users;
	}

	public User show(HttpServletRequest request, HttpServletResponse response){
			String username;
			if(principle.isSuperuser()){
				username = (Objects.nonNull(this.params.get("id")))? (this.params.get("id")):principle.name();
			}else {
				username = principle.name();
			}

			User user = userService.findByUsername(username);

			if (Objects.nonNull(user)) {
				return user;
			}
			return null;
    }

	// admin can pass a role else defaults to 'ROLE_USER
	public User create(HttpServletRequest request, HttpServletResponse response){

			String role = this.params.get("role");
			Authority auth = authService.findByAuthority(role);

			User user = new User();
			user.setUsername(this.params.get("login"));
			user.setEmail(this.params.get("email"));
			user.setPassword(passwordEncoder.encode(this.params.get("password")));

			// todo : need rollback upon fail
			if(Objects.nonNull(userService.save(user))){
				UserAuthority uAuth = new UserAuthority();
				uAuth.setUser(user);
				uAuth.setAuthority(auth);
				uAuthService.save(uAuth);
			}
			return user;

	}



	public User updatePassword(HttpServletRequest request, HttpServletResponse response){

			String username;
			if(principle.isSuperuser()){
				username = (Objects.nonNull(this.params.get("id")))? (this.params.get("id")):principle.name();
			}else {
				username = principle.name();
			}
			User user = userService.findByUsername(username);

			if (Objects.nonNull(user)) {
				user.setPassword(passwordEncoder.encode(this.params.get("password")));
				userService.save(user);
			}else{
				writeErrorResponse(response, "404", request.getRequestURI());
			}

			return user;
	}



	public User getByUsername(HttpServletRequest request, HttpServletResponse response){
		String username;
		if(principle.isSuperuser()){
			username = (Objects.nonNull(this.params.get("id")))? (this.params.get("id")):principle.name();
		}else {
			username = principle.name();
		}

		User user = userService.findByUsername(username);
		if (!Objects.nonNull(user)) {
			writeErrorResponse(response, "404", request.getRequestURI());
		}
		return user;
	}

	/*
	LinkedHashMap delete() {
		User user
		List prole
		try {
			user = User.get(params.id)
			if(user){
					prole = PersonRole.findAllByPerson(user)
					prole.each() {
						it.delete(flush: true, failOnError: true)
					}


					 // additional dependencies to be removed should be put here


					user.delete(flush: true, failOnError: true)
					return [person: [id: params.id.toLong()]]
			}else{
				render(status: 500,text:"Id " + params.id + " does not match record in database.")
			}
		}catch(Exception e){
			throw new Exception("[PersonController : delete] : Exception - full stack trace follows:",e)
		}
	}
*/

}
