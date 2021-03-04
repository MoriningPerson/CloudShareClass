//package com.example.demo.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import javax.sql.DataSource;
//
//
//@Configuration
////@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter
//{
//    @Autowired
//    DataSource dataSource;
//
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        return bCryptPasswordEncoder;
//    }
//
//    @Autowired
//    BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .antMatchers("/Course/**").authenticated()  //  /Course/**需要验证身份
////                .antMatchers("/User/**").authenticated()  //  /User/**需要验证身份
//                .antMatchers("/shouldAuthority").authenticated()  //  /shouldAuthority需要验证身份
//                .anyRequest().permitAll()  //  其他都不需要验证
//                .and()
//                .httpBasic().and().csrf().disable();
////        http
////                .formLogin().loginProcessingUrl("/signIn")
////                .successForwardUrl("/return0").failureForwardUrl("/return1")
////                .usernameParameter("username").passwordParameter("password")
////                .and()
////                .logout().logoutUrl("/signOut")
////                .and()
////                .authorizeRequests()
////                .antMatchers("/user/**").authenticated()
////                .anyRequest().permitAll()
////                .and()
////                .requiresChannel()
////                .antMatchers("register", "login").requiresSecure()
////                .and()
////                .csrf().disable();
//    }
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.jdbcAuthentication().dataSource(dataSource)
//                .usersByUsernameQuery("select `name`,`password`,true from User where `name` = ? ")
//                .authoritiesByUsernameQuery("select `name`, true from User where `name` = ?")
//                .passwordEncoder(bCryptPasswordEncoder);
//    }
//}
package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Autowired
    DataSource dataSource;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .formLogin().loginProcessingUrl("/signIn")
                .successForwardUrl("/return0").failureForwardUrl("/return1")
                .usernameParameter("username").passwordParameter("password")
                .and()
                .logout().logoutUrl("/signOut")
                .and()
                .authorizeRequests()
//                .antMatchers("/Course/**").authenticated()  //  /Course/**需要验证身份
//                .antMatchers("/User/**").authenticated()  //  /User/**需要验证身份
//                .antMatchers("/shouldAuthority").authenticated()  //  /shouldAuthority需要验证身份
//                .anyRequest().permitAll()  //  其他都不需要验证
                .antMatchers("/User/login").authenticated()
                .antMatchers("/User/getDetail").authenticated()
                .antMatchers("/User/recentBrowse").authenticated()
                .antMatchers("/Post/**").authenticated()
                .antMatchers("/Posting/like").authenticated()
                .antMatchers("/Posting/comment").authenticated()
                .antMatchers("/shouldAuthority").authenticated()
                .anyRequest().permitAll()
                .and()
                .requiresChannel()
                .antMatchers("register", "login").requiresSecure()
                .and()
                .cors()
                .and()
                .csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery("select `name`,`password`,true from User where `name` = ?" )
                .authoritiesByUsernameQuery("select `name`,true from User where `name` = ?")
                .passwordEncoder(bCryptPasswordEncoder);
    }
}

