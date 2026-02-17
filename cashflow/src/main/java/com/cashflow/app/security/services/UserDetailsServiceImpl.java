package com.cashflow.app.security.services;

import com.cashflow.app.entity.User;
import com.cashflow.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 支持用户名、邮箱或手机号登录
        User user = userRepository.findByUsernameOrEmailOrPhone(username, username, username)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with identifier: " + username));

        return UserDetailsImpl.build(user);
    }
}
