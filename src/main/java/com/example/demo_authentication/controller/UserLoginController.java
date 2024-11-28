package com.example.demo_authentication.controller;

import com.example.demo_authentication.data.UserEntity;
import com.example.demo_authentication.data.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserLoginController {

  private final UserEntityRepository entityRepository;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/join")
  public ResponseEntity<String> join(@RequestBody UserEntity userEntity) {
    String password = passwordEncoder.encode(userEntity.getPassword()); // 비밀번호 암호화 코드
    userEntity.setPassword(password); // password를 암호화 한걸로 변경
    entityRepository.save(userEntity); // repository에 저장
    return ResponseEntity.status(HttpStatus.CREATED).body("가입성공");
  }

  @GetMapping("/admin")
  public ResponseEntity<String> admin() {
    return ResponseEntity.status(HttpStatus.OK).body("관리자입니다");
  }


}
