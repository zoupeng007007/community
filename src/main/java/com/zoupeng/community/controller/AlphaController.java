package com.zoupeng.community.controller;

import com.sun.javafx.collections.MappingChange;
import com.zoupeng.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.PartHttpMessageWriter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@RestController
public class AlphaController {
    @Autowired
    private AlphaService service;

    @RequestMapping("/hello")

    public String sayHello(){
       return "hello";
    }
    @RequestMapping("/find")
    public String find(){
        return service.find();
    }

    @RequestMapping("/http")
    /**
     * 没有返回值，可以采用request和response，底层也是采用的这种方式,返回的页面
     */
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ":" + value);
        }
        System.out.println(request.getParameter("code"));
        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try (
                PrintWriter writer = response.getWriter();
                ){
            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //参数获取
    //   /student?current=1&limit=20
    @RequestMapping(path = "/student",method = RequestMethod.GET)
    public String getStudent(
            @RequestParam(name = "current",required = false,defaultValue = "1") int current,
            @RequestParam(name = "limit",required = false,defaultValue = "10") int limit){//只要传的参数在url中，就会被获取与之匹配的参数
                                                                                        //要考虑url没有该参数的情况，required=false即可
        System.out.println(current);
        System.out.println(limit);
        return "somsdsd";
    }

    //参数获取路径变量
    // /student/123
//    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @GetMapping(path = "/student/{id}")  //相当于上面
    public String getParameter(@PathVariable("id") int id){
        System.out.println(id);
        return "success";
    }
    @PostMapping(path = "/student")

    public Map<String,Object> saveStudent(String name,int age){//可以不用@RequestParameter注解，只要有参数即可
        Map<String,Object> res = new HashMap<>();
        res.put("name",name);
        res.put("age",age);

        return res;
    }
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    //responseBody 默认返回json
    public ModelAndView getTeacher(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","张三");
        modelAndView.addObject("age",30);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    @RequestMapping(path = "/school",method = RequestMethod.GET)
    //不能使用@Restcontroller,不然默认是@ResponsBody
    public String getSchool(Model model){//DispatcherServlet调用getSchool方法时，自动实例化并由其处理返回
        model.addAttribute("name","北京大学");
        model.addAttribute("age",80);
        return "demo/view";//返回View的路径
    }

    //响应JSON数据  (一般用于异步请求)
    //Java对象  JS对象，无法直接转化，通过JSON
    //application/json  返回Map 请求资源时的数据类型为json
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    public Map<String,Object> emp(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age",11);
        emp.put("salary",6666);
        return emp;
    }
    @RequestMapping(path = "/emp1",method = RequestMethod.GET)
    public List<Map<String,Object>> emp1(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age",11);
        emp.put("salary",6666);
        List<Map<String,Object>> list = new ArrayList<>();
        list.add(emp);
        list.add(emp);
        list.add(emp);
        Integer.valueOf("abc");
        return list;
    }

    //ajax示例
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    public String testAjax(String name, int age){
        System.out.println(name);
        System.out.println(age);
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put(name,age);
        return "{'code':0,'status':3}";
    }

}
