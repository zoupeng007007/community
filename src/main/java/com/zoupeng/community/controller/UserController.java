package com.zoupeng.community.controller;


import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CommunityUtil;
import com.zoupeng.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

//处理用户登录后的信息，例如跳转到个人设置页面
@RequestMapping("/user")
@Controller
@Slf4j
public class UserController {

    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){

        return "/site/setting";
    }
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //读取用户传输的图片，并写入服务器，更新用户图片的地址
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","你还没有上传图片！");
            return "/site/setting";
        }
        //得到原始的文件名
        String fileName = headerImage.getOriginalFilename();
        //从最后一个点开始截取
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确！");
            return "/site/setting";
        }
        //生成随机的文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常",e);
        }
        // 更新当前用户头像的路径（web访问路径）
        // http://localhost:8080/community/ + user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain  + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    //当用户刷新界面，读取图片时会触发，读取服务器的图片到web的目录下，也就是在用户数据库中的实际图片路径
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName,HttpServletResponse response){
        //服务器存放的地址
        fileName = uploadPath + "/" + fileName;
        //文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        //响应图片
        response.setContentType("image/" + suffix);
        try(
                FileInputStream is = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1){
                os.write(buffer,0,len);
            }
        } catch (IOException e) {
            log.error("读取头像失败"  +e.getMessage());
        }
    }

    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPassword,String newPassword,String confirmPassword){
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmError","新密码与确认密码不匹配，请检查后重新输入！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        boolean result = userService.updatePassword(user, oldPassword, newPassword);

        if (result){
            return "redirect:/logout";
        }else {
            model.addAttribute("oldPasswordMsg","旧密码输入不正确！");
            return "/site/setting";
        }
    }

}
