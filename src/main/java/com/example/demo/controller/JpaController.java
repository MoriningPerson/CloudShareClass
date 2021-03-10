package com.example.demo.controller;


import com.example.demo.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.utils.UserUtil.*;

import java.util.*;

@RestController
public class JpaController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    static int user_id = 10;
    static int post_id = 10;

    @RequestMapping(value = "/User/insert", method = RequestMethod.POST)
    public String userInsert(@RequestParam(value = "name")String name,
                             @RequestParam(value = "school")String school,
                             @RequestParam(value = "telephone")String telephone,
                             @RequestParam(value = "password")String password) {
        user_id++;
        password = bCryptPasswordEncoder.encode(password);
        String portrait_url = "http://47.100.79.77/img/portrait.f98bd381.svg";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT * FROM User WHERE name = ?", name);
        if (list.size() == 1) {
            return "用户名重复";
        } else if (UserUtil.vaildatePassword(password).equals("★★☆☆☆☆")) {
            return "密码太弱";
        } else {
            jdbcTemplate.update("INSERT INTO User VALUES (?, ?, ?, ?, ?, ?)",
                    user_id, name, school, telephone, password, portrait_url);
            return "注册成功";
        }
    }

    @RequestMapping("/User/getDetail")
    public List<Map<String, Object>> userGetDetail() {
        String name = "";
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser") {
            // 如果已经登录了
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
            name = userDetails.getUsername();
        }
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id, name, school, telephone, portrait_url " +
                        "FROM User " +
                        "WHERE name = ? ", name);
        return list;
    }

    @RequestMapping("/Course/hot")
    public List<Map<String, Object>> courseHot() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT course_id, name, url, cover, origin, score, counter, type " +
                        "FROM Mycourse " +
                        "WHERE type <> ? " +
                        "ORDER BY counter DESC " +
                        "LIMIT 5","resource");
        for (Map<String, Object> object : list) {
            int course_id = (int)object.get("course_id");
            String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id = " + course_id,String.class);
            String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id = " + course_id,String.class);
            object.put("titleList",titleList);
            object.put("universityList",universityList);
        }
        return list;
    }

    @RequestMapping("/Posting/hot")
    public List<Map<String, Object>> postingHot() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT post_id, type, title, content, counter, post_time, user_id, name, portrait_url " +
                        "FROM Posting NATURAL JOIN Post NATURAL JOIN User " +
                        "ORDER BY counter DESC " +
                        "LIMIT 3");
        return list;
    }

    @RequestMapping("/User/recentBrowse")
    public List<Map<String, Object>> userRecentBrowse() {
        String name = "";
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser") {
            // 如果已经登录了
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
            name = userDetails.getUsername();
        }
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList(
                "SELECT Browse.course_id, User.name, url, cover, origin, score, counter, type " +
                        "FROM Browse NATURAL JOIN Mycourse NATURAL JOIN User " +
                        "WHERE User.name = ? " +
                        "ORDER BY Browse.browse_time DESC " +
                        "LIMIT 5", name);
        int list1len = list1.size();
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                "SELECT course_id, name, url, cover, origin, score, counter, type " +
                        "FROM Mycourse " +
                        "ORDER BY counter DESC " +
                        "LIMIT ?", 5 - list1len);
        list1.addAll(list2);
        for (Map<String, Object> object : list1) {
            int course_id = (int)object.get("course_id");
            String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id = " + course_id,String.class);
            String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id = " + course_id,String.class);
            object.put("titleList",titleList);
            object.put("universityList",universityList);
        }
        return list1;
    }

    @RequestMapping("/Posting/searchByCourseId")
    public List<Map<String, Object>> postingSearchByCourseId(@RequestParam(value = "course_id")int course_id) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT post_id, type, title, content, counter, post_time, user_id, name, portrait_url " +
                        "FROM Posting NATURAL JOIN Post NATURAL JOIN User NATURAL JOIN Relative " +
                        "WHERE course_id = ?", course_id);
        return list;
    }

    @RequestMapping("/Rate/insert")
    public String rateInsert(@RequestParam(value = "course_id")int course_id,
                             @RequestParam(value = "score")int score) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                "SELECT * " +
                        "FROM Rate " +
                        "WHERE user_id = ? AND course_id = ? ", user_id, course_id);
        if (list2.size() == 1) {
            return "评分失败";
        } else {
            int rate_time = (int)(System.currentTimeMillis() / 1000);
            jdbcTemplate.update("INSERT INTO Rate VALUES (?, ?, ?, ?)",
                    user_id, course_id, rate_time, score);
            jdbcTemplate.update("UPDATE Mycourse " +
                            "SET score = (SELECT AVG(score) as avg FROM Rate WHERE course_id = ?) " +
                            "WHERE course_id = ? ",
                    course_id, course_id);
            jdbcTemplate.update("UPDATE Course " +
                    "SET score = ( SELECT score FROM Mycourse WHERE course_id = ?)",course_id);
            return "评分成功";
        }
    }

    @RequestMapping("/Star/insert")
    public String starInsert(@RequestParam(value = "course_id")int course_id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        list = jdbcTemplate.queryForList("SELECT * FROM STAR" +
                "WHERE course_id = ? and user_id = ?",course_id,user_id);
        if (list.size() > 0) {
            return "收藏失败";
        }
        jdbcTemplate.update("INSERT INTO Star VALUES (?, ?)", course_id, user_id);
        return "收藏成功";
    }

    @RequestMapping("/Star/search")
    public List<Map<String, Object>> starSearch() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                "SELECT Temp.course_id, Course.name, Course.url, Course.cover, Course.origin, Mycourse.type, Course.universityList, Course.titleList, Course.contentList " +
                        "FROM (SELECT course_id AS course_id FROM Star WHERE user_id = ? ) as Temp, Course, Mycourse " +
                        "WHERE Temp.course_id = Course.course_id AND Course.course_id = Mycourse.course_id ", user_id);
        return list2;
    }

    @RequestMapping("/Post/insert")
    public String postInsert(@RequestParam(value = "type")String type,
                             @RequestParam(value = "title")String title,
                             @RequestParam(value = "content")String content) {
        post_id++;
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        int post_time = (int)System.currentTimeMillis() / 1000;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        jdbcTemplate.update("INSERT INTO Post VALUES (?, ?)", user_id, post_id);
        jdbcTemplate.update("INSERT INTO Posting VALUES (?, ?, ?, ?, ?, ?)",
                post_id, type, title, content, 0, post_time);
        return "发布成功";
    }

    @RequestMapping("/Course/post/insert")
    public String postInsertCourse(@RequestParam(value = "course_id")int course_id,
                                   @RequestParam(value = "type")String type,
                                   @RequestParam(value = "title")String title,
                                   @RequestParam(value = "content")String content) {
        post_id++;
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        int post_time = (int)System.currentTimeMillis() / 1000;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        jdbcTemplate.update("INSERT INTO Post VALUES (?, ?)", user_id, post_id);
        jdbcTemplate.update("INSERT INTO Posting VALUES (?, ?, ?, ?, ?, ?)",
                post_id, type, title, content, 0, post_time);
        jdbcTemplate.update("INSERT INTO `Relative` VALUES (?, ?)",
                course_id,post_id);
        return "发布成功";
    }

    @RequestMapping("/Posting/like")
    public String postingLike(@RequestParam(value = "post_id")int post_id) {
        jdbcTemplate.update("UPDATE Posting " +
                "SET counter = counter + 1 " +
                "WHERE post_id = ? ", post_id);
        return "收藏成功";
    }

    @RequestMapping("/Posting/comment")
    public String postingComment(@RequestParam(value = "post_id")int post_id,
                               @RequestParam(value = "content")String content) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        jdbcTemplate.update("INSERT INTO Comment " +
                "VALUES (?, ?, ?, ?) ", user_id, post_id, content, (int)(System.currentTimeMillis() / 1000));
        return "评论成功";
    }

    @RequestMapping("/Posting/all")
    public List<Map<String, Object>> postingSearch() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT post_id, type, title, content, counter, post_time, user_id, name, portrait_url " +
                        "FROM Posting NATURAL JOIN Post NATURAL JOIN User " +
                        "ORDER BY counter");
        return list;
    }

    @RequestMapping("User/check")
    public String checkInfo(@RequestParam(value="username")String username,
                            @RequestParam(value="password")String password)
    {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", username);
        if (list.size() != 0) {
            return "用户名重复";
        } else if (UserUtil.vaildatePassword(password).equals("★★☆☆☆☆")) {
            return "密码太弱";
        } else {
            return "校验成功";
        }
    }

    @RequestMapping("/Course/detail")
    public List<Map<String, Object>> courseDetail(@RequestParam(value="course_id")int course_id) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT course_id, name, url, cover, origin, score, counter, type " +
                        "FROM Mycourse " +
                        "WHERE course_id = ?",course_id);
        for (Map<String, Object> object : list) {
            String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id = " + course_id,String.class);
            String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id = " + course_id,String.class);
            object.put("titleList",titleList);
            object.put("universityList",universityList);
        }
        return list;
    }

    @RequestMapping("Course/relative")
    public List<Map<String, Object>> courseRelative(@RequestParam(value="course_id")int course_id) {
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList(
                "SELECT course_id,name,url,cover,origin,type " +
                        "FROM Mycourse " +
                        "WHERE relative_id = ? and Course.course_id <> ? " +
                        "ORDER BY score " +
                        "LIMIT 10",
                course_id,course_id
        );
        String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id=" + course_id,String.class);
        String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id=" + course_id,String.class);
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                "SELECT course_id,name,url,cover,origin,type " +
                        "FROM Course " +
                        "WHERE titleList = ? and universityList = ? and course_id <> ? " +
                        "LIMIT ?"
                        ,titleList,universityList,course_id,10-list1.size()
        );
        list1.addAll(list2);
        for (Map<String, Object> object : list1) {
            titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id = " + course_id,String.class);
            universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id = " + course_id,String.class);
            object.put("titleList",titleList);
            object.put("universityList",universityList);
        }
        return list1;
    }

    @RequestMapping("/User/browse")
    public String browse(@RequestParam(value="course_id")int course_id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        jdbcTemplate.update("INSERT INTO Browse VALUES (?, ?, ?)", course_id, user_id,  (int)(System.currentTimeMillis() / 1000));
        jdbcTemplate.update("UPDATE Mycourse SET counter = counter + 1 " +
                "WHERE course_id=?",course_id);
        return "浏览成功";
    }
}
