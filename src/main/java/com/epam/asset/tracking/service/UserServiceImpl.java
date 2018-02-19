package com.epam.asset.tracking.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.epam.asset.tracking.domain.User;
import com.epam.asset.tracking.domain.User.Role;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {
	
	User user = removeThis();
	
	private User removeThis() {
		User user = new User(Role.USER);
		user.setUsername("Adam");
		user.setPassword("$2a$04$I9Q2sDc4QGGg5WNTLmsz0.fvGv3OjoZyj81PrSFyGOqMphqfS2qKu");
		return user;
	}
	
	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		
		if(userId == null || !userId.equals(user.getUsername())) {
			throw new UsernameNotFoundException("Invalid username or password.");
		}
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), getAuthority());
	}

	
	private List<GrantedAuthority> getAuthority() {
		return Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

	@Override
	public List<User> findAll() {
		List<User> list = new ArrayList<>();
		//userDao.findAll().iterator().forEachRemaining(list::add);
		list.add(user);
		return list;
	}
}