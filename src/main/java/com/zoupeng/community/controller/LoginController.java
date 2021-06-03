package com.zoupeng.community.controller;

import com.google.code.kaptcha.Producer;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.CommunityUtil;
import com.zoupeng.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*返回一个注册页面*/
@Slf4j
@Controller
public class LoginController implements CommunityConstant {

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @Autowired
    UserService userService;

    /*获取登录页面*/
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    //SpringMVC 自动将匹配属性注入user,
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，已经向您的邮箱发送了一封邮件，请尽快激活");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {

        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEATED) {
            model.addAttribute("msg", "无效操作，该账号已经激活过");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您的激活码是乱编的");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }

    @Autowired
    Producer kapchaProducer;
    @Autowired
    RedisTemplate redisTemplate;
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {
        //生成验证码
        String text = kapchaProducer.createText();
        BufferedImage image = kapchaProducer.createImage(text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        //创建一个Cookie
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);//60s过期
        cookie.setPath(contextPath);//都有效
        response.addCookie(cookie);
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        //将验证码存入Redis，设置为60失效
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            log.error("响应验证码失败" + e.getMessage());
        }
    }

    //    //生成验证码
//    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
//    public void getKaptcha(HttpServletResponse response, HttpSession session){
//        //生成验证码
//        String text = kapchaProducer.createText();
//        BufferedImage image = kapchaProducer.createImage(text);
//
//        //将验证码存入session，不能存到服务器，不安全
//        session.setAttribute("kaptcha",text);
//        //将图片输出给浏览器
//        response.setContentType("image/png");
//        try {
//          OutputStream outputStream = response.getOutputStream();
//            ImageIO.write(image,"png",outputStream);
//        } catch (IOException e) {
//            log.error("响应验证码失败" + e.getMessage());
//        }
//    }
//生成验证码

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    //code为用户输入的验证码  rememberMe 为是否勾选记住
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model,  HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        //先判断验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }
        //检查账号,密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {//成功
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {//失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("statusMsg", map.get("statusMsg"));
            return "/site/login";
        }
    }
//    @RequestMapping(path = "/login", method = RequestMethod.POST)
//    //code为用户输入的验证码  rememberMe 为是否勾选记住
//    public String login(String username, String password, String code, boolean rememberMe,
//                        Model model, HttpSession session, HttpServletResponse response) {
//        //先判断验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");
//        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
//            model.addAttribute("codeMsg", "验证码不正确");
//            return "/site/login";
//        }
//        //检查账号,密码
//        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
//        Map<String, Object> map = userService.login(username, password, expiredSeconds);
//        if (map.containsKey("ticket")) {//成功
//            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
//            cookie.setPath(contextPath);
//            cookie.setMaxAge(expiredSeconds);
//            response.addCookie(cookie);
//            return "redirect:/index";
//        } else {//失败
//            model.addAttribute("usernameMsg", map.get("usernameMsg"));
//            model.addAttribute("passwordMsg", map.get("passwordMsg"));
//            model.addAttribute("statusMsg", map.get("statusMsg"));
//            return "/site/login";
//        }
//    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
