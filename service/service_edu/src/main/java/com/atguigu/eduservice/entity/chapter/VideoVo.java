package com.atguigu.eduservice.entity.chapter;

import lombok.Data;

@Data
public class VideoVo {

    private String id;

    private String title;

    private String videoOriginalName;

    private String videoSourceId;//前端需要v-for="video in chapter.children" <a :href="'/player'+video.videoSourceId" >
}
