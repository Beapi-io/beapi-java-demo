package demo.application.config;


import demo.application.filter.JwtRequestFilter;
import demo.application.service.JwtUserDetailsService;
import io.beapi.api.filter.RequestInitializationFilter;
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

@Order(1000)
@Configuration
@EnableWebSecurity(debug=false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfiguration(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


/*
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<JwtRequestFilter> jwtRequestFilter() {
        FilterRegistrationBean<JwtRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtRequestFilter());
        registrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER+1);
        //registrationBean.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER-100)
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

 */

    @Override
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().cors().configurationSource(corsConfigurationSource());
        httpSecurity.authorizeRequests().antMatchers("/authenticate", "/register").permitAll().anyRequest().authenticated();
        httpSecurity.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost","http://localhost:80","http://localhost:8080", "http://127.0.0.1:80", "http://test.nosegrind.net"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.addExposedHeader("Access-Control-Allow-Headers");
        config.setAllowCredentials(true);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}