package com.colin.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 积分实体类
 */
@Data
@TableName("t_points")
public class Points {

    /**
     * 积分ID
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    @TableField
    private String username;

    /**
     * 增加的积分
     */
    @TableField
    private Integer points;

    /**
     * frozen_points
     */
    @TableField
    private Integer frozenPoints;
}
