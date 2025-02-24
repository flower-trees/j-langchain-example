package org.salt.jlangchain.demo.rag;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.salt.jlangchain.demo.DemoApplication;
import org.salt.jlangchain.demo.rag.pdf.PdfChatExample;
import org.salt.jlangchain.demo.rag.pdf.PdfSummaryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@SpringBootConfiguration
public class PdfExampleTest {

    @Autowired
    PdfSummaryExample pdfSummaryExample;

    @Autowired
    PdfChatExample pdfChatExample;

    @Test
    public void pdfSummary() {
        pdfSummaryExample.pdfSummary();
    }

    @Test
    public void PdfChatExample() {
        pdfChatExample.pdfChat();
    }
}