package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
public class DataController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    static int tag_id = 0;


    @RequestMapping("/Course/insertByFile")
    public void courseInsertByFile(@RequestBody String file) throws Exception {
        HashSet<String> hs = new HashSet<String>();

        String[] obj = file.split("\n");
        System.out.println(obj.length);
        for (String o : obj) {
            try {
                System.out.println("o = " + o);
                JSONObject object = JSONObject.parseObject(o);
                int course_id = object.getIntValue("id");
                System.out.println("course_id = " + course_id);
                String name = object.getString("title");
                System.out.println("name = " + name);
                String url = object.getString("class_url");
                if (!url.contains("http")) url = "https:" + url;
                System.out.println("url = " + url);
                String cover = object.getString("image_url");
                if (!cover.contains("http")) cover = "http:" + cover;
                System.out.println("cover = " + cover);
                String origin;
                if (url.contains("bilibili"))
                    origin = "bilibili";
                else
                    origin = "MOOC";
                int score = 0;
                int counter = 0;
                String type = "course";
                int relative_id = object.getIntValue("relative_id");
                System.out.println("relative_id = " + relative_id);
                if (hs.contains(url)) {
                    continue;
                }
                hs.add(url);
                jdbcTemplate.update("INSERT INTO Mycourse VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        course_id, name, url, cover, origin, score, counter, type, relative_id);
                if (relative_id == course_id) {
                    jdbcTemplate.update("INSERT INTO Course VALUES (?, ?, ?, ?, ?, ? ,? ,? ,?, ?)",
                            course_id, name, url, cover, origin, score, type, "", "", "");
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


    @RequestMapping("/Tag/insertByFile")
    public void tagInsertByFile(@RequestBody String file) throws Exception {
        String[] obj = file.split("\n");
        System.out.println(obj.length);
        String type = obj[0];
        boolean first = true;
        for (String o : obj) {
            if (first)
                first = false;
            else
            {
                try {
                    jdbcTemplate.update("INSERT INTO Tag VALUES (?, ?, ?, ?)",
                            tag_id, o, 0, type);
                    tag_id += 1;
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }


    @RequestMapping("/Course/insertTagByFile")
    public void courseTagByFile(@RequestBody String file) throws Exception {
        String[] obj = file.split("\n");
        System.out.println(obj.length);
        for (String o : obj) {
            try
            {
                System.out.println("o = " + o);
                JSONObject object = JSONObject.parseObject(o);
                int course_id = object.getIntValue("id");
                String tag_school = object.getString("tag_school");
                String tag_content = object.getString("tag_content");
                String tag_title = object.getString("tag_title");
                List<Map<String, Object>> list = jdbcTemplate.queryForList(
                        "SELECT * FROM Course WHERE course_id=?", course_id);
                if (list.size() > 0) {
                    jdbcTemplate.update("UPDATE Course SET titleList= ? WHERE id = ?",
                            tag_title,course_id);
                    jdbcTemplate.update("UPDATE Course SET universityList= ? WHERE id = ?",
                            tag_school,course_id);
                    jdbcTemplate.update("UPDATE Course SET contentList= ? WHERE id = ?",
                            tag_content,course_id);
                }
                list = jdbcTemplate.queryForList(
                        "SELECT * FROM Mycourse WHERE course_id=?", course_id);
                if (list.size() > 0) {
                    for (String title: tag_title.split("；")) {
                        String sql = "SELECT tag_id FROM Tag WHERE tag_name = " + title;
                        Integer tag_id = jdbcTemplate.queryForObject(sql, Integer.class);
                        jdbcTemplate.update("INSERT INTO Has VALUES (?, ?, ?)",
                                course_id,tag_id,"title");
                    }
                    for (String content: tag_content.split("；")) {
                        String sql = "SELECT tag_id FROM Tag WHERE tag_name = " + content;
                        Integer tag_id = jdbcTemplate.queryForObject(sql, Integer.class);
                        jdbcTemplate.update("INSERT INTO Has VALUES (?, ?, ?)",
                                course_id,tag_id,"content");
                    }
                    for (String school: tag_school.split("；")) {
                        String sql = "SELECT tag_id FROM Tag WHERE tag_name = " + school;
                        Integer tag_id = jdbcTemplate.queryForObject(sql, Integer.class);
                        jdbcTemplate.update("INSERT INTO Has VALUES (?, ?, ?)",
                                course_id,tag_id,"university");
                    }
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
