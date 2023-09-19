package com.site.article.html.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.site.api.controller.article.ArticleHTMLControllerApi;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Component
public class ArticleHTMLComponent {

    final static Logger logger = LoggerFactory.getLogger(ArticleHTMLComponent.class);

    @Autowired
    private GridFSBucket gridFSBucket;

    @Value("${freemarker.html.article}")
    private String articlePath;

    public Integer download(String articleId, String articleMongoId) throws Exception {

        // Storage address of static html
        String path = articlePath + File.separator + articleId + ".html";

        // Obtain file stream. Set storage address and path name
        File file = new File(path);

        // Create output stream
        OutputStream outputStream = new FileOutputStream(file);

        // Perform download
        gridFSBucket.downloadToStream(new ObjectId(articleMongoId), outputStream);

        return HttpStatus.OK.value();
    }

    public Integer delete(String articleId) throws Exception {

        // Compose the storage path of article html
        String path = articlePath + File.separator + articleId + ".html";

        // Get file stream. Set storage location and name
        File file = new File(path);

        // perform delete
        file.delete();

        return HttpStatus.OK.value();
    }
}
