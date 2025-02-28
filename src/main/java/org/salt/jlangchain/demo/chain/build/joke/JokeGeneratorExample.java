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
import org.salt.jlangchain.core.BaseRunnable;
import org.salt.jlangchain.core.ChainActor;
import org.salt.jlangchain.core.llm.ollama.ChatOllama;
import org.salt.jlangchain.core.llm.openai.ChatOpenAI;
import org.salt.jlangchain.core.parser.StrOutputParser;
import org.salt.jlangchain.core.parser.generation.ChatGeneration;
import org.salt.jlangchain.core.parser.generation.Generation;
import org.salt.jlangchain.core.prompt.string.PromptTemplate;
import org.salt.jlangchain.core.prompt.value.StringPromptValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JokeGeneratorExample {

    @Autowired
    private ChainActor chainActor;

    public void jokeGenerator(String topic, String vendor) {
        // Step 1: 定义基础组件
        ChatOllama ollama = ChatOllama.builder().model("llama3:8b").build();
        ChatOpenAI chatGPT = ChatOpenAI.builder().model("gpt-4").build();
        StrOutputParser parser = new StrOutputParser();

        System.out.println("主题: " + topic);

        // Step 2: 子链 - 生成笑话
        BaseRunnable<StringPromptValue, ?> jokePrompt = PromptTemplate.fromTemplate(
                "请给我讲一个关于${topic}的中式笑话。"
        );
        FlowInstance jokeChain = chainActor.builder()
                .next(jokePrompt)
                .next(
                        Info.c("vendor == 'ollama'", ollama),
                        Info.c("vendor == 'chatgpt'", chatGPT),
                        Info.c(input -> "抱歉，我不知道怎么生成这个笑话！")
                )
                .next(parser)
                .build();

        // Step 3: 子链 - 评价笑话
        BaseRunnable<StringPromptValue, ?> analysisPrompt = PromptTemplate.fromTemplate(
                "这个中文笑话好笑吗？请用中文简要评价：${joke}"
        );
        FlowInstance analysisChain = chainActor.builder()
                .next(analysisPrompt)
                .next(ollama) // 使用Ollama模型进行评价
                .next(parser)
                .build();

        // Step 4: 主链 - 组合嵌套
        FlowInstance mainChain = chainActor.builder()
                .next(jokeChain) // 先生成笑话
                .next(input -> { System.out.println("生成的笑话: " + input); return input; }) // 中间打印笑话内容
                .next(input -> Map.of("joke", ((Generation) input).getText())) // 提取笑话文本
                .next(analysisChain) // 再评价笑话
                .build();

        // Step 5: 执行并输出结果
        ChatGeneration result = chainActor.invoke(mainChain, Map.of("topic", topic, "vendor", vendor));
        System.out.println("生成的笑话的评价: " + result.getText());
    }
}