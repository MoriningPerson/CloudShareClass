package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class DataController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    @RequestMapping("/Course/insertByFile")
    public void courseInsertByFile(@RequestBody String file) throws Exception {
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
                if (!url.contains("http")) url = "http:" + url;
                System.out.println("url = " + url);
                String cover = object.getString("image_url");
                if (!cover.contains("http")) cover = "http:" + cover;
                System.out.println("cover = " + cover);
                String origin = "bilibili";
                int score = 0;
                int counter = 0;
                String type = "video";
                int relative_id = object.getIntValue("relative_id");
                System.out.println("relative_id = " + relative_id);

                jdbcTemplate.update("INSERT INTO Mycourse VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        course_id, name, url, cover, origin, score, counter, type, relative_id);
                jdbcTemplate.update("INSERT INTO Course VALUES (?, ?, ?, ?, ?, ? ,? ,? ,?)",
                        course_id, name, url, cover, origin, score, "", "", "");
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
                    jdbcTemplate.update("INSERT INTO Tag ('tag_name', 'counter', 'type') VALUES (?, ?, ?)",
                            o, 0, type);
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
                jdbcTemplate.update("UPDATE Course SET titleList= ? WHERE id = ?",
                        tag_title,course_id);
                jdbcTemplate.update("UPDATE Course SET universityList= ? WHERE id = ?",
                        tag_school,course_id);
                jdbcTemplate.update("UPDATE Course SET contentList= ? WHERE id = ?",
                        tag_content,course_id);
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
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
