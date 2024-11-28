package com.example.demo_authentication.service;

import com.example.demo_authentication.data.UserEntity;
import com.example.demo_authentication.data.UserEntityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserLoginService implements UserDetailsService {

  private final UserEntityRepository userEntityRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // username 가져오기
    Optional<UserEntity> userEntity = userEntityRepository.findById(username);
    if (userEntity.isEmpty()) {
      throw new UsernameNotFoundException(username);
    }
    UserEntity user = userEntity.get();
    // ROLE 설정
    List<GrantedAuthority> authorities = new ArrayList<>(); // 인증 권한 저장 기능 -> grantedAuthority
    if(user.getUsername().equals("aaa")) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    } else {
      authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // ROLE_ "반드시 기입 필요"
    }

    // security 전달할 새로운 객체 생성 -> entity에 있는 username, password, ROLE 설정한 값
    return new User(user.getUsername(), user.getPassword(), authorities);
  }



}
