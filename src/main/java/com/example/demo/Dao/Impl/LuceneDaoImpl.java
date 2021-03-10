package com.example.demo.Dao.Impl;
import com.example.demo.entity.Course;
import com.example.demo.lucene.MyIKAnalyzer;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.Dao.ILuceneDao;
import org.apache.lucene.document.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.highlight.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository(value = "luceneDao")
public class LuceneDaoImpl implements ILuceneDao{

    @Autowired(required = false)
    private IndexWriter indexWriter;

    @Autowired
    private Analyzer analyzer;

    @Autowired
    private SearcherManager searcherManager;

    @Autowired
    private CourseMapper courseMapper;
    /*
    * 创建索引，有三个字段，id，name，tags
    * */
    @Override
    public void createCourseIndex(List<Course> courseList) throws IOException {
        List<Document> docs = new ArrayList<Document>();
        for(Course c : courseList) {
            Document document = new Document();
            document.add(new Field("name", c.getName() + "", TextField.TYPE_STORED));
            String contentTags = c.getContentList();
            String titleTags = c.getTitleList();
            String universityTags = c.getUniversityList();
            document.add(new StringField("id", c.getId() + "", Field.Store.YES));
            document.add(new Field("name", c.getName() + "", TextField.TYPE_STORED));
            document.add(new Field("contentTags", contentTags + "", TextField.TYPE_STORED));
            document.add(new Field("titleTags", titleTags + "", TextField.TYPE_STORED));
            document.add(new Field("universityTags", universityTags + "", TextField.TYPE_STORED));
            docs.add(document);
        }
        indexWriter.addDocuments(docs);
        indexWriter.commit();
    }

    /*
    * 通过关键字搜索
    * */
    @Override
    public List<Course> searchByKeyWord(String keyWord) throws IOException, ParseException, InvalidTokenOffsetsException {
        searcherManager.maybeRefresh();
        List<Course> list = new ArrayList<>();
        IndexSearcher indexSearcher = searcherManager.acquire();
        Analyzer analyzer = new IKAnalyzer();
        // 两个搜索域，name 和 tags
        String[] fields = {"name", "contentTags", "titleTags", "universityTags"};
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line = keyWord != null ? keyWord : in.readLine();
        Query query = parser.parse(line);

        TopDocs topDocs = indexSearcher.search(query, 50);

        // 高亮处理
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
        Fragmenter fragmenter = new SimpleFragmenter(100);
        highlighter.setTextFragmenter(fragmenter);


        for(ScoreDoc scoreDoc : topDocs.scoreDocs) {
            int doc = scoreDoc.doc;
            Document document = indexSearcher.doc(doc);
            Course course = new Course();
            // 这里需要一个通过 id 拿到数据库中 Course 的接口
            course = courseMapper.getCourseById(document.get("id"));
            String name = highlighter.getBestFragment(new MyIKAnalyzer(), "name", document.get("name"));
            if(name == null) {
                name = course.getName();
            }
            String contentTags = highlighter.getBestFragment(new MyIKAnalyzer(), "contentTags", document.get("contentTags"));
            if(contentTags == null) {
                contentTags = course.getContentList();
            }
            String titleTags = highlighter.getBestFragment(new MyIKAnalyzer(), "titleTags", document.get("titleTags"));
            if(titleTags == null) {
                titleTags = course.getTitleList();
            }
            String universityTags = highlighter.getBestFragment(new MyIKAnalyzer(), "universityTags", document.get("universityTags"));
            if(universityTags == null) {
                universityTags = course.getUniversityList();
            }
//            System.out.println("content " + contentTags);
            course.setName(name);
            course.setContentList(contentTags);
            course.setTitleList(titleTags);
            course.setUniversityList(universityTags);
            list.add(course);
        }
        return list;
    }

    /*给新数据添加索引*/
    @Override
    public void addCourseIndex(Course course) throws IOException {
        Document document = new Document();
        document.add(new StringField("id", course.getId() + "", Field.Store.YES));
        document.add(new Field("name", course.getName() + "", TextField.TYPE_STORED));
        document.add(new Field("contentTags", course.getContentList() + "", TextField.TYPE_STORED));
        document.add(new Field("titleTags", course.getTitleList() + "", TextField.TYPE_STORED));
        document.add(new Field("universityTags", course.getUniversityList() + "", TextField.TYPE_STORED));
        indexWriter.addDocument(document);
        indexWriter.commit();
    }

}
