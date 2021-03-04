package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController
{
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @RequestMapping(value = "/return0")
    public int return0()
    {
        return 0;
    }
    @RequestMapping(value = "/return1")
    public int return1()
    {
        return 1;
    }
    @RequestMapping(value = "/signIn")
    public String signIn()
    {
        return "SignIn";
    }
    @RequestMapping(value = "/signOut")
    public String signOut()
    {
        return "signOut";
    }

    @RequestMapping(value = "/getPassword")
    public String getPassword(@RequestParam(value = "password",required = true) String password)
    {
        String tmp = bCryptPasswordEncoder.encode(password);
        return tmp;
    }
}
