package com.site.search.controller;

import com.site.grace.result.GraceJSONResult;
import com.site.search.pojo.Stu;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class HelloController {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @RequestMapping("hello")
    public Object hello() {
        return GraceJSONResult.ok();
    }
    @GetMapping("createIndex")
    public Object createIndex() {
        esTemplate.createIndex(Stu.class);
        return GraceJSONResult.ok();
    }
    @GetMapping("deleteIndex")
    public Object deleteIndex() {
        esTemplate.deleteIndex(Stu.class);
        return GraceJSONResult.ok();
    }
    @GetMapping("addDoc")
    public Object addDoc() {
        Stu stu = new Stu();
        stu.setStuId(1001l);
        stu.setName("Hannah");
        stu.setAge(21);
        stu.setDesc("学生");
        stu.setMoney(100.2f);

        IndexQuery query = new IndexQueryBuilder().withObject(stu).build();

        esTemplate.index(query);
        return GraceJSONResult.ok();
    }

    @GetMapping("updateDoc")
    public Object updateDoc() {

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("desc", "Hello world");
        updateMap.put("age", 22);


        IndexRequest ir = new IndexRequest();
        ir.source(updateMap);

        UpdateQuery uq = new UpdateQueryBuilder()
                .withClass(Stu.class)
                .withId("1001") // id of the file that will be update
                .withIndexRequest(ir)
                .build();

        esTemplate.update(uq);
        return GraceJSONResult.ok();
    }

    @GetMapping("getDoc")
    public Object getDoc(String id) {

        GetQuery getQuery = new GetQuery();
        getQuery.setId(id);

        Stu stu = esTemplate.queryForObject(getQuery, Stu.class);
        return GraceJSONResult.ok(stu);
    }

    @GetMapping("deleteDoc")
    public Object deleteDoc(String id) {

        esTemplate.delete(Stu.class, id);
        return GraceJSONResult.ok();
    }
}
