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

package org.salt.jlangchain.demo.rag.tools;

import org.apache.commons.lang3.StringUtils;
import org.salt.function.flow.FlowInstance;
import org.salt.function.flow.Info;
import org.salt.function.flow.context.ContextBus;
import org.salt.jlangchain.core.ChainActor;
import org.salt.jlangchain.core.handler.ConsumerHandler;
import org.salt.jlangchain.core.handler.TranslateHandler;
import org.salt.jlangchain.core.llm.ollama.ChatOllama;
import org.salt.jlangchain.core.message.AIMessage;
import org.salt.jlangchain.core.parser.StrOutputParser;
import org.salt.jlangchain.core.parser.generation.ChatGeneration;
import org.salt.jlangchain.core.prompt.string.PromptTemplate;
import org.salt.jlangchain.core.prompt.value.StringPromptValue;
import org.salt.jlangchain.rag.tools.Tool;
import org.salt.jlangchain.utils.PromptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class ZeroShotReactDescription {

    @Autowired
    ChainActor chainActor;

    public void run() {

        PromptTemplate prompt = PromptTemplate.fromTemplate(
                """
                Answer the following questions as best you can. You have access to the following tools:
                
                ${tools}
                
                Use the following format:
                
                Question: the input question you must answer
                Thought: Consider whether you already have enough information to answer the question. If so, proceed directly to the final answer.
                
                If additional information is needed, take the following steps:
                - Identify what specific information is missing.
                - Call the appropriate tool to obtain that information.
                - Analyze the new information and determine if the question can now be answered.
                
                When using tools, follow this structured approach:
                Action: the action to take, should be one of [${toolNames}]
                Action Input: the input to the action
                Observation: the result of the action
                
                - You may use tools **up to 3 times**. If you still lack a complete answer after 3 attempts, summarize the best possible response.
                - If a tool's result is **irrelevant or does not improve understanding**, do not call the same tool again. Instead, attempt to derive an answer from available information.
                
                Thought: Based on the gathered information, determine if you can now provide a final answer. If yes, proceed to:
                Final Answer: the final answer to the original input question.
                If not, provide the best possible answer with a note on any remaining uncertainties.
                
                Begin!
                
                Question: ${input}
                Thought:
                """);

        ChatOllama llm = ChatOllama.builder().model("llama3:8b").temperature(0f).build();

        Tool getWeather = Tool.builder()
                .name("get_weather")
                .params("location: String")
                .description("Get city weather information and enter the city name")
                .func(location -> String.format("The weather in %s is sunny with a temperature of 25°C", location))
                .build();

        Tool getTime = Tool.builder()
                .name("get_time")
                .params("city: String")
                .description("Get city the current time and enter the city name")
                .func(location -> String.format("%s The current time is 12:00 PM", location))
                .build();

        List<Tool> tools = List.of(getWeather, getTime);

        prompt.withTools(tools);

        TranslateHandler<AIMessage, AIMessage> cut = new TranslateHandler<>(llmResult -> {
            if (llmResult == null || StringUtils.isEmpty(llmResult.getContent()) || !llmResult.getContent().contains("Observation:")) {
                if (llmResult != null) {
                    System.out.println(llmResult.getContent());
                }
                return llmResult;
            }
            String prefix = llmResult.getContent().substring(0, llmResult.getContent().indexOf("Observation:"));
            System.out.println(prefix);
            llmResult.setContent(prefix);
            return llmResult;
        });

        TranslateHandler<Map<String, String>, AIMessage> trans = new TranslateHandler<>(llmResult -> PromptUtil.stringToMap(llmResult.getContent()));

        int limit = 10;
        Function<Integer, Boolean> isFinish = i -> {
            Map<String, String> map = ContextBus.get().getResult(trans.getNodeId());
            return i < limit && (map == null || (map.containsKey("Action") && map.containsKey("Action Input")));
        };

        Function<Object, Boolean> isCall = map -> ((Map<String, String>) map).containsKey("Action") && ((Map<String, String>) map).containsKey("Action Input");

        TranslateHandler<Object, Map<String, String>> call = new TranslateHandler<>(map -> {
            StringPromptValue promptResult = ContextBus.get().getResult(prompt.getNodeId());
            AIMessage cutResult = ContextBus.get().getResult(cut.getNodeId());

            Tool useTool = tools.stream().filter(t -> t.getName().toLowerCase().equals(map.get("Action"))).findAny().orElse(null);
            if (useTool == null) {
                promptResult.setText(promptResult.getText().trim() + "again");
                return promptResult;
            }
            String observation = (String) useTool.getFunc().apply(map.get("Action Input"));
            System.out.println("Observation: " + observation);

            String prefix = cutResult.getContent();
            String agentScratchpad = prefix.substring(prefix.indexOf("Thought:") + 8).trim() + "\nObservation:" + observation + "\nThought:";
            promptResult.setText(promptResult.getText().trim() + agentScratchpad);
            return promptResult;
        });

        StrOutputParser parser = new StrOutputParser();

        TranslateHandler<Object, Object> answer = new TranslateHandler<>(input -> {
            ChatGeneration generation = (ChatGeneration) input;
            String content = generation.getText();
            if (content.contains("Final Answer:")) {
                int start = content.indexOf("Final Answer:") + 13;
                int end = content.indexOf("\n", start);
                if (end > 0) {
                    generation.setText(content.substring(start, end).trim());
                } else {
                    generation.setText(content.substring(start).trim());
                }
            }
            return generation;
        });

        ConsumerHandler<?> sPrint = new ConsumerHandler<>(input -> System.out.println("> Entering new AgentExecutor chain..."));
        ConsumerHandler<?> ePrint = new ConsumerHandler<>(input -> System.out.println("> Finished chain."));

        FlowInstance chain = chainActor.builder()
                .next(sPrint) // print start
                .next(prompt)
                .loop(
                        // Loop Exit Conditions
                        isFinish,

                        // Loop Flow
                        llm,
                        chainActor.builder()
                                .next(cut).next(trans) // convert content generated by mll
                                .next(
                                        Info.c(isCall, call), // need call function
                                        Info.c(input -> ContextBus.get().getResult(llm.getNodeId())) // else no need, return mll result
                                )
                                .build()

                )
                .next(parser)
                .next(answer) // deal result
                .next(ePrint) // print end
                .build();

        ChatGeneration result = chainActor.invoke(chain, Map.of("input", "What's the weather like in Shanghai?"));
        System.out.println(result);
    }
}
