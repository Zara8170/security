package com.example.demo_authentication.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean // 회원가입 할때마다 새로운 객체가 만들어지면, 메모리에 부담을 줄 수 있기 때문에, bean으로 등록하여 객체를 하나만 만들어서 계속 사용하기 위해 여기서 정의
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean // 재사용성, 유연성을 위해 함수 선언해서 successHandler에 집어넣는 것
  public AuthenticationSuccessHandler loginSuccessHandler() {
    return ((request, response, authentication) -> {

      Map<String, Object> responseData = new HashMap<>();
      responseData.put("result", "로그인 성공"); // 여기서는 Json 형태로 반환을 안해줘서 Json형태로 반환이 필요하다.

      Object principal = authentication.getPrincipal();
      if (principal instanceof UserDetails) { // principal 이 userdetails 구현햇냐?
        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();
        responseData.put("username", username);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        authorities.forEach(authority->{
          responseData.put("Authority", authority.getAuthority());
        });
      }



      // Json형태로 반환해주는 식
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonmessage = objectMapper.writeValueAsString(responseData); // Json String 형태로 반환해줌
//      response.setStatus(200); or
      response.setStatus(HttpStatus.OK.value()); // body
      response.setContentType("application/json"); // Json 형태 / header
      response.setCharacterEncoding("UTF-8"); // 한글 보낼때 설정? / header
      response.getWriter().write(jsonmessage); // 항상 문자열로 들어가야됨 / body
    });
  }

  @Bean
  public AuthenticationFailureHandler loginFailureHandler() {
    return ((request, response, authentication) -> {
      Map<String, Object> responseData = new HashMap<>();
      responseData.put("error", "로그인 실패");

      // Json형태로 반환해주는 식
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonmessage = objectMapper.writeValueAsString(responseData);
//      response.setStatus(200); or
      response.setStatus(401); // body
      response.setContentType("application/json"); // Json 형태 / header
      response.setCharacterEncoding("UTF-8"); // 한글 보낼때 설정 / header
      response.getWriter().write(jsonmessage); // 항상 문자열로 들어가야됨 / body
    });
  }

  @Bean
  public LogoutSuccessHandler logoutHandler() {
    return (request, response, authentication) -> {
      response.setStatus(HttpStatus.OK.value());
      response.getWriter().write("logout success");
    };
  }

  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {

    http.csrf(csrf->csrf.disable())
        .cors(cors->{})
        .authorizeHttpRequests(authorize->
//            authorize.requestMatchers("/**").permitAll() // "/**" -> 인증 안해도 다 받아주겟다
            authorize.requestMatchers("/","/login","/join").permitAll() // 로그인, 조인 허용
                .requestMatchers("/admin").hasRole("ADMIN") // admin으로 허용
                .anyRequest().authenticated() // 로그인에 성공해야만 가능하다
        );

    http.formLogin(form->
        form.loginProcessingUrl("/login")
            .successHandler(loginSuccessHandler()) // 만들어야됨 / 콜백함수가 전달되야됨
            .failureHandler(loginFailureHandler()) // 만들어야됨
    );
    http.logout(logout->
            logout.logoutUrl("/logout")
                .logoutSuccessHandler(logoutHandler()) // 로그아웃 끝나면 실행되는것 (서버쪽)
                .addLogoutHandler((request,response,authentication)-> {
                  if (request.getSession()!=null) {
                    request.getSession().invalidate(); // Session 끝낸다
                  }
                })  // 로그아웃이 될때, 실행되야되는 부분들
                .deleteCookies("JSESSIONID")
        );



    http.cors(cors->cors.configurationSource(request -> {
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);
      config.setAllowedOrigins(
          Arrays.asList("http://localhost:3000", "http://localhost:3001", "http://localhost:3002"));
      config.addAllowedHeader("*");
      config.addAllowedMethod("*");
      return config;
    }));
    return http.build();
  }
}
