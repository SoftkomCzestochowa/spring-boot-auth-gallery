package com.quasar.controllers;

import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController
{

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getLoginPage(@RequestParam Optional<String> error)
	{
		for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
		{
			System.out.println(ga.getAuthority());
		}

		return "login_form";
	}

	@RequestMapping(value = "/access-denied", method = RequestMethod.GET)
	public String getAccessDeniedPage(@RequestParam Optional<String> error)
	{
		return "access_denied";
	}
	

	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public String adminPage(ModelMap model) {
		model.addAttribute("user", getPrincipal());
		return "admin";
	}
	
	
	private String getPrincipal(){
		String userName = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			userName = ((UserDetails)principal).getUsername();
		} else {
			userName = principal.toString();
		}
		return userName;
	}
}