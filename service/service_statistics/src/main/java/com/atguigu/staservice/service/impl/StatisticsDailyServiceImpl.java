package com.atguigu.staservice.service.impl;

import com.atguigu.commonutils.R;
import com.atguigu.staservice.Client.UcenterClient;
import com.atguigu.staservice.entity.StatisticsDaily;
import com.atguigu.staservice.mapper.StatisticsDailyMapper;
import com.atguigu.staservice.service.StatisticsDailyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 网站统计日数据 服务实现类
 * </p>
 *
 * @author testjava
 * @since 2021-04-26
 */
@Service
public class StatisticsDailyServiceImpl extends ServiceImpl<StatisticsDailyMapper, StatisticsDaily> implements StatisticsDailyService {

    @Autowired
    private UcenterClient ucenterClient;
    @Override
    public void registerCount(String day) {
        //添加记录之前删除表相同日期的数据
        QueryWrapper<StatisticsDaily> wrapper = new QueryWrapper<>();
        wrapper.eq("date_calculated",day);
        baseMapper.delete(wrapper);

        //远程调用得到某一天注册人数
        R registerR = ucenterClient.countRegister(day);
        Integer countRegister = (Integer)registerR.getData().get("countRegister");

        //把获取数据添加数据库，统计分析表里
        //第一种方式，加之前做判断看表里有没有相同日期数   据，有做更新
        //第二种先删除，再添加
        StatisticsDaily sta = new StatisticsDaily();
        sta.setRegisterNum(countRegister);//注册人数
        sta.setDateCalculated(day);//统计日期

        sta.setVideoViewNum(RandomUtils.nextInt(100,200));
        sta.setLoginNum(RandomUtils.nextInt(100,200));
        sta.setCourseNum(RandomUtils.nextInt(100,200));
        baseMapper.insert(sta);
    }

    //图表显示，返回两部分数据，日期json数组，数量json数组，也可以传对象
    @Override
    public Map<String, Object> getShowData(String type, String begin, String end) {
        //根据条件查询对应数据
        QueryWrapper<StatisticsDaily> wrapper = new QueryWrapper<>();
        wrapper.between("date_calculated",begin,end);
        wrapper.select("date_calculated",type);//指定要查询的列，是登录就查登录字段，是播放量就查播放量字段 前端也有type

        //***注意使用 baseMapper.selectList 或者 service.list 返回的值都需要遍历取值 赋值给需要的list,list才能赋值给list
        //**(遍历中需要 get的值 如daily.getDateCalculated() 就必须要加wrapper.select
        //***
        List<StatisticsDaily> staList = baseMapper.selectList(wrapper);//把两个查询条件包装起来

        //因为返回的有两部分数据：日期和日期对应的数量
        //前端要求数组的json结构，对应后端java代码是list集合
        //创建两个list集合，一个日期list，一个数量list

        List<String> date_calculatedList = new ArrayList<>();
        List<Integer> numDataList = new ArrayList<>();

        //遍历所有数据list集合，进行封装
        for (int i = 0; i < staList.size(); i++) {
            StatisticsDaily daily = staList.get(i);
            //封装日期list集合
            date_calculatedList.add(daily.getDateCalculated());
            //封装对应的数量
            switch (type){
                case "login_num":
                    numDataList.add(daily.getLoginNum());
                    break;
                case "register_num":
                    numDataList.add(daily.getRegisterNum());
                    break;
                case "video_view_num":
                    numDataList.add(daily.getVideoViewNum());
                    break;
                case "course_num":
                    numDataList.add(daily.getCourseNum());
                    break;
                default:
                    numDataList.add(0);
                    break;
            }
        }

        //把封装之后两个list集合放到map集合，进行返回
        Map<String,Object> map = new HashMap<>();
        map.put("data_calculatedList",date_calculatedList);
        map.put("numDataList",numDataList);
        return map;
    }
}
