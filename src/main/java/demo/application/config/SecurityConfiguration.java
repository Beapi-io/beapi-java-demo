package demo.application.config;


import demo.application.filter.JwtRequestFilter;
import demo.application.service.JwtUserDetailsService;
import io.beapi.api.filter.RequestInitializationFilter;
//import io.beapi.api.filter.FilterChainExceptionHandler;
//import io.beapi.api.filter.CorsSecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.annotation.Order;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Order(1000)
@Configuration
@EnableWebSecurity(debug=false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    @Autowired
    private RequestInitializationFilter requestInitializationFilter;

    @Autowired
    private FilterRegistrationBean requestInitializationFilterRegistration;

    //@Autowired
    //private FilterChainExceptionHandler filterChainExceptionHandler;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfiguration(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

/*
    @Bean
    public CsrfTokenRepository tokenRepository() {
        return new CookieCsrfTokenRepository();
    }
 */


    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter();
    }



    // this registers filter with RequestMappingHandlerMapping
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<JwtRequestFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtRequestFilter());
        registrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER+1);
        //registrationBean.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER-100)
        registrationBean.addUrlPatterns("/authenticate","/register","/error");
        return registrationBean;
    }




    @Override
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }



    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().cors();
        httpSecurity.authorizeRequests().antMatchers("/authenticate", "/register").permitAll().anyRequest().authenticated();
        httpSecurity.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        //httpSecurity.exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/error"));
        httpSecurity.addFilterAfter(jwtRequestFilter(), ExceptionTranslationFilter.class);
        httpSecurity.addFilterAfter(requestInitializationFilter, JwtRequestFilter.class);
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }


}