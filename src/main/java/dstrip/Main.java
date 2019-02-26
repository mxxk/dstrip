package dstrip;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;


public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println(
                "Usage:\n" +
                "\n" +
                "    dstrip <input pdf file> <output pdf file>\n"
            );
            return;
        }

        var inputFileName = args[0];
        var outputFileName = args[1];
        var inputFile = new File(inputFileName);

        try (var document = PDDocument.load(inputFile)) {
            processDocument(document);
            document.save(outputFileName);
        }
    }

    static void processDocument(PDDocument document) throws IOException {
        var engine = new EngineThatCould();

        for (var page : document.getPages()) {
            engine.processPage(page);
            var oldStream = engine.getWatermarkStream();

            if (oldStream == null) {
                // If the watermark stream is null, the watermark was not found on this page. Skip.
                continue;
            }

            int watermarkStartIndex = engine.getWatermarkStartIndex();
            int watermarkEndIndex = engine.getWatermarkEndIndex();
            var newStream = new PDStream(document);
            var parser = new PDFStreamParser(oldStream);

            try (var outStream = newStream.createOutputStream(COSName.FLATE_DECODE)) {
                var streamWriter = new ContentStreamWriter(outStream);
                int textBlockCounter = 0;
                Object prevToken = null;
                Object token = parser.parseNextToken();

                while (token != null) {
                    if (
                        token instanceof Operator &&
                        ((Operator)token).getName().toLowerCase().equals("tj")
                    ) {
                        if (
                            textBlockCounter >= watermarkStartIndex &&
                            textBlockCounter < watermarkEndIndex
                        ) {
                            // Do not write the current token nor its previous argument.
                            token = prevToken = null;
                        }

                        textBlockCounter++;
                    }

                    if (prevToken != null) {
                        // Use writeTokens which accepts a List, since its varargs version writes
                        // newlines after every call.
                        streamWriter.writeTokens(Arrays.asList(prevToken));
                    }

                    prevToken = token;
                    token = parser.parseNextToken();
                }

                if (prevToken != null) {
                    streamWriter.writeTokens(Arrays.asList(prevToken));
                }
            }

            if (oldStream instanceof PDPage) {
                // ((PDPage)oldStream).setContents(newStream);
                throw new UnsupportedOperationException(
                    "Editing page streams yet to be implemented"
                );

            } else if (oldStream instanceof PDFormXObject) {
                var stream = ((PDFormXObject)oldStream).getStream();

                try (var outStream = stream.createOutputStream(COSName.FLATE_DECODE)) {
                    newStream.getCOSObject().createInputStream().transferTo(outStream);
                }

            } else {
                throw new IllegalStateException("Unexpected stream type.");
            }
        }
    }
}