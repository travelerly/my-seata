package com.colin.points.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.colin.points.entity.Points;
import com.colin.points.mapper.PointsMapper;
import com.colin.points.service.TCCPointsService;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 会员积分服务
 */
@Slf4j
@Service
public class TCCPointsServiceImpl extends ServiceImpl<PointsMapper, Points> implements TCCPointsService {
    @Autowired
    PointsMapper pointsMapper;

    /**
     * 会员增加（冻结）积分
     * @param username 用户名
     * @param points 冻结的积分
     */
    @Override
    public void increase(String username, Integer points) {
        QueryWrapper<Points> wrapper = new QueryWrapper<Points>();
        wrapper.lambda().eq(Points::getUsername, username);
        Points userPoints = this.getOne(wrapper);
        if (userPoints == null) {
            userPoints = new Points();
            userPoints.setUsername(username);
            // 设置冻结积分
            userPoints.setFrozenPoints(points);
            this.save(userPoints);
        } else {
            // 设置冻结积分
            userPoints.setFrozenPoints(points);
            this.saveOrUpdate(userPoints);
        }
    }

    @Override
    public boolean increaseCommit(BusinessActionContext context) {
        // 先查询用户积分
        QueryWrapper<Points> wrapper = new QueryWrapper<Points>();
        wrapper.lambda().eq(Points::getUsername, context.getActionContext("username"));
        Points userPoints = this.getOne(wrapper);
        if (userPoints != null){
            // 增加用户积分
            userPoints.setPoints(userPoints.getPoints() + userPoints.getFrozenPoints());
            // 清除冻结积分
            userPoints.setFrozenPoints(0);
            this.saveOrUpdate(userPoints);
        }
        log.info("=== xid ===" + context.getXid() + "提交成功");
        return true;
    }

    @Override
    public boolean increaseRollBack(BusinessActionContext context) {
        // 先查询用户积分
        QueryWrapper<Points> wrapper = new QueryWrapper<Points>();
        wrapper.lambda().eq(Points::getUsername, context.getActionContext("username"));
        Points userPoints = this.getOne(wrapper);
        if (userPoints != null){
            // 清除冻结积分
            userPoints.setFrozenPoints(0);
            // 更新
            this.saveOrUpdate(userPoints);
        }
        log.info("=== xid ===" + context.getXid() + "回滚成功");
        return true;
    }
}
