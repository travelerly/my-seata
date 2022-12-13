package com.colin.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.colin.storage.entity.Storage;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * 仓库服务
 */
@LocalTCC
public interface TCCStorageService extends IService<Storage> {

    @TwoPhaseBusinessAction(name = "decreaseTCC",commitMethod = "decreaseCommit",rollbackMethod = "decreaseRollBack")
    public void decrease(@BusinessActionContextParameter(paramName = "goodsId") Integer goodsId,
                         @BusinessActionContextParameter(paramName = "quantity") Integer quantity);

    public boolean decreaseCommit(BusinessActionContext context);

    public boolean decreaseRollBack(BusinessActionContext context);
}
