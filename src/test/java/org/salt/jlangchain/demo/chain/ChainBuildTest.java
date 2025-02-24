package org.salt.jlangchain.demo.chain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.salt.jlangchain.demo.DemoApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@SpringBootConfiguration
public class ChainBuildTest {

    @Autowired
    ChainBuildDemo chainBuildDemo;

    @Test
    public void SimpleDemo() {
        chainBuildDemo.SimpleDemo();
    }

    @Test
    public void SwitchDemo() {
        chainBuildDemo.SwitchDemo();
    }

    @Test
    public void ComposeDemo() {
        chainBuildDemo.ComposeDemo();
    }

    @Test
    public void ParallelDemo() {
        chainBuildDemo.ParallelDemo();
    }

    @Test
    public void RouteDemo() {
        chainBuildDemo.RouteDemo();
    }

    @Test
    public void DynamicDemo() {
        chainBuildDemo.DynamicDemo();
    }
}