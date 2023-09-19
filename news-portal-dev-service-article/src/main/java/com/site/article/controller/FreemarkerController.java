package com.site.article.controller;

import com.site.pojo.Article;
import com.site.pojo.Spouse;
import com.site.pojo.Stu;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.Temperature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

@Controller
@RequestMapping("free")
public class FreemarkerController {

    @Value("${freemarker.html.target}")
    private String htmlTarget;

    @GetMapping("/createHTML")
    @ResponseBody
    public String createHTML(Model model) throws Exception {

        // 0. Configure freemarker environment
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        // Set freemarker file path
        String classPath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classPath.replace("%20", " ") + "templates"));


//        System.out.println(htmlTarget);
//        System.out.println(classPath + "templates");
        // 1. Get ftl template file
        Template template = cfg.getTemplate("stu.ftl", "utf-8");

        // 2. Get dynamic data
        String stranger = "Ty";
        model.addAttribute("there", stranger);
        model = makeModel(model);

        // 3. Merge dynamic data ans ftl template to generate static html file
        File tempDic = new File(htmlTarget);
        if (!tempDic.exists()) {
            tempDic.mkdirs();
        }

        Writer out = new FileWriter(htmlTarget + File.separator + "10010" + ".html");
        template.process(model, out);
        out.close();

        return "ok";
    }

    @GetMapping("/hello")
    public String hello(Model model) {

        // Define the contents that insert to the template.
        // Output string
        String stranger = "Ty";
        model.addAttribute("there", stranger);

        makeModel(model);
        // "stu" here is the address of freemarker template classpath:/templates/
        // match *.ftl
        return "stu";
    }

    private Model makeModel(Model model) {
        Stu stu = new Stu();
        stu.setUid("10010");
        stu.setUsername("inews");
        stu.setAmount(23.54f);
        stu.setAge(23);
        stu.setHaveChild(true);
        stu.setBirthday(new Date());

        Spouse spouse = new Spouse();
        spouse.setUsername("Bob");
        spouse.setAge(18);

        stu.setSpouse(spouse);
        stu.setArticleList(getArticles());
        stu.setParents(getParents());

        model.addAttribute("stu", stu);
        return model;
    }

    private List<Article> getArticles() {

        Article article1 = new Article();
        article1.setId("1001");
        article1.setTitle("Nice Weather!");

        Article article2 = new Article();
        article2.setId("1002");
        article2.setTitle("Nice Food!");

        Article article3 = new Article();
        article3.setId("1003");
        article3.setTitle("Nice Job!");

        List<Article> list = new ArrayList<>();

        list.add(article1);
        list.add(article2);
        list.add(article3);

        return list;

    }

    private Map<String, String> getParents () {
        Map<String, String> parents= new HashMap<>();
        parents.put("father", "Nibaba");
        parents.put("Mother", "NLM");
        return parents;
    }
}
