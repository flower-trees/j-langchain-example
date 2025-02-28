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

package org.salt.jlangchain.demo.rag.pdf;

import org.salt.function.flow.FlowInstance;
import org.salt.function.flow.context.ContextBus;
import org.salt.jlangchain.core.BaseRunnable;
import org.salt.jlangchain.core.ChainActor;
import org.salt.jlangchain.core.llm.ollama.ChatOllama;
import org.salt.jlangchain.core.parser.StrOutputParser;
import org.salt.jlangchain.core.parser.generation.ChatGeneration;
import org.salt.jlangchain.core.prompt.string.PromptTemplate;
import org.salt.jlangchain.core.prompt.value.StringPromptValue;
import org.salt.jlangchain.rag.embedding.OllamaEmbeddings;
import org.salt.jlangchain.rag.loader.pdf.PdfboxLoader;
import org.salt.jlangchain.rag.media.Document;
import org.salt.jlangchain.rag.splitter.StanfordNLPTextSplitter;
import org.salt.jlangchain.rag.vector.BaseRetriever;
import org.salt.jlangchain.rag.vector.Milvus;
import org.salt.jlangchain.rag.vector.VectorStore;
import org.salt.jlangchain.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class PdfChatExample {

    @Autowired
    ChainActor chainActor;

    public void pdfChat() {

        PdfboxLoader loader = PdfboxLoader.builder()
                .filePath("./files/pdf/en/Transformer.pdf")
                .extractImages(false)
                .build();
        List<Document> documents = loader.load();

        System.out.println("Load documents count:" + documents.size());

        StanfordNLPTextSplitter splitter = StanfordNLPTextSplitter.builder().chunkSize(1000).chunkOverlap(100).build();
        List<Document> splits = splitter.splitDocument(documents);

        System.out.println("Splits count:" + splits.size());

        VectorStore vectorStore = Milvus.fromDocuments(
                splits,
                OllamaEmbeddings.builder().model("nomic-embed-text").vectorSize(768).build(),
                "JLangChain");

        System.out.println("Save success");

        BaseRetriever baseRetriever = vectorStore.asRetriever();

        String promptTemplate = """
                Please provide the following text content:
                
                ${text}
                
                Answer the question:${question}
                """;

        BaseRunnable<StringPromptValue, ?> prompt = PromptTemplate.fromTemplate(promptTemplate);
        ChatOllama llm = ChatOllama.builder().model("deepseek-r1:7b").build();

        Function<Object, String> formatDocs = input -> {
            if (input == null) {
                return "";
            }
            List<Document> docs = (List<Document>) input;
            StringBuilder sb = new StringBuilder();
            for (Document doc : docs) {
                sb.append(doc.getPageContent()).append("\n");
            }
            return sb.toString();
        };

        FlowInstance chain = chainActor.builder()
                .next(baseRetriever)
                .next(input -> { System.out.println("Query content:" + JsonUtil.toJson(input)); return input;})
                .next(formatDocs)
                .next(input -> Map.of("text", input, "question", ContextBus.get().getFlowParam()))
                .next(prompt)
                .next(llm)
                .next(new StrOutputParser()).build();

        ChatGeneration result = chainActor.invoke(chain, "Why is masking necessary in the decoder’s self-attention mechanism?");
        System.out.println("Chat Result:" + result);
    }
}
