package com.atguigu.eduservice.controller.front;



import com.atguigu.commonutils.JwtUtils;
import com.atguigu.commonutils.R;
import com.atguigu.commonutils.ordervo.UcenterMemberVo;
import com.atguigu.eduservice.client.UcenterClient;
import com.atguigu.eduservice.entity.EduComment;
import com.atguigu.eduservice.service.CommentService;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评论 前端控制器
 * </p>
 *
 * @author testjava
 * @since 2021-04-19
 */
@RestController
@RequestMapping("/eduservice/commentfront")
@CrossOrigin
public class CommentFrontController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UcenterClient ucenterClient;

    //根据课程id查询评论列表
    @GetMapping("getCommentFrontList/{page}/{limit}")
    public R getCommentFrontList(@PathVariable long page, @PathVariable long limit, String courseId){
        Page<EduComment> pageComment = new Page<>(page,limit);
        QueryWrapper<EduComment> wrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(courseId)){
            wrapper.eq("course_id",courseId);
        }
        //按最新排序
        wrapper.orderByDesc("gmt_create");
        commentService.page(pageComment,wrapper);

        List<EduComment> records = pageComment.getRecords();
        long current = pageComment.getCurrent();
        long total = pageComment.getTotal();
        long pages = pageComment.getPages();
        long size = pageComment.getSize();
        boolean hasNext = pageComment.hasNext();
        boolean hasPrevious = pageComment.hasPrevious();
        Map<String, Object> map = new HashMap<>();
        map.put("items",records);
        map.put("current",current);
        map.put("total",total);
        map.put("pages",pages);
        map.put("size",size);
        map.put("hasNext",hasNext);
        map.put("hasPrevious",hasPrevious);

        return R.ok().data(map);

    }

    //添加评论 需要把传过来Vo对象的值 赋值给EduComment这个对象，需要得到用户id，用户昵称，用户头像三个值
    //查用户在edu模块里面做不到，只能调用ucenter。ucenter里面的对象ucentermember edu拿不到，需要复制到commonutil里
    @PostMapping("auth/addComment")
    public R addComment(HttpServletRequest request,@RequestBody EduComment eduComment){
        String memberId = JwtUtils.getMemberIdByJwtToken(request);
        //判断用户是否登录
        if (StringUtils.isEmpty(memberId)){
            throw new GuliException(20001,"请先登录");
        }
        eduComment.setMemberId(memberId);
        //远程调用ucenter根据用户id获取用户信息 ,因为前端用了getFrontCourseInfo方法，返回的对象中有
        //teacherId 和courseId两个值，所以可以从前端获取
        UcenterMemberVo memberVo = ucenterClient.getMemberInfoById(memberId);

        eduComment.setAvatar(memberVo.getAvatar());
        eduComment.setNickname(memberVo.getNickname());

        //保存评论
        commentService.save(eduComment);

        return R.ok();

    }


}

