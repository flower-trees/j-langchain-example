package org.salt.jlangchain.demo;

import org.salt.jlangchain.config.JLangchainConfig;
import org.salt.jlangchain.demo.chain.ChainBuildDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"org.salt.jlangchain.demo"})
@Import(JLangchainConfig.class)
public class DemoApplication {

    @Autowired
    ChainBuildDemo chainBuildDemo;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
