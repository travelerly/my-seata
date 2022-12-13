package com.colin.storage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_storage")
public class Storage {

    /**
     * 库存ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品ID
     */
    @TableField
    private String goodsId;

    /**
     * 库存量
     */
    @TableField
    private Integer storage;

    /**
     * 冻结库存量
     */
    @TableField
    private Integer frozenStorage;
}
