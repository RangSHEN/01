package com.atguigu.servicebase.exceptionhandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor   //生成有参数构造
@NoArgsConstructor   //生成无参数构造

/**
 * 非系统自带异常，需要手动抛出
 */
public class GuliException extends RuntimeException{

    private Integer code;//状态码

    private String msg;//异常属性
}
