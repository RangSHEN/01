package com.atguigu.eduservice.controller;


import com.atguigu.commonutils.R;
import com.atguigu.eduservice.entity.EduSubject;
import com.atguigu.eduservice.entity.subject.OneSubject;
import com.atguigu.eduservice.service.EduSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 课程科目 前端控制器
 * </p>
 *
 * @author testjava
 * @since 2021-03-12
 */
@RestController
@RequestMapping("/eduservice/subject")
//@CrossOrigin
public class EduSubjectController {

    @Autowired
    private EduSubjectService subjectService;

    //添加课程分类
    //获取上传过来的文件，把文件内容读取出来
    @PostMapping("addSubject")
    public R addSubject(MultipartFile file){
        //上传过来excel文件
        subjectService.saveSubject(file,subjectService); //为了后面能用把subjectService传过来
        return R.ok();

    }

    //课程分类的列表功能(树形)
    @GetMapping("getAllSubject")
    public R getAllSubject(){
        //list集合泛型是一级分类，一级分类中本身含有二级
       List<OneSubject> list = subjectService.getAllOneTwoSubject();//多条记录必返回list
        return R.ok().data("list",list);
    }

}

