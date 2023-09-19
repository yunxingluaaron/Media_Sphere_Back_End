package com.site.api.controller.files;

import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "Controller for file upload", tags = {"Controller for file upload"})
@RequestMapping("fs")
public interface FileUploaderControllerApi {
    /**
     * API for uploading user profile (single file upload)
     */
    @PostMapping("/uploadFace") // route of the method
    public GraceJSONResult uploadFace(@RequestParam String userId, MultipartFile file) throws Exception;

    /**
     * Upload multiple files
     * @param userId
     * @param files
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "API for uploading user profile", notes = "API for uploading user profile", httpMethod = "POST")
    @PostMapping("/uploadSomeFiles") // route of the method
    public GraceJSONResult uploadSomeFiles(@RequestParam String userId, MultipartFile[] files) throws Exception;

    /**
     * Upload to the gridFs in MongoDB
     * @param newAdminBO
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadToGridFS") // route of the method
    public GraceJSONResult uploadToGridFS(@RequestBody NewAdminBO newAdminBO) throws Exception;

    /**
     * Obtain faceId from gridFS
     * @param faceId
     * @return
     * @throws Exception
     */
    @GetMapping("/readInGridFS") // route of the method
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * Obtain faceId from gridFS and return BASE64
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/readFace64InGridFS") // route of the method
    public GraceJSONResult readFace64InGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
