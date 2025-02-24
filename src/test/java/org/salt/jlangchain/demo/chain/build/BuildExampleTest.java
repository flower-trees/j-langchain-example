package org.salt.jlangchain.demo.chain.build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.salt.jlangchain.demo.DemoApplication;
import org.salt.jlangchain.demo.chain.ChainBuildDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@SpringBootConfiguration
public class BuildExampleTest {

    @Autowired
    SimpleBuildExample simpleBuildExample;

    @Test
    public void SimpleDemo() {
        simpleBuildExample.SimpleDemo();
    }
}