package com.TJS.reggie.Controller;

import com.TJS.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MultipartFilter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String Basepath;
    /**
     * 文件上传
     * 在拦截器的放行目录中放行
     * @param file  必须和前端form表单的name属性一致
     * @return
     */
    @PostMapping("upload")
    public R<String> upload(MultipartFile file){
        //获取文件名后缀
        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        //拼接目录
        filename = UUID.randomUUID().toString() +suffix;
        String path = Basepath+ filename;
        File file1 = new File(Basepath);
        //如果目录不存在就创建目录
        if(!file1.exists())file1.mkdirs();//mkdir();

        //转存到该目录
        try {
            file.transferTo(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);
    }
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream stream = new FileInputStream(new File(Basepath+name));
            response.setContentType("image/jepg");

            //输出流，通过输出流将文件写回浏览器，在浏览器显示图片
            ServletOutputStream outputStream = response.getOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = stream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
            }
            //关闭资源
            stream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
