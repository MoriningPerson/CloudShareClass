package com.example.demo.controller;


import com.example.demo.entity.Course;
import com.example.demo.service.ILuceneService;
import com.example.demo.utils.UserUtil;
import com.mysql.cj.x.protobuf.MysqlxCursor;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class JpaController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    static int user_id = 40;
    static int post_id = 70;

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
            jdbcTemplate.update("INSERT INTO User VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    user_id, name, school, telephone, password, portrait_url, "", "", "", "", "", "");
            return "注册成功";
        }
    }

    @RequestMapping("/User/update")
    public String updateInfo(@RequestParam(value="birth", required = false)String birth,
                             @RequestParam(value="nickname", required = false)String nickname,
                             @RequestParam(value="sex", required = false)String sex,
                             @RequestParam(value="signature", required = false)String signiture,
                             @RequestParam(value="education",required = false)String education,
                             @RequestParam(value="city",required = false)String city) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        if (birth != null && birth.length() > 0) {
            jdbcTemplate.update("UPDATE `User` SET birth=? WHERE user_id=?",birth,user_id);
        }
        if (education != null && education.length() > 0) {
            jdbcTemplate.update("UPDATE `User` SET education=? WHERE user_id=?",education,user_id);
        }
        if (nickname != null && nickname.length() > 0) {
            jdbcTemplate.update("UPDATE `User` SET nickname=? WHERE user_id=?",nickname,user_id);
        }
        if (city != null && city.length() > 0) {
            jdbcTemplate.update("UPDATE `User` SET city=? WHERE user_id=?", city,user_id);
        }
        if (signiture != null && signiture.length() > 0) {
            jdbcTemplate.update("UPDATE `User` SET signature=? WHERE user_id=?",signiture,user_id);
        }
        if (sex != null && sex.length() > 0) {
            jdbcTemplate.update("UPDATE `User` SET sex=? WHERE user_id=?",sex,user_id);
        }
        return "更新成功";
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
                "SELECT user_id, name, school, telephone, portrait_url, birth, sex, nickname, city, education, signature " +
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> item: list) {
            int tt = (int)item.get("post_time");
            item.replace("post_time", sdf.format(new Date(tt * 1000L)));
        }
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> item: list) {
            int tt = (int)item.get("post_time");
            item.replace("post_time", sdf.format(new Date(tt * 1000L)));
        }
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
            updateLike(course_id,score,user_id);
            jdbcTemplate.update("DELETE FROM Message WHERE user_id=? and course_id=?",user_id,course_id);
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
        int post_time = (int)(System.currentTimeMillis() / 1000);
        System.out.println(System.currentTimeMillis());
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
                        "ORDER BY counter DESC");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> item: list) {
            int tt = (int)item.get("post_time");
            item.replace("post_time", sdf.format(new Date(tt * 1000L)));
        }
        return list;
    }

    @RequestMapping("/User/posting")
    public List<Map<String, Object>> userPosting() {
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
                "SELECT post_id, type, title, content, counter, post_time, user_id, name, portrait_url " +
                        "FROM Posting NATURAL JOIN Post NATURAL JOIN User WHERE user_id=? " +
                        "ORDER BY counter DESC",user_id);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> item: list2) {
            int tt = (int)item.get("post_time");
            item.replace("post_time", sdf.format(new Date(tt * 1000L)));
        }
        return list2;
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
                        "WHERE relative_id = ? and course_id <> ? and `type` = ? " +
                        "ORDER BY score DESC " +
                        "LIMIT 10",
                course_id,course_id,"course"
        );
        String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id=" + course_id,String.class);
        String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id=" + course_id,String.class);
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                "SELECT course_id,name,url,cover,origin,type " +
                        "FROM Course " +
                        "WHERE titleList = ? and universityList = ? and course_id <> ? and `type` = ? " +
                        "LIMIT ?"
                        ,titleList,universityList,course_id,"course",10-list1.size()
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

    @RequestMapping("Resource/relative")
    public List<Map<String, Object>> resourceRelative(@RequestParam(value="course_id")int course_id) {
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList(
                "SELECT course_id,name,url,cover,origin,type " +
                        "FROM Mycourse " +
                        "WHERE relative_id = ? and course_id <> ? and `type` = ? " +
                        "ORDER BY score DESC ",
                course_id,course_id,"resource"
        );
        if (list1.size() < 10) {
            String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id=" + course_id,String.class);
            String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id=" + course_id,String.class);
            List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                    "SELECT course_id,name,url,cover,origin,type " +
                            "FROM Course " +
                            "WHERE titleList = ? and universityList = ? and course_id <> ? and `type` = ? " +
                            "LIMIT ?"
                    ,titleList,universityList,course_id,"resource",10-list1.size()
            );
            list1.addAll(list2);
        }
        for (Map<String, Object> object : list1) {
            String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id = " + course_id,String.class);
            String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id = " + course_id,String.class);
            object.put("titleList",titleList);
            object.put("universityList",universityList);
        }
        return list1;
    }

    @RequestMapping("Video/relative")
    public List<Map<String, Object>> videoRelative(@RequestParam(value="course_id")int course_id) {
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList(
                "SELECT course_id,name,url,cover,origin,type " +
                        "FROM Mycourse " +
                        "WHERE relative_id = ? and course_id <> ? and `type` = ? " +
                        "ORDER BY score DESC " +
                        "LIMIT 10",
                course_id,course_id,"video"
        );
        String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id=" + course_id,String.class);
        String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id=" + course_id,String.class);
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                "SELECT course_id,name,url,cover,origin,type " +
                        "FROM Course " +
                        "WHERE titleList = ? and universityList = ? and course_id <> ? and `type` = ? " +
                        "LIMIT ?"
                ,titleList,universityList,course_id,"video",10-list1.size()
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

    @RequestMapping("/Course/star")
    public String star(@RequestParam(value="course_id")int course_id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList("SELECT * FROM Star WHERE user_id=? and course_id=?",user_id,course_id);
        if (list1.size() > 0) {
            return "收藏失败";
        }
        jdbcTemplate.update("INSERT INTO Star VALUES (?, ?)", course_id, user_id);
        updateLike(course_id,5,user_id);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateRecommend(user_id);
            }
        });
        thread.start();
        return "收藏成功";
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
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList("SELECT * FROM Browse WHERE user_id=? and course_id=?",user_id,course_id);
        if (list1.size() > 0) {
            return "浏览失败";
        }
        jdbcTemplate.update("INSERT INTO Browse VALUES (?, ?, ?)", course_id, user_id,  (int)(System.currentTimeMillis() / 1000));
        jdbcTemplate.update("UPDATE Mycourse SET counter = counter + 1 " +
                "WHERE course_id=?",course_id);
        updateLike(course_id,3,user_id);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateRecommend(user_id);
            }
        });
        thread.start();
        return "浏览成功";
    }

    @RequestMapping("/User/watch")
    public String watch(@RequestParam(value="course_id")int course_id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList("SELECT * FROM Watch WHERE user_id=? and course_id=?",user_id,course_id);
        if (list1.size() > 0) {
            return "观看失败";
        }
        jdbcTemplate.update("INSERT INTO Watch VALUES (?, ?)", course_id, user_id);
        jdbcTemplate.update("UPDATE Mycourse SET counter = counter + 1 " +
                "WHERE course_id = ?",course_id);
        jdbcTemplate.update("INSERT INTO Message VALUES (?, ?)",user_id,course_id);
        updateLike(course_id,5,user_id);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateRecommend(user_id);
            }
        });
        thread.start();
        return "观看成功";
    }

    @RequestMapping("/User/recommend")
    public List<Map<String,Object>> recommend() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        List<Map<String,Object>> list2 = jdbcTemplate.queryForList("SELECT * FROM Recommend NATURAL JOIN Course WHERE user_id = ?",user_id);
        if (list2.size() == 0) {
            List<Map<String, Object>> list4 = jdbcTemplate.queryForList(
                    "SELECT course_id, name, url, cover, origin, score, counter, type " +
                            "FROM Mycourse " +
                            "ORDER BY counter DESC " +
                            "LIMIT 20");
            for (Map<String, Object> object : list4) {
                int course_id = (int)object.get("course_id");
                String titleList = jdbcTemplate.queryForObject("SELECT titleList FROM Course WHERE course_id = " + course_id,String.class);
                String universityList = jdbcTemplate.queryForObject("SELECT universityList FROM Course WHERE course_id = " + course_id,String.class);
                object.put("titleList",titleList);
                object.put("universityList",universityList);
            }
            return list4;
        } else {
            return list2;
        }
    }

    @RequestMapping("/User/interest")
    public List<Map<String,Object>> interest() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        return jdbcTemplate.queryForList("SELECT * FROM `Like` " +
                "WHERE user_id=? " +
                "ORDER BY value DESC " +
                "LIMIT 5",user_id);
    }

    @RequestMapping("/User/message")
    public List<Map<String,Object>> message() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        return jdbcTemplate.queryForList("SELECT * FROM Message NATURAL JOIN Course WHERE user_id = ?",user_id);
    }

    @RequestMapping("/User/tag")
    public String tag(@RequestParam(value="tag")String tag) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        String[] tags = tag.split("；");
        for (String item: tags) {
            List<Map<String,Object>> list2 = jdbcTemplate.queryForList("SELECT tag_id FROM Tag WHERE tag_name = ?",item);
            if (list2.size() > 0) {
                int tag_id = (int)list2.get(0).get("tag_id");
                List<Map<String,Object>> list3 = jdbcTemplate.queryForList("SELECT * FROM `Like` WHERE user_id=? and tag_id=?",user_id,tag_id);
                if (list3.size() == 0) {
                    jdbcTemplate.update("INSERT INTO `Like` VALUES (?, ?, ?)",user_id,tag_id,10);
                }
            }
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateRecommend(user_id);
            }
        });
        thread.start();
        return "更新成功";
    }

    @RequestMapping("/User/star")
    public List<Map<String,Object>> userStar() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        return jdbcTemplate.queryForList("SELECT * FROM Star NATURAL JOIN Course WHERE user_id=?",user_id);
    }

    @RequestMapping("/User/updatePassword")
    public void userUpdatePassword(@RequestParam (value="password")String password) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        String password2 = bCryptPasswordEncoder.encode(password);
        jdbcTemplate.update(
                "UPDATE User SET password = ? WHERE user_id = ? ", password2, user_id);
    }


    public void updateLike(int course_id, int weight, int user_id) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * from Has " +
                "WHERE course_id= ? ",course_id);
        for (Map<String, Object> item: list) {
            int cur_weight = weight;
            if (item.get("type").equals("content")) {
                cur_weight = (int)(cur_weight * 0.3);
            }
            List<Map<String, Object>> list1 = jdbcTemplate.queryForList("SELECT * FROM `Like` " +
                    "WHERE tag_id = ?",item.get("tag_id"));
            if (list1.size() == 0) {
                jdbcTemplate.update("INSERT INTO `Like` VALUES(?, ?, ?)",user_id,item.get("tag_id"),cur_weight);
            } else {
                jdbcTemplate.update("UPDATE `Like` set value=? where user_id=? and tag_id=?",
                        (int)list1.get(0).get("value") + cur_weight,user_id,item.get("tag_id"));
            }
            jdbcTemplate.update("UPDATE Tag SET counter = counter + 1 WHERE tag_id=?",item.get("tag_id"));
        }
    }

    public double userScore(int course_id,int user_id) {
        List<Map<String,Object>> list = jdbcTemplate.queryForList("SELECT * from Course WHERE course_id=?",course_id);
        if (list.size() == 0) return 0;
        double origin_score = Double.parseDouble(((java.math.BigDecimal)list.get(0).get("score")).toString());
        List<Map<String,Object>> user_course = jdbcTemplate.queryForList("SELECT * FROM Has NATURAL JOIN `Like` WHERE course_id = ? and user_id= ? ",course_id, user_id);
        List<Map<String,Object>> list_user = jdbcTemplate.queryForList("SELECT * FROM `Like` WHERE user_id = ?",user_id);
        List<Map<String,Object>> list_course = jdbcTemplate.queryForList("SELECT * FROM Has WHERE course_id = ?",course_id);
        double union = Math.sqrt(list_course.size());
        double intersect = 0;
        for (Map<String, Object> item : user_course) {
            intersect += Double.parseDouble(((Integer)item.get("value")).toString());
        }
        double sum = 0;
        for (Map<String, Object> item : list_user) {
            sum += Double.parseDouble(((Integer)item.get("value")).toString()) * Double.parseDouble(((Integer)item.get("value")).toString());
        }
        union = union * Math.sqrt(sum);
        return origin_score + (intersect / union * 10);
    }

    @PostMapping("/User/updatePortrait")
    public String userUpdatePortrait(@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        String origin = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + origin;
        String filePath = "/www/" + fileName; //文件路径
        String portrait_url = "http://47.100.79.77/" + fileName;
        File dest = new File(filePath);
        try {
            file.transferTo(dest);
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
            String name = userDetails.getUsername();
            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "SELECT user_id " +
                            "FROM User " +
                            "WHERE name = ?", name);
            int user_id = (int)list.get(0).get("user_id");
            jdbcTemplate.update("UPDATE User SET portrait_url = ? WHERE user_id = ? ",
                    portrait_url, user_id);
            return "上传成功";
        } catch (IOException e) {
            System.out.println(e);
        }
        return "上传失败！";
    }

    public void updateRecommend(int user_id) {
        System.out.println("update Recommend");
        List<Map<String,Object>> list = jdbcTemplate.queryForList("SELECT course_id, score FROM `Has` NATURAL JOIN `Like` NATURAL JOIN CourseScore " +
                "WHERE user_id=?",user_id);
        Set<Integer> set = new HashSet<>();
        List<Map<String,Object>> list2 = new ArrayList<>();
        for (Map<String,Object> item: list) {
            if (set.contains((Integer)item.get("course_id"))) continue;
            set.add((Integer)item.get("course_id"));
            Map<String,Object> temp = new HashMap<>();
            temp.put("course_id",(Integer)item.get("course_id"));
            temp.put("score", (java.math.BigDecimal)item.get("score"));
            list2.add(temp);
        }
        list2.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                java.math.BigDecimal s1 = (java.math.BigDecimal)o1.get("score");
                return s1.compareTo((java.math.BigDecimal)o2.get("score"));
            }
        });
        list2.subList(0,100);
        System.out.println("finish Find Courses");
        for (Map<String, Object> item: list2) {
            item.replace("score", userScore((int)item.get("course_id"),user_id));
        }
        System.out.println("finish Update Score");
        jdbcTemplate.update("DELETE FROM Recommend WHERE user_id=?",user_id);
        if (list2.size() > 20) {
            list2 = list2.subList(0,20);
        }
        for (Map<String,Object> item : list2) {
            jdbcTemplate.update("INSERT INTO Recommend VALUES (?, ?)",user_id,item.get("course_id"));
        }
        System.out.println("finish Update");
    }
}
