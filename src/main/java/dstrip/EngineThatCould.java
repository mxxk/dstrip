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
    PDContentStream envelopeMarkerStream;
    Integer envelopeMarkerColor;
    Integer watermarkColor;
    int skipTextBlocks;
    int watermarkTextBlocks;

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

    public int getSkipTextBlocks() {
        return skipTextBlocks;
    }

    public int getWatermarkTextBlocks() {
        return watermarkTextBlocks;
    }

    public PDContentStream getEnvelopeMarkerStream() {
        return envelopeMarkerStream;
    }

    @Override
    public void processPage(PDPage page) throws IOException {
        resetState();
        beginStream(page);
        super.processPage(page);
        streamStack.pop();
    }

    @Override
    public void showForm(PDFormXObject form) throws IOException {
        beginStream(form);
        super.showForm(form);
        streamStack.pop();
    }

    @Override
    protected void showText(byte[] string) throws IOException {
        if (envelopeMarkerColor == null) {
            if (new String(string).startsWith("DocuSign Envelope ID: ")) {
                envelopeMarkerColor = getTextColor();
                // Find the stream to which this text belongs.
                envelopeMarkerStream = streamStack.peekFirst();
            }

            skipTextBlocks++;

        } else {
            if (watermarkColor == null) {
                watermarkColor = getTextColor();

                if (watermarkColor == envelopeMarkerColor) {
                    throw new IllegalStateException("Invalid state encountered");
                }

                // This is the first part of the watermark to remove.
                // Increment text counter.
                watermarkTextBlocks++;

            } else if (getTextColor() == watermarkColor) {
                // Text color is the same; must be a continuation of the watermark.
                // Increment text counter.
                watermarkTextBlocks++;

            } else {
                // The color is not the same; the watermark must have ended. Set watermark
                // color to an impossible value to prevent further matching.
                watermarkColor = -1;
            }
        }
    }

    private int getTextColor() throws IOException {
        return getGraphicsState().getNonStrokingColor().toRGB();
    }

    private void beginStream(PDContentStream stream) {
        streamStack.push(stream);
        skipTextBlocks = 0;
    }

    private void resetState() {
        streamStack = new LinkedList<>();
        envelopeMarkerColor = null;
        watermarkColor = null;
        skipTextBlocks = 0;
        watermarkTextBlocks = 0;
    }
}