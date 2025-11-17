package com.cmtc.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;   // 🔹 Newly Added
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;                     
import java.util.List;                       
import java.util.concurrent.TimeUnit;       

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 🔹 Newly Added: Commonly used upload directories from properties
    @Value("${file.upload-dir}")
    private String fileUploadDir;

    @Value("${gallery.upload.dir}")
    private String galleryUploadDir;

    @Value("${rojgar.nirman.upload.dir}")
    private String rojgarNirmanUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // -----------------------------
        // 🔹 1. Common upload directories handling
        // -----------------------------
        List<String> uploadDirs = Arrays.asList(fileUploadDir, galleryUploadDir, rojgarNirmanUploadDir);

        for (String dir : uploadDirs) {
            if (dir != null && !dir.trim().isEmpty()) {
                String fileSystemPath = "file:" + dir;
                if (!fileSystemPath.endsWith("/")) {
                    fileSystemPath += "/";
                }

                // Map all upload directories to /files/**
                registry.addResourceHandler("/files/**")
                        .addResourceLocations(fileSystemPath)
                        .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
                        // 🔹 Cache uploaded content (like images, PDFs) for 30 days
            }
        }

        // -----------------------------
        // 🔹 2. React build static resource mapping
        // -----------------------------
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/static/",                 // Static content packaged in JAR
                        "file:/opt/madhyam/frontend/build/")  // React build folder (adjust if needed)
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
                // 🔹 Cache React static assets (JS, CSS, images) for 1 year
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // This pattern excludes all URLs that contain a '.' (like .js, .css, .png)
        registry.addViewController("/{path:^(?!api|auth|files|swagger-ui|v3|h2-console|actuator|assets|error|.js|.css|.*\\..*).*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:^(?!api|auth|files|swagger-ui|v3|h2-console|actuator|assets|error|.js|.css|.*\\..*).*}/**")
                .setViewName("forward:/index.html");
    }
}

