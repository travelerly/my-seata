package com.colin.conf;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源代理
 * @author: colin
 * @Create: 2022/12/7 19:22
 */
//@Configuration
public class DataSourceConfiguration {

    /**
     * 设置代理数据源
     * @Primary：设置首选数据源
     * @return
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource druidDataSource(){
        return new DruidDataSource();
    }

}
