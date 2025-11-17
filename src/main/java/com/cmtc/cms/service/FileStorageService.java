package com.cmtc.cms.service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;


public interface FileStorageService {
	 String saveFile(MultipartFile file);
	    void deleteFile(String filePath);
	    Resource loadAsResource(String filename);
	    String saveFile(MultipartFile file, String customFileName) ;
	    String saveFile(MultipartFile file, String customFileName, String subDir,String url) ;



}
