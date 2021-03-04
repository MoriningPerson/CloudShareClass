package com.example.demo.service;

import java.io.IOException;
import java.util.List;

import com.example.demo.entity.Course;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

public interface ILuceneService {
    public void synCourseCreateIndex() throws IOException;

    public List<Course> searchByKeyWord(String keyWord) throws IOException, ParseException, InvalidTokenOffsetsException;
}
