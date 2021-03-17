package com.example.demo.controller;
import com.example.demo.entity.Course;
import com.example.demo.entity.ResultBean;
import com.example.demo.service.ILuceneService;
import com.example.demo.utils.ResultUtil;
import org.apache.ibatis.ognl.ObjectElementsAccessor;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/Course/search")
public class CourseController {

    @Autowired
    private ILuceneService service;

    // 关键字搜索接口
    @PostMapping("/keyWord")
    public List<Map<String,Object>> searchByKeyWord(@RequestParam(value = "keyword")String keyWord)
            throws IOException, ParseException, InvalidTokenOffsetsException {
        List<Course> result = service.searchByKeyWord(keyWord);
        result.sort((p1, p2) -> p1.getScore() > p2.getScore() ? 1 : 0);
        List<Map<String,Object>> result2 = new ArrayList<>();
        for (Course item: result) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("course_id",item.getId());
            temp.put("name",item.getName());
            temp.put("cover",item.getCover());
            temp.put("url",item.getUrl());
            temp.put("origin",item.getOrigin());
            temp.put("score",item.getScore());
            temp.put("titleList",item.getTitleList());
            temp.put("universityList",item.getUniversityList());
            temp.put("contentList",item.getContentList());
            temp.put("type",item.getType());
            result2.add(temp);
        }
        return result2;
    }

}
