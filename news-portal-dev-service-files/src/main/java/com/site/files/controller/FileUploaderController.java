package com.site.files.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.site.api.controller.files.FileUploaderControllerApi;
import com.site.exception.GraceException;
import com.site.files.resource.FileResource;
import com.site.files.service.UploaderService;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.bo.NewAdminBO;
import com.site.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.site.utils.FileUtils.fileToBase64;

@RestController
public class FileUploaderController implements FileUploaderControllerApi {

    final static Logger logger = LoggerFactory.getLogger(FileUploaderController.class);

    @Autowired
    private UploaderService uploaderService;

    @Autowired
    private FileResource fileResource;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    public GraceJSONResult uploadFace(String userId, MultipartFile file) throws Exception {

        String path = "";
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        if (file != null) {
            // Obtain file name
            String fileName = file.getOriginalFilename();

            // Check file name is empty
            if (StringUtils.isNotBlank(fileName)) {
                // Get file suffix
                String fileNameArr[] = fileName.split("\\.");
                String suffix = fileNameArr[fileNameArr.length - 1];
                // Filter out illegal suffix
                if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg") ) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                }
                // Perform file upload
                path = uploaderService.uploadFdfs(file, suffix);


            } else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
            }

        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        logger.info("path = " + path);

        String finalPath = null;
        if (StringUtils.isNotBlank(path)) {
            finalPath = fileResource.getHost() + path;
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        return GraceJSONResult.ok(finalPath);
    }

    @Override
    public GraceJSONResult uploadSomeFiles(String userId, MultipartFile[] files) throws Exception {

        // Create a list for storing the url address of multiple files and return to front end
        List<String> imageUrlList = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String path = "";
                if (StringUtils.isBlank(userId)) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
                }

                if (file != null) {
                    // Obtain file name
                    String fileName = file.getOriginalFilename();

                    // Check file name is empty
                    if (StringUtils.isNotBlank(fileName)) {
                        // Get file suffix
                        String fileNameArr[] = fileName.split("\\.");
                        String suffix = fileNameArr[fileNameArr.length - 1];
                        // Filter out illegal suffix
                        if (!suffix.equalsIgnoreCase("png") &&
                                !suffix.equalsIgnoreCase("jpg") &&
                                !suffix.equalsIgnoreCase("jpeg") ) {
                            continue;
                        }
                        // Perform file upload
                        path = uploaderService.uploadFdfs(file, suffix);


                    } else {
                        continue;
                    }
                } else {
                    continue;
                }

                logger.info("path = " + path);

                String finalPath = "";
                if (StringUtils.isNotBlank(path)) {
                    finalPath = fileResource.getHost() + path;
                    // FIXME:Before add the final path to imageUrlList, we need to check the image content
                    imageUrlList.add(finalPath);
                } else {
                    continue;
                }
            }
        }
        return GraceJSONResult.ok(imageUrlList);
    }

    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception {

        // Obtain the base64 string of image
        String file64 = newAdminBO.getImg64();

        // Convert bse64 string to byte array
        byte[] bytes = new BASE64Decoder().decodeBuffer(file64.trim());

        // Convert to input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        // Upload to gridFS
        ObjectId fileId = gridFSBucket.uploadFromStream(newAdminBO.getUsername() + ".png", inputStream);

        // Obtain the file' key in gridFS
        String fileIdStr = fileId.toString();

        return GraceJSONResult.ok(fileIdStr);
    }

    @Override
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 0. check parameter
        if (StringUtils.isBlank(faceId) || faceId.equalsIgnoreCase("null")) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        // 1. Obtain file from gridFS
        File adminFace = readGridFSByFaceId(faceId);

        // 2. Output faceId to front end
        FileUtils.downloadFileByStream(response, adminFace);

    }

    private File readGridFSByFaceId(String faceId) throws Exception{

        GridFSFindIterable gridFSFiles = gridFSBucket.find(Filters.eq("_id", new ObjectId(faceId)));
        GridFSFile gridFS = gridFSFiles.first();
        if (gridFS == null) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }


        String fileName = gridFS.getFilename();
        System.out.println(fileName);

        // Obtain file stream, save file to temporary directory in server
        File fileTemp = new File("/workspace/temp_face");
        if (!fileTemp.exists()) {
            fileTemp.mkdirs();
        }

        File myFile = new File("/workspace/temp_face/" + fileName);

        // Creat file output stream
        OutputStream os = new FileOutputStream(myFile);

        // download to server
        gridFSBucket.downloadToStream(new ObjectId(faceId), os);

        return myFile;
    }

    @Override
    public GraceJSONResult readFace64InGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // faceId empty check is performed in AdminMngController

        // 0. Obtain the stored face image from gridFS
        File myFace = readGridFSByFaceId(faceId);

        // 1. convert the image to BASE64
        String base64Face = FileUtils.fileToBase64(myFace);

        return GraceJSONResult.ok(base64Face);
    }


}
