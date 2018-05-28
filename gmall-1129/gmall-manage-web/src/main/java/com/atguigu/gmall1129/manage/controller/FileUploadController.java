package com.atguigu.gmall1129.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.midi.Track;
import java.io.IOException;

/**
 * @param
 * @return
 */

@Controller
public class FileUploadController {



    @PostMapping("/fileUpload")
    @ResponseBody
    public String upload(MultipartFile file) throws IOException, MyException {
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(configFile);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer, null);

        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        String extFilename = StringUtils.substringAfter(originalFilename, ".");


        String[] upload_file = storageClient.upload_file(bytes, extFilename, null);
        String imgUrl="http://file.gmall.com";
        for (int i = 0; i < upload_file.length; i++) {
            String uploadFilePath = upload_file[i];
            imgUrl+="/"+uploadFilePath;
        }
        System.out.println("imgUrl = " + imgUrl);
        return imgUrl;
    }

    @Test
    public void uploadTest () throws IOException, MyException {
        String file = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(file);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer, null);


        String[] upload_file = storageClient.upload_file("c://victor2.jpg", "jpg", null);

        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            System.out.println("s = " + s);
        }

    }

}
