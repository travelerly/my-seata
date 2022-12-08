package com.colin.bussiness.service.impl;


import com.colin.bussiness.feign.OrderServiceFeign;
import com.colin.bussiness.feign.PointsServiceFeign;
import com.colin.bussiness.feign.StorageServiceFeign;
import com.colin.bussiness.service.BussinessService;
import com.colin.bussiness.utils.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 业务逻辑
 */
@Service
public class BussinessServiceImpl implements BussinessService {

    @Autowired
    OrderServiceFeign orderServiceFeign;
    @Autowired
    PointsServiceFeign pointsServiceFeign;
    @Autowired
    StorageServiceFeign storageServiceFeign;

    @Autowired
    IdWorker idWorker;

    /**
     * 商品销售
     *
     * @param goodsId  商品id
     * @param num      销售数量
     * @param username 用户名
     * @param money    金额
     */
    /*@Transactional*/
    @GlobalTransactional(rollbackFor = Exception.class,timeoutMills = 6000,name = "sale")
    public void sale(Integer goodsId, Integer num, Double money, String username) {
        //创建订单
        orderServiceFeign.addOrder(idWorker.nextId(), goodsId, num, money, username);
        //增加积分
        pointsServiceFeign.increase(username, (int) (money / 10));
        //扣减库存
        storageServiceFeign.decrease(goodsId, num);
    }
}
