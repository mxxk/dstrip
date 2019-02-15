package dstrip;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

public class TextStripTest {
    @Test
    public void testOriginalDocument() throws IOException {
        try (var document = getDocument()) {
            String text = getText(document, 1, 1);
            // Make sure document has watermark.
            assertTrue(text.contains("DEMONSTRATION DOCUMENT ONLY"));
            // Make sure document has form data.
            assertTrue(text.contains("John"));
            assertTrue(text.contains("Smith"));
        }
    }

    @Test
    public void testProcessedDocument() throws IOException {
        try (var document = getDocument()) {
            Main.processDocument(document);
            String text = getText(document, 1, 1);
            // Make sure document no longer has watermark.
            assertFalse(text.contains("DEMONSTRATION DOCUMENT ONLY"));
            // Make sure document still has form data.
            assertTrue(text.contains("John"));
            assertTrue(text.contains("Smith"));
        }
    }

    private PDDocument getDocument() throws IOException {
        try (
            var stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("signed.pdf");
        ) {
            return PDDocument.load(stream);
        }
    }

    private String getText(PDDocument document, int startPage, int endPage) throws IOException {
        var stripper = new PDFTextStripper();
        // TODO: Extend test to multi-page PDFs.
        stripper.setStartPage(startPage);
        stripper.setEndPage(endPage);
        return stripper.getText(document);
    }
}