package com.site.files.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploaderService {

    /*
    * Upload file with fastDFS
    * */
    public String uploadFdfs(MultipartFile file, String fileExtName) throws Exception;


}


