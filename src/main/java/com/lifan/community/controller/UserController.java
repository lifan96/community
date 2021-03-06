package com.lifan.community.controller;

import com.lifan.community.entity.User;
import com.lifan.community.service.UserService;
import com.lifan.community.util.CommunityUtil;
import com.lifan.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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

    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }

        //获取文件原始文件名
        String fileName = headerImage.getOriginalFilename();
        //拆分文件名后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //判断后缀是否为空
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确！");
            return "/site/setting";
        }

        //生成随机新文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！",e);
        }

        //更新当前用户的头像的路径
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        //操作数据库，更新当前用户头像路径
        userService.updateHeader(user.getId(),headerUrl);

        //重定向到首页
        return "redirect:/index";

    }

    //读取更新后的图片
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
                ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败！"+e.getMessage());
        }
    }


    @RequestMapping(value = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,Model model){
        //判断密码是否为空
        if (oldPassword == null && newPassword == null){
            return "/site/setting";
        }
        //获取当前用户
        User user = hostHolder.getUser();

        //将密码转换md5
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());

        //比较当前用户密码与需要更改的密码是否相同

        if (!user.getPassword().equals(oldPassword)){
            model.addAttribute("oldPasswordMsg","原密码有误，请重新输入！");
            return "/site/setting";
        }

        if (user.getPassword().equals(newPassword)){
            model.addAttribute("newPasswordMsg","修改密码不能与旧密码相同！");
            return "/site/setting";
        }

        //将新密码更新至数据库
        userService.updatePassword(user.getId(),newPassword);
        //退出，重新登录
        hostHolder.clear();     //提前清除当前用户
        model.addAttribute("msg","修改成功，请重新登录！");
        model.addAttribute("target","/logout");
        return "/site/operate-result";
    }

}
