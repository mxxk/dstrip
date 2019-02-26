package dstrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

public class TextStripTest {
    @Test
    public void testMultiPage() throws IOException {
        var text = getProcessedDocumentText("multi-page.pdf");
        // Make sure final document has no watermark.
        assertFalse(text.contains("DEMONSTRATION DOCUMENT ONLY"));
        // Make sure final document retained non-watermark text.
        assertTrue(text.contains("John"));
        assertTrue(text.contains("Smith"));
        assertTrue(text.contains("hello, world"));
        assertTrue(text.contains("Follow the steps below"));
    }

    @Test
    public void testTextBeforeWatermark() throws IOException {
        var text = getProcessedDocumentText("text-before-watermark.pdf");
        // Make sure final document has no watermark.
        assertFalse(text.contains("DEMONSTRATION DOCUMENT ONLY"));
        // Make sure final document retained non-watermark text.
        assertTrue(text.contains("John"));
        assertTrue(text.contains("Smith"));
    }

    @Test
    public void testNoTextBeforeWatermark() throws IOException {
        var text = getProcessedDocumentText("no-text-before-watermark.pdf");
        // Make sure final document has no watermark.
        assertFalse(text.contains("DEMONSTRATION DOCUMENT ONLY"));
        // Make sure final document retained non-watermark text.
        assertTrue(text.contains("hello, world"));
        assertTrue(text.contains("Some other text"));
    }

    @Test
    public void testOnlyWatermark() throws IOException {
        var text = getProcessedDocumentText("only-watermark.pdf");
        assertEquals("", text.strip());
    }

    @Test
    public void testNoWatermark() throws IOException {
        var text = getProcessedDocumentText("no-watermark.pdf");
        assertEquals("hello, world", text.strip());
    }

    @Test
    public void testBlank() throws IOException {
        var text = getProcessedDocumentText("blank.pdf");
        assertEquals("", text.strip());
    }

    private String getProcessedDocumentText(String name) throws IOException {
        try (
            var stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(name);
            var document = PDDocument.load(stream);
        ) {
            Main.processDocument(document);
            return new PDFTextStripper().getText(document);
        }
    }
}