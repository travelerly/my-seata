package com.colin.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.colin.order.entity.Order;
import com.colin.order.mapper.OrderMapper;
import com.colin.order.service.TCCOrderService;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class TCCOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements TCCOrderService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Order order) {
        // 设置订单创建时间
        order.setCreateTime(new Date());
        // try 阶段预检查
        order.setStatus(0);
        // 保存订单
        this.save(order);
        log.info("=== 一阶段执行完成 ===");
    }

    @Override
    public boolean addCommit(BusinessActionContext context) {
        Order order = JSON.parseObject(context.getActionContext("order").toString(), Order.class);
        order = this.getById(order.getId());
        if (order != null){
            // commit 阶段-提交事务
            order.setStatus(1);
            // 修改订单（update status）
            this.saveOrUpdate(order);
        }
        log.info("=== xid ===" + context.getXid() + "提交成功");
        return true;
    }

    @Override
    public boolean addRollBack(BusinessActionContext context) {
        Order order = JSON.parseObject(context.getActionContext("order").toString(), Order.class);
        order = this.getById(order.getId());
        if (order != null){
            // 删除订单
            this.removeById(order.getId());
        }
        log.info("=== xid ===" + context.getXid() + "回滚成功");
        return true;
    }

}
