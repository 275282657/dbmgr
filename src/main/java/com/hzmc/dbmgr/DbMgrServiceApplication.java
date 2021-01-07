package com.hzmc.dbmgr;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication(exclude = SolrAutoConfiguration.class)
@MapperScan("com.hzmc.**.mapper")
@EnableFeignClients
@EnableEurekaClient
public class DbMgrServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(DbMgrServiceApplication.class, args);
	}

}
