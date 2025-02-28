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
import org.salt.function.flow.context.ContextBus;
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
public class JokeGeneratorAndImproveExample {

    @Autowired
    private ChainActor chainActor;

    public void jokeGeneratorAndImprove(String topic, String vendor) {
        // Step 1: 定义基础组件
        ChatOllama qwen = ChatOllama.builder().model("qwen2.5:0.5b").build();
        ChatOllama deepseek = ChatOllama.builder().model("deepseek-llm:7b").build();
        ChatOpenAI chatgpt = ChatOpenAI.builder().model("gpt-4o").build();
        StrOutputParser parser = new StrOutputParser();

        System.out.println("主题: " + topic);

        // Step 2: 子链 - 生成初始笑话
        BaseRunnable<StringPromptValue, ?> jokePrompt = PromptTemplate.fromTemplate(
                "请给我讲一个关于${topic}的笑话，只输出笑话内容即可。"
        );
        FlowInstance jokeChain = chainActor.builder()
                .next(jokePrompt)
                .next(
                        Info.c("vendor == 'simple'", qwen),
                        Info.c("vendor == 'smart'", deepseek),
                        Info.c(input -> "抱歉，我不知道怎么生成这个笑话！")
                )
                .next(parser)
                .build();

        // Step 3: 子链 - 评价笑话
        BaseRunnable<StringPromptValue, ?> analysisPrompt = PromptTemplate.fromTemplate(
                "这个笑话好笑吗？请简要评价（50字内）：${joke}，只输出评价内容即可。"
        );
        FlowInstance analysisChain = chainActor.builder()
                .next(analysisPrompt)
                .next(chatgpt)
                .next(parser)
                .build();

        // Step 4: 子链 - 改进笑话
        BaseRunnable<StringPromptValue, ?> improvePrompt = PromptTemplate.fromTemplate(
                "根据以下评价：${feedback}\n改进这个笑话，使它更有趣：${joke}\n只输改进后的笑话内容即可，不要其他内容："
        );
        FlowInstance improveChain = chainActor.builder()
                .next(improvePrompt)
                .next(
                        Info.c("vendor == 'simple'", qwen),
                        Info.c("vendor == 'smart'", deepseek),
                        Info.c(input -> "抱歉，我不知道怎么生成这个笑话！")
                )
                .next(parser)
                .build();

        // Step 5: 主链 - 组合所有步骤
        FlowInstance mainChain = chainActor.builder()
                .next(jokeChain) // 生成初始笑话
                .next(input -> { System.out.println("笑话: " + input); return input; }) // 中间打印笑话内容
                .next(input -> Map.of("joke", ((Generation) input).getText())) // 提取笑话
                .next(analysisChain) // 评价笑话
                .next(input -> { System.out.println("评价: " + input); return input; }) // 中间打印评价内容
                .next(input -> Map.of(
                        "joke", ((Generation) ContextBus.get().getResult(jokeChain.getFlowId())).getText(), // 保留原始笑话
                        "feedback", ((Generation) input).getText() // 获取评价
                ))
                .next(improveChain) // 改进笑话
                .build();

        // Step 6: 执行并输出结果
        ChatGeneration result = chainActor.invoke(mainChain, Map.of("topic", topic, "vendor", vendor));
        System.out.println("改进后的笑话: " + result.getText());
    }
}