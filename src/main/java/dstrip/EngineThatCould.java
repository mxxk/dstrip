package dstrip;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.contentstream.operator.text.BeginText;
import org.apache.pdfbox.contentstream.operator.text.EndText;
import org.apache.pdfbox.contentstream.operator.text.MoveText;
import org.apache.pdfbox.contentstream.operator.text.MoveTextSetLeading;
import org.apache.pdfbox.contentstream.operator.text.NextLine;
import org.apache.pdfbox.contentstream.operator.text.SetCharSpacing;
import org.apache.pdfbox.contentstream.operator.text.SetFontAndSize;
import org.apache.pdfbox.contentstream.operator.text.SetTextHorizontalScaling;
import org.apache.pdfbox.contentstream.operator.text.SetTextLeading;
import org.apache.pdfbox.contentstream.operator.text.SetTextRenderingMode;
import org.apache.pdfbox.contentstream.operator.text.SetTextRise;
import org.apache.pdfbox.contentstream.operator.text.SetWordSpacing;
import org.apache.pdfbox.contentstream.operator.text.ShowText;
import org.apache.pdfbox.contentstream.operator.text.ShowTextAdjusted;
import org.apache.pdfbox.contentstream.operator.text.ShowTextLine;
import org.apache.pdfbox.contentstream.operator.text.ShowTextLineAndSpace;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;


class EngineThatCould extends PDFStreamEngine {
    // Stack of streams to keep track of which stream is currently being processed.
    LinkedList<PDContentStream> streamStack;

    PDContentStream watermarkStream;
    Integer watermarkColor;
    // Index of current text block.
    int textBlockIndex;
    // Index of first text block which is part of the watermark.
    int watermarkStartIndex;
    // Index of last text block which is part of the watermark.
    int watermarkEndIndex;

    public EngineThatCould() throws IOException {
        super();
        resetState();
        addOperator(new BeginText());
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new EndText());
        addOperator(new MoveText());
        addOperator(new MoveTextSetLeading());
        addOperator(new NextLine());
        addOperator(new Restore());
        addOperator(new Save());
        addOperator(new SetCharSpacing());
        addOperator(new SetFontAndSize());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new SetMatrix());
        addOperator(new SetNonStrokingColor());
        addOperator(new SetNonStrokingColorN());
        addOperator(new SetTextHorizontalScaling());
        addOperator(new SetTextLeading());
        addOperator(new SetTextRenderingMode());
        addOperator(new SetTextRise());
        addOperator(new SetWordSpacing());
        addOperator(new ShowText());
        addOperator(new ShowTextAdjusted());
        addOperator(new ShowTextLine());
        addOperator(new ShowTextLineAndSpace());
    }

    public int getWatermarkStartIndex() {
        return watermarkStartIndex;
    }

    public int getWatermarkEndIndex() {
        return watermarkEndIndex;
    }

    public PDContentStream getWatermarkStream() {
        return watermarkStream;
    }

    @Override
    public void processPage(PDPage page) throws IOException {
        resetState();
        beginStream(page);
        super.processPage(page);
        endStream();
    }

    @Override
    public void showForm(PDFormXObject form) throws IOException {
        beginStream(form);
        super.showForm(form);
        endStream();
    }

    @Override
    protected void showText(byte[] string) throws IOException {
        if (watermarkColor == null) {
            // If its color is not saved, the start of the watermark is yet to be found.
            if (new String(string).startsWith("DEMONSTRATION DOCUMENT ONLY")) {
                watermarkColor = getTextColor();
                watermarkStartIndex = textBlockIndex;
                // Save the stream to which the watermark belongs.
                watermarkStream = streamStack.peekFirst();
            }

        } else if (watermarkEndIndex == -1) {
            if (getTextColor() != watermarkColor) {
                // If the current text block has a differnet color than the watermark, then it marks
                // the end of the watermark.
                watermarkEndIndex = textBlockIndex;
            }
        }

        textBlockIndex++;
    }

    private int getTextColor() throws IOException {
        return getGraphicsState().getNonStrokingColor().toRGB();
    }

    private void beginStream(PDContentStream stream) {
        streamStack.push(stream);
        // Since entering a new stream, reset the text block index.
        textBlockIndex = 0;
    }

    private void endStream() {
        streamStack.pop();

        if (watermarkStartIndex >= 0 && watermarkEndIndex == -1) {
            // If watermark was found but was the last text on the page, assign the end index
            // manually.
            watermarkEndIndex = textBlockIndex;
        }
    }

    private void resetState() {
        streamStack = new LinkedList<>();
        watermarkColor = null;
        textBlockIndex = -1;
        watermarkStartIndex = -1;
        watermarkEndIndex = -1;
    }
}