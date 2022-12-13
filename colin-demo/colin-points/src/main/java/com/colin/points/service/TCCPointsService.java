package com.colin.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.colin.points.entity.Points;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC
public interface TCCPointsService extends IService<Points> {

    @TwoPhaseBusinessAction(name = "increaseTCC",commitMethod = "increaseCommit",rollbackMethod = "increaseRollBack")
    public void increase(@BusinessActionContextParameter(paramName = "username") String username,
                         @BusinessActionContextParameter(paramName = "points")Integer points);

    public boolean increaseCommit(BusinessActionContext context);

    public boolean increaseRollBack(BusinessActionContext context);
}
