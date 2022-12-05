package com.colin.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.colin.order.entity.Order;
import com.colin.order.mapper.OrderMapper;
import com.colin.order.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public void add(Order order) {
        //设置订单创建时间
        order.setCreateTime(new Date());
        //保存订单
        this.save(order);
    }
}
