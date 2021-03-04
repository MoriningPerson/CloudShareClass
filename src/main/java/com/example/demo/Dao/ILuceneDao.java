package com.example.demo.Dao;

import com.example.demo.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import java.io.IOException;
import java.util.List;

@Mapper
public interface ILuceneDao {
    /*创建索引*/
    public void createCourseIndex(List<Course> courseList) throws IOException;
    /*关键字搜索*/
    public List<Course> searchByKeyWord(String keyWord) throws IOException, ParseException, InvalidTokenOffsetsException;
    /*给新数据添加索引*/
    public void addCourseIndex(Course course) throws IOException;
    /*其他 API 另加，暂时就写了这仨*/
}
