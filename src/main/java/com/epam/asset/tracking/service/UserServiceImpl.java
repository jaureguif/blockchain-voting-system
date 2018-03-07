package com.epam.asset.tracking.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.User;
import com.epam.asset.tracking.repository.BusinessProviderRepository;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {

	@Autowired
	BusinessProviderRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		return userRepository.findByUsername(userName)
			.map(user -> new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), getAuthority()))
			.orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
	}

	private List<GrantedAuthority> getAuthority() {
		return Arrays.asList(new SimpleGrantedAuthority("ROLE_BUSINESS_PROVIDER"));
	}

	@Override
	public List<User> findAll() {
		throw new NotImplementedException();
	}
}