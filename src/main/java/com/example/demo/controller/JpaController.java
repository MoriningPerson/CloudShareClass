package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class JpaController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    static int user_id = 10;
    static int post_id = 10;

    @RequestMapping("/hello")
    public String hello() {
        System.out.println(System.currentTimeMillis() / 1000);
//        System.out.println(Calendar.getInstance().getTimeInMillis());
//        System.out.println(new Date().getTime());
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        System.out.println(userDetails.getUsername());  //  获取用户名，加上了这个的话hello也需要验证身份
//
//        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        System.out.println(bCryptPasswordEncoder.encode("123456"));
//        System.out.println(bCryptPasswordEncoder.encode("222222"));
//        System.out.println(bCryptPasswordEncoder.encode("333333"));
        return "<div>Hello World!</div><div>This is jpa on project!</div>";
    }

    @RequestMapping("/shouldAuthority")  // 需要身份验证的hello界面
    public String shouldAuthority() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        System.out.println(userDetails.getUsername());
        return "<div>Hello World!</div><div>This is jpa on project!</div>";
    }

//    @RequestMapping("/Course/searchByCourseId")
//    public List<Map<String, Object>> courseSearchByCourseId(@RequestBody String jsonObject) {
//        JSONObject object = JSONObject.parseObject(jsonObject);
//        int course_id = object.getIntValue("course_id");
//        List<Map<String, Object>> list = jdbcTemplate.queryForList(
//                "SELECT * FROM Mycourse WHERE course_id = ?", course_id);
//        return list;
//    }
//
    @RequestMapping("/Course/insertByFile")
    public void courseInsertByFile(@RequestBody String file) throws Exception {
//        System.out.println(file);
        String[] obj = file.split("\n");
        System.out.println(obj.length);
        for (String o : obj) {
            System.out.println("o = " + o);
            JSONObject object = JSONObject.parseObject(o);
            int course_id = object.getIntValue("id");
            System.out.println("course_id = " + course_id);
            String name = object.getString("title");
            System.out.println("name = " + name);
            String url = object.getString("class_url");
            System.out.println("url = " + url);
            String cover = object.getString("image_url");
            System.out.println("cover = " + cover);
            String origin = "bilibili";
            int score = 0;
            int counter = 0;
            String type = "video";
            int relative_id = object.getIntValue("relative_id");
            System.out.println("relative_id = " + relative_id);

            jdbcTemplate.update("INSERT INTO Mycourse VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    course_id, name, url, cover, origin, score, counter, type, relative_id);
        }
    }

    @RequestMapping("/User/insert")
    public String userInsert(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        user_id++;
        String name = object.getString("name");
        String school = object.getString("school");
        String telephone = object.getString("telephone");
        String password = bCryptPasswordEncoder.encode(object.getString("password"));
        String portrait_url = "http://47.100.79.77/img/portrait.f98bd381.svg";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT * FROM User WHERE name = ?", name);
        if (list.size() == 1) {
            return "注册失败";
        }
        else {
            jdbcTemplate.update("INSERT INTO User VALUES (?, ?, ?, ?, ?, ?)",
                    user_id, name, school, telephone, password, portrait_url);
            return "注册成功";
        }
    }

    @RequestMapping("/User/login")
    public String userLogin(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        String name = object.getString("name");
        String password = bCryptPasswordEncoder.encode(object.getString("password"));
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT * FROM User WHERE name = ? and password = ?",
                name, password);
        if (list.size() == 1)
            return "登录成功";
        else
            return "登录失败";
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
                "SELECT course_id, name, url, cover, origin, score, counter, type, universityList, titleList, contentList " +
                        "FROM Course, Mycourse " +
                        "WHERE Course.name = Mycourse.name AND Course.url = Mycourse.url " +
                        "ORDER BY counter DESC " +
                        "LIMIT 3");
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
                        "FROM Browse, Mycourse, User " +
                        "WHERE Browse.course_id = Mycourse.course_id AND Browse.user_id = User.user_id AND User.name = ? " +
                        "ORDER BY Browse.browse_time DESC " +
                        "LIMIT 5", name);
        int list1len = list1.size();
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(
                "SELECT course_id, Course.name, url, cover, origin, score, counter, type " +
                        "FROM Course, Mycourse " +
                        "WHERE Course.name = Mycourse.name AND Course.url = Mycourse.url " +
                        "ORDER BY counter DESC " +
                        "LIMIT ?", 5 - list1len);
        list1.addAll(list2);
        return list1;
    }

    @RequestMapping("/Posting/searchByCourseId")
    public List<Map<String, Object>> postingSearchByCourseId(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        int course_id = object.getIntValue("course_id");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT post_id, type, title, content, counter, post_time, user_id, name, portrait_url " +
                        "FROM Posting NATURAL JOIN Post NATURAL JOIN User NATURAL JOIN Relative " +
                        "WHERE course_id = ?", course_id);
        return list;
    }

    @RequestMapping("/Rate/insert")
    public String rateInsert(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        int course_id = object.getIntValue("course_id");
        int score = object.getIntValue("score");
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
//            List<Map<String, Object>> list3 = jdbcTemplate.queryForList(
//                    "SELECT AVG(score) as avg FROM Rate WHERE course_id = ? ", course_id);
//            double avgscore = (double)(list3.get(0).get("avg"));
            jdbcTemplate.update("UPDATE Mycourse " +
                            "SET score = (SELECT AVG(score) as avg FROM Rate WHERE course_id = ?) " +
                            "WHERE course_id = ? ",
                    course_id, course_id);
            return "评分成功";
        }
    }

    @RequestMapping("/Star/insert")
    public void starInsert(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        int course_id = object.getIntValue("course_id");
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT user_id " +
                        "FROM User " +
                        "WHERE name = ?", name);
        int user_id = (int)list.get(0).get("user_id");
        jdbcTemplate.update("INSERT INTO Star VALUES (?, ?)", course_id, user_id);
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
                        "WHERE Temp.course_id = Course.id AND Course.id = Mycourse.course_id ", user_id);
        return list2;
    }

    @RequestMapping("/Post/insert")
    public String postInsert(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        post_id++;
        int post_time = (int)System.currentTimeMillis() / 1000;
        String type = object.getString("type");
        String title = object.getString("title");
        String content = object.getString("content");
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String name = userDetails.getUsername();
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

    @RequestMapping("/Posting/like")
    public void postingLike(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        int post_id = object.getIntValue("post_id");
        jdbcTemplate.update("UPDATE Posting " +
                "SET counter = counter + 1 " +
                "WHERE post_id = ? ", post_id);
    }

    @RequestMapping("/Posting/comment")
    public void postingComment(@RequestBody String jsonObject) {
        JSONObject object = JSONObject.parseObject(jsonObject);
        int post_id = object.getIntValue("post_id");
        String content = object.getString("content");
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
    }

    @RequestMapping("/Posting/search")
    public List<Map<String, Object>> postingSearch() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT post_id, type, title, content, counter, post_time, user_id, name, portrait_url " +
                        "FROM Posting NATURAL JOIN Post NATURAL JOIN User " +
                        "ORDER BY counter");
        return list;
    }


//    @RequestMapping("/User/searchByUserId")
//    public List<Map<String, Object>> userSearchByUserId(@RequestBody String jsonObject) {
//        JSONObject object = JSONObject.parseObject(jsonObject);
//        int user_id = object.getIntValue("user_id");
//        List<Map<String, Object>> list = jdbcTemplate.queryForList(
//                "SELECT * FROM User WHERE user_id = ?", user_id);
//        return list;
//    }
//
//    @RequestMapping("/Posting/insert")
//    public void postingInsert(int post_id, String type, String title,
//                              String content, int counter, String post_time) {
//        jdbcTemplate.update("INSERT INTO Posting VALUES (?, ?, ?, ?, ?, ?)",
//                post_id, type, title, content, counter, post_time);
//    }
}
