package com.atguigu.staservice.schedule;

import com.atguigu.staservice.service.StatisticsDailyService;
import com.atguigu.staservice.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class scheduledTask {

    @Autowired
    private StatisticsDailyService staService;

    //0/5 * * * * ?表示每隔5秒执行一次这个方法 必须是6位
 /*   @Scheduled(cron = "0/5 * * * * ?")
    public void task1(){
        System.out.println("***********执行了");
    }*/

    //在每天凌晨1点,把前一天数据进行数据查询添加。传不了day 3月9日凌晨1点 把3月8日数据添加
    @Scheduled(cron = "0 0 1 * * ?")
    public void task2(){
       staService.registerCount(DateUtil.formatDate(DateUtil.addDays(new Date(), -1)));
    }
}
