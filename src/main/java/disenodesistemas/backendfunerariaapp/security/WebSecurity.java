package disenodesistemas.backendfunerariaapp.security;

import disenodesistemas.backendfunerariaapp.security.jwt.JwtEntryPoint;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtTokenFilter;
import disenodesistemas.backendfunerariaapp.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
//con perPostEnabled se usa para indicar a q metodos puede acceder solo el admin
// Los metodos que no lleven anotación pueden acceder el admin como un generic user
// @preauthorized solo puede acceder el admin
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final JwtEntryPoint jwtEntryPoint;

    public WebSecurity(@Lazy final UserService userService, final JwtEntryPoint jwtEntryPoint) {
        this.userService = userService;
        this.jwtEntryPoint = jwtEntryPoint;
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter(){
        return new JwtTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/v1/users", "/api/v1/users/login","/api/v1/users/forgot-password", "/api/v1/users/reset-password", "/api/v1/addresses").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/users/activation", "/api/v1/categories").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic().and()
                .exceptionHandling().authenticationEntryPoint(jwtEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }

}
