package org.salt.jlangchain.demo.rag.pdf;

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
public class PdfExampleTest1 {

    @Autowired
    PdfSummaryExample1 pdfSummaryExample;

    @Autowired
    PdfChatExample1 pdfChatExample;

    @Test
    public void pdfSummary() {
        pdfSummaryExample.pdfSummary();
    }

    @Test
    public void PdfChatExample() {
        pdfChatExample.pdfChat();
    }
}