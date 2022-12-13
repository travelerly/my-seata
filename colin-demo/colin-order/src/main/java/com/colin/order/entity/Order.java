package com.colin.order.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单实体类
 */
@Data
@TableName("t_order")
public class Order implements Serializable {

    /**
     * 订单id
     */
    @TableId
    private Long id;

    /**
     * 商品ID
     */
    @TableField
    private Integer goodsId;

    /**
     * 商品数量
     */
    @TableField
    private Integer num;

    /**
     * 商品总金额
     */
    @TableField
    private Double money;

    /**
     * 订单创建时间
     */
    @TableField
    private java.util.Date createTime;

    /**
     * 用户名称
     */
    @TableField
    private String username;

    /**
     * setatus
     */
    @TableField
    private Integer status;
}

