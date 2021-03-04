package com.example.demo.service.Impl;

import com.example.demo.Dao.ILuceneDao;
import com.example.demo.entity.Course;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.service.ILuceneService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class LuceneServiceImpl implements ILuceneService{

    @Autowired
    private ILuceneDao luceneDao;

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public void synCourseCreateIndex() throws IOException {
        List<Course> allCourse = courseMapper.getAllCourse();
        luceneDao.createCourseIndex(allCourse);
    }

    @Override
    public List<Course> searchByKeyWord(String keyWord) throws IOException, ParseException, InvalidTokenOffsetsException {
        return luceneDao.searchByKeyWord(keyWord);
    }
}
