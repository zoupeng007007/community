package com.zoupeng.community.controller;


import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.zoupeng.community.annotation.LoginRequired;
import com.zoupeng.community.entity.Comment;
import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.entity.Page;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.*;
import com.zoupeng.community.util.CommunityConstant;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import sun.nio.cs.US_ASCII;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//处理用户登录后的信息，例如跳转到个人设置页面
@RequestMapping("/user")
@Controller
@Slf4j
public class UserController implements CommunityConstant {


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

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired//表示登录才能访问
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        //上传文件名称
        String fileName = CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));//成功时返回
        //生成上传的凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    //更新头像的路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils
                .isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空!");
        }
        String url = "http://" + headerBucketUrl + "/"  + fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJSONString(0);
    }

    //upload废弃
    @LoginRequired//表示登录才能访问
    //读取用户传输的图片，并写入服务器，更新用户图片的地址
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "你还没有上传图片！");
            return "/site/setting";
        }
        //得到原始的文件名
        String fileName = headerImage.getOriginalFilename();
        //从最后一个点开始截取
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
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
            throw new RuntimeException("上传文件失败，服务器异常", e);
        }
        // 更新当前用户头像的路径（web访问路径）
        // http://localhost:8080/community/ + user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }


    ///header/废弃
    //当用户刷新界面，读取图片时会触发，读取服务器的图片到web的目录下，也就是在用户数据库中的实际图片路径
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放的地址
        fileName = uploadPath + "/" + fileName;
        //文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        //响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream is = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            log.error("读取头像失败" + e.getMessage());
        }
    }

    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmError", "新密码与确认密码不匹配，请检查后重新输入！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        boolean result = userService.updatePassword(user, oldPassword, newPassword);

        if (result) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", "旧密码输入不正确！");
            return "/site/setting";
        }
    }

    //点赞
    @Autowired
    LikeService likeService;


    @Autowired
    FollowService followService;

    //主页，显示点赞相关信息
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePge(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user", user);
        //获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";

    }

    @Autowired
    private DiscussPostService discussPostService;

    //我的帖子
    @RequestMapping(path = "/profile/mypost",method = RequestMethod.GET)
    public String getMyDiscussPost(Page page,Model model){
        User user = hostHolder.getUser();
        int number = discussPostService.findDiscussPostRows(user.getId());
        page.setPath("/user/profile/mypost");
        page.setRows(number);

        //我的帖子数
        model.addAttribute("number",number);
        List<DiscussPost> list = discussPostService.findDiscussPosts(user.getId(),page.getOffset(),page.getLimit(),0);

        //帖子详情
        model.addAttribute("posts",list);

        //用户信息
        model.addAttribute("user",user);

        //赞
//        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_USER,user.getId());
//        model.addAttribute("likeCount",likeCount);
        return "/site/my-post";
    }


    @Autowired
    CommentService commentService;
    //我的回复
    @RequestMapping(path = "/profile/mycomment",method = RequestMethod.GET)
    public String getMyComment(Page page,Model model){
        User user = hostHolder.getUser();
        int number = commentService.findCommentCount(ENTITY_TYPE_USER,user.getId());
        page.setPath("/user/profile/mycomment");
        page.setRows(number);


        //我的评论
        model.addAttribute("number",number);
        List<Comment> list = commentService.findCommentsByEntity(ENTITY_TYPE_USER,user.getId(),page.getOffset(),page.getLimit());

        //评论/回复
        model.addAttribute("comments",list);

        //用户信息
        model.addAttribute("user",user);

        //赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_USER,user.getId());
        model.addAttribute("likeCount",likeCount);

        model.addAttribute("msg","睡醒了开发");
        return "/site/my-reply";
    }
}
