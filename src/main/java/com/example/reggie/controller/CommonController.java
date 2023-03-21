package com.example.reggie.controller;

import com.example.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@RestController
@RequestMapping("/common")
@ResponseBody
@Slf4j
public class CommonController {
    @Value("${images.basePath}")
    String basePath;

    /**
     * 接收上传文件并进行转储，返还文件相对路径
     * @param file
     * @return 存储文件相对路径
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // 1.为文件起自定义别名（要求全局唯一）
        String filename = generateNameForFile(file.getOriginalFilename());

        // 2.若基本路径文件夹不存在，则创建文件夹
        Path path = Paths.get(basePath);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error(e.getMessage());
                return R.error("创建文件夹失败");
            }
        }

        // 3.将基本路径与文件名结合，进行转储
        Path filepath = path.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filepath);
        } catch (IOException e) {
            log.error(e.getMessage());
            return R.error("转储文件失败");
        }

        // 4.关闭文件输入流
        return R.success(filename);
    }

    @GetMapping("/download")
    public void download(@RequestParam String name, ServletResponse response) {
        // 1.根据文件名获取文件绝对路径
        Path path = Paths.get(basePath).resolve(Paths.get(name));

        // 2.获取文件的输入流与response的输出流
        try (InputStream input = new FileInputStream(path.toString())) {
            // 3.不断拷贝文件字节流
            int len = 1024;
            byte[] bytes = new byte[len];
            while ((len = input.read(bytes)) > 0) {
                response.getOutputStream().write(bytes);
            }
        } catch (IOException e) {
            log.error("文件字节流拷贝失败");
        }
    }

    private String generateNameForFile(String filename) {
        // 1.取出文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));

        // 2.生成UUID
        String id = UUID.randomUUID().toString();

        // 3.结合UUID与文件后缀得到文件名
        String name = id + suffix;
        return name;
    }

}
