package com.bainaryglobe;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class PDFProcessor {

    private float ratio;
    private int boldLevel;

    public PDFProcessor(float ratio, int boldLevel) {
        this.ratio = ratio;
        this.boldLevel = boldLevel;
    }

    public void processPDF(String filePath) throws IOException {
        PDDocument document = PDDocument.load(new File(filePath));
        PDDocument newDocument = new PDDocument();

        for (PDPage page : document.getPages()) {
            PDPage newPage = new PDPage();
            newDocument.addPage(newPage);

            // Copy non-text elements from old page to new page
            copyNonTextElements(page, newPage);

            // Process text and add to new page
            processText(page, newPage);
        }

        newDocument.save(filePath.replace(".pdf", "_modified.pdf"));
        newDocument.close();
        document.close();
    }

    private void copyNonTextElements(PDPage oldPage, PDPage newPage) {
        PDPageContentStream stream = new PDPageContentStream(newDocument, newPage);
        List<PDRectangle> regions = extractNonTextRegions(oldPage);

        for (PDRectangle region : regions) {
            stream.drawRectangle(region.getX(), region.getY(), region.getWidth(), region.getHeight());
        }

        stream.close();
    }

    private List<PDRectangle> extractNonTextRegions(PDPage page) {
        List<PDRectangle> regions = new ArrayList<>();

        for (PDRectangle shape : page.getShapes()) {
            if (shape.getClosable()) {
                regions.add(shape.getBoundingBox());
            }
        }

        for (PDImage image : page.getImages()) {
            regions.add(image.getBoundingBox());
        }

        return regions;
    }

    private void processText(PDPage oldPage, PDPage newPage) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        String text = textStripper.getText(new PDDocument().addPage(oldPage));

        List<String> lines = splitTextIntoLines(text);
        for (String line : lines) {
            List<String> words = splitLineIntoWords(line);
            for (String word : words) {
                applyBoldFormatting(word, newPage);
            }
        }
    }

    private List<String> splitTextIntoLines(String text) {
        return Arrays.asList(text.split("\n"));
    }

    private List<String> splitLineIntoWords(String line) {
        return Arrays.asList(line.split("\\s+"));
    }

    private void applyBoldFormatting(String word, PDPage newPage) {
        int boldLength = Math.round(word.length() * ratio);
        PDType1Font boldFont = (PDType1Font) PDType1Font.BOLD;
        PDType1Font normalFont = (PDType1Font) PDType1Font.STANDARD;

        for (int i = 0; i < boldLength; i++) {
            newPage.setFont(boldFont);
            newPage.showText(word.charAt(i) + "");
        }

        for (int i = boldLength; i < word.length(); i++) {
            newPage.setFont(normalFont);
            newPage.showText(word.charAt(i) + "");
        }
    }

    public static void main(String[] args) {
        float ratio = Float.parseFloat(args[0]);
        int boldLevel = Integer.parseInt(args[1]);
        String filePath = args[2];

        PDFProcessor processor = new PDFProcessor(ratio, boldLevel);
        try {
            processor.processPDF(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}













