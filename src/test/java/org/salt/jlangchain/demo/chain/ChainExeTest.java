package org.salt.jlangchain.demo.chain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.salt.jlangchain.demo.DemoApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@SpringBootConfiguration
public class ChainExeTest {

    @Autowired
    ChainExtDemo chainExtDemo;

    @Test
    public void StreamDemo() throws TimeoutException, InterruptedException {
        chainExtDemo.StreamDemo();
    }

    @Test
    public void ChainStreamDemo() throws TimeoutException, InterruptedException {
        chainExtDemo.ChainStreamDemo();
    }

    @Test
    public void InputDemo() throws TimeoutException, InterruptedException {
        chainExtDemo.InputDemo();
    }

    @Test
    public void OutputFunctionDemo() throws TimeoutException, InterruptedException {
        chainExtDemo.OutputFunctionDemo();
    }

    @Test
    public void EventDemo() throws TimeoutException {
        chainExtDemo.EventDemo();
    }

    @Test
    public void EventChainDemo() throws TimeoutException {
        chainExtDemo.EventChainDemo();
    }

    @Test
    public void EventFilterDemo() throws TimeoutException {
        chainExtDemo.EventFilterDemo();
    }
}