package org.salt.jlangchain.demo.chain.build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.salt.jlangchain.demo.DemoApplication;
import org.salt.jlangchain.demo.chain.build.joke.JokeGeneratorExample;
import org.salt.jlangchain.demo.chain.build.joke.JokeGeneratorAndImproveExample;
import org.salt.jlangchain.demo.chain.build.joke.LaughMaster;
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

    @Autowired
    JokeGeneratorExample jokeGeneratorExample;
    @Test
    public void JokeGeneratorDemo() {
        jokeGeneratorExample.jokeGenerator("程序员", "chatgpt");
    }

    @Autowired
    LaughMaster laughMaster;
    @Test
    public void LaughMasterDemo() {
        laughMaster.makeMeLaugh("程序员", "smart");
    }

    @Autowired
    JokeGeneratorAndImproveExample jokeGeneratorAndImproveExample;
    @Test
    public void JokeImproveDemo() {
        jokeGeneratorAndImproveExample.jokeGeneratorAndImprove("猫", "simple");
    }
}