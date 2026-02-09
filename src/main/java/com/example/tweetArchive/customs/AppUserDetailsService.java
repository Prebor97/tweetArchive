package com.example.tweetArchive.customs;

import com.example.tweetArchive.entities.UserInfo;
import com.example.tweetArchive.repository.UserInfoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AppUserDetailsService implements UserDetailsService {
    private final UserInfoRepository userInfoRepository;

    public AppUserDetailsService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userInfoRepository.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("User Not Found"));
        AppUserDetails userDetails = new AppUserDetails(user);
        return userDetails;
    }
}

