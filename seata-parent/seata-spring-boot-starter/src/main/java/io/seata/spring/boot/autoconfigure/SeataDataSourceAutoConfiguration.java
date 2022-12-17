/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.spring.boot.autoconfigure;

import io.seata.spring.annotation.datasource.SeataAutoDataSourceProxyCreator;
import io.seata.spring.boot.autoconfigure.properties.SeataProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import static io.seata.spring.annotation.datasource.AutoDataSourceProxyRegistrar.BEAN_NAME_SEATA_AUTO_DATA_SOURCE_PROXY_CREATOR;

/**
 * seata 数据源自动配置类
 * The type Seata data source auto configuration.
 *
 * @author xingfudeshi@gmail.com
 */
@ConditionalOnBean(DataSource.class)
// 此处对应 nacos 配置文件中的参数
@ConditionalOnExpression("${seata.enabled:true} && ${seata.enableAutoDataSourceProxy:true} && ${seata.enable-auto-data-source-proxy:true}")
@AutoConfigureAfter({SeataCoreAutoConfiguration.class})
public class SeataDataSourceAutoConfiguration {

    /**
     * SeataAutoDataSourceProxyCreator 继承了抽象类 AbstractAutoProxyCreator，是用来创建 AOP 代理的后置处理器，
     * 在每个 bean 的实例化之后，SeataAutoDataSourceProxyCreator 都会进行介入，执行 postProcessAfterInitialization() 方法，
     * 进而会执行 wrapIfNecessary() 方法：
     * 对于非 DataSource 类型的 bean 直接返回，不做任何处理
     * 对于 DataSource 类型的 bean（默认为 DruidDataSourceWrapper），会为其创建数据源代理对象 DataSourceProxy。
     *
     * The bean seataAutoDataSourceProxyCreator.
     */
    @Bean(BEAN_NAME_SEATA_AUTO_DATA_SOURCE_PROXY_CREATOR)
    @ConditionalOnMissingBean(SeataAutoDataSourceProxyCreator.class)
    public SeataAutoDataSourceProxyCreator seataAutoDataSourceProxyCreator(SeataProperties seataProperties) {
        return new SeataAutoDataSourceProxyCreator(seataProperties.isUseJdkProxy(),
            seataProperties.getExcludesForAutoProxying(), seataProperties.getDataSourceProxyMode());
    }
}
