/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.salt.jlangchain.demo.chain.build.joke;

import org.salt.function.flow.FlowInstance;
import org.salt.function.flow.Info;
import org.salt.jlangchain.core.ChainActor;
import org.salt.jlangchain.core.llm.ollama.ChatOllama;
import org.salt.jlangchain.core.llm.openai.ChatOpenAI;
import org.salt.jlangchain.core.parser.StrOutputParser;
import org.salt.jlangchain.core.parser.generation.ChatGeneration;
import org.salt.jlangchain.core.parser.generation.Generation;
import org.salt.jlangchain.core.prompt.string.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LaughMaster {

    @Autowired
    private ChainActor chainActor;

    public void makeMeLaugh(String topic, String vibe) {
        // 模型军团：Ollama和ChatGPT待命
        ChatOllama funnyOllama = ChatOllama.builder().model("llama3:8b").build();
        ChatOpenAI coolGPT = ChatOpenAI.builder().model("gpt-4").build();

        System.out.println("主题：" + topic);

        // 子链1：生成笑话
        var jokePrompt = PromptTemplate.fromTemplate("给我讲个关于${topic}的中式笑话，越搞笑越好！");
        FlowInstance jokeChain = chainActor.builder()
                .next(jokePrompt)
                .next(
                        Info.c("vibe == 'wild'", funnyOllama), // 狂野派用Ollama
                        Info.c("vibe == 'smart'", coolGPT),    // 聪明派用ChatGPT
                        Info.c(input -> "哎呀，这主题太冷门，我笑不出来！"))
                .next(new StrOutputParser())
                .build();

        // 子链2：毒舌评价
        var judgePrompt = PromptTemplate.fromTemplate("这个笑话好笑吗？使用中文毒舌点说：${joke}");
        FlowInstance judgeChain = chainActor.builder()
                .next(judgePrompt)
                .next(funnyOllama) // 毒舌大师Ollama上场
                .next(new StrOutputParser())
                .build();

        // 主链：笑话+评价一条龙
        FlowInstance laughFlow = chainActor.builder()
                .next(jokeChain) // 先生成笑话
                .next(input -> { System.out.println("生成笑话: " + input); return input; }) // 中间打印笑话内容
                .next(input -> Map.of("joke", ((Generation) input).getText()))
                .next(judgeChain) // 再评价笑话
                .build();

        // 启动魔法！
        ChatGeneration result = chainActor.invoke(laughFlow, Map.of("topic", topic, "vibe", vibe));
        System.out.println("笑话大师说：" + result.getText());
    }
}