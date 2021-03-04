package com.example.demo.controller;
//import com.example.demo.entity.Course;
import com.example.demo.entity.Course;
import com.example.demo.entity.ResultBean;
import com.example.demo.service.ILuceneService;
import com.example.demo.utils.ResultUtil;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/course/search")
public class CourseController {

    @Autowired
    private ILuceneService service;

    // 需要验证身份
    @GetMapping("/helloProject")
    public String helloProject() {
        return "Hello Project!";
    }

    // 关键字搜索接口
    @PostMapping("/keyWord")
    public ResultBean<List<Course>> searchByKeyWord(@RequestBody String keyWord) throws IOException, ParseException, InvalidTokenOffsetsException {
        List<Course> result = service.searchByKeyWord(keyWord);
        return ResultUtil.success(result);
    }

}
