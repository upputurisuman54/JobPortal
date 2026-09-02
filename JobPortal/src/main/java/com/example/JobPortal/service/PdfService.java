package com.example.JobPortal.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfService {

    private static final float MARGIN_X = 42;
    private static final float MARGIN_TOP = 32;
    private static final float MARGIN_BOTTOM = 28;
    private static final float MIN_SCALE = 0.55f;

    private static final float NAME_SIZE = 17;
    private static final float NAME_LEADING = 20;
    private static final float CONTACT_SIZE = 8.6f;
    private static final float CONTACT_LEADING = 11;
    private static final float HEADER_SIZE = 10.5f;
    private static final float HEADER_LEADING = 13;
    private static final float HEADER_SPACE_BEFORE = 7;
    private static final float RULE_SPACE_AFTER = 5;
    private static final float BODY_SIZE = 8.8f;
    private static final float BODY_LEADING = 11.3f;
    private static final float BODY_SPACE_AFTER = 2;
    private static final float BULLET_SIZE = 8.6f;
    private static final float BULLET_LEADING = 10.8f;
    private static final float BULLET_SPACE_AFTER = 1;
    private static final float BULLET_INDENT = 12;
    private static final float BOLD_LINE_SIZE = 9;
    private static final float BOLD_LINE_LEADING = 11.3f;
    private static final float BOLD_SPACE_BEFORE = 3;
    private static final float ITALIC_SIZE = 8.6f;
    private static final float ITALIC_LEADING = 11;
    private static final float ITALIC_SPACE_AFTER = 1.5f;
    private static final float BLANK_HEIGHT = 4;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w./#?&=%-]+)|((?:www\\.)?(?:linkedin\\.com|github\\.com)[\\w./#?&=%-]*)"
    );

    private final PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDType1Font obliqueFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    private enum LineType {
        NAME, CONTACT, HEADER, BOLD_LINE, ITALIC_LINE, BULLET, BODY, BLANK
    }

    private static class RawLine {
        LineType type;
        String text;

        RawLine(LineType type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    private static class LayoutResult {
        float consumedHeight;

        LayoutResult(float consumedHeight) {
            this.consumedHeight = consumedHeight;
        }
    }

    public byte[] generatePdf(String title, String content) {
        try (PDDocument document = new PDDocument()) {
            List<RawLine> rawLines = parseContent(title, content);

            float pageHeight = PDRectangle.A4.getHeight();
            float availableHeight = pageHeight - MARGIN_TOP - MARGIN_BOTTOM;

            float chosenScale = MIN_SCALE;
            boolean fits = false;

            for (float s = 1.0f; s >= MIN_SCALE - 0.001f; s -= 0.05f) {
                LayoutResult probe = layout(rawLines, s, null, false);
                if (probe.consumedHeight <= availableHeight) {
                    chosenScale = s;
                    fits = true;
                    break;
                }
            }

            if (!fits) {
                chosenScale = MIN_SCALE;
            }

            layout(rawLines, chosenScale, document, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private String stripInlineBold(String text) {
        if (text == null) {
            return text;
        }
        return text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
    }

    private List<RawLine> parseContent(String title, String content) {
        List<RawLine> raw = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            raw.add(new RawLine(LineType.NAME, stripInlineBold(title.trim())));
            raw.add(new RawLine(LineType.BLANK, ""));
        }

        String[] lines = content.split("\n", -1);
        boolean sawName = false;
        boolean inContactBlock = false;

        for (String rawLine : lines) {
            String trimmed = rawLine.trim();

            if (!sawName) {
                if (trimmed.isEmpty()) {
                    continue;
                }
                raw.add(new RawLine(LineType.NAME, stripInlineBold(trimmed)));
                sawName = true;
                inContactBlock = true;
                continue;
            }

            if (inContactBlock) {
                if (!trimmed.isEmpty() && trimmed.contains("|")) {
                    raw.add(new RawLine(LineType.CONTACT, stripInlineBold(trimmed)));
                    continue;
                } else {
                    inContactBlock = false;
                }
            }

            if (trimmed.isEmpty()) {
                raw.add(new RawLine(LineType.BLANK, ""));
                continue;
            }

            if (trimmed.matches("-{3,}") || trimmed.matches("_{3,}")) {
                continue;
            }

            if (trimmed.startsWith("## ")) {
                raw.add(new RawLine(LineType.HEADER, stripInlineBold(trimmed.substring(3).trim())));
                continue;
            }

            if (trimmed.startsWith("# ")) {
                raw.add(new RawLine(LineType.HEADER, stripInlineBold(trimmed.substring(2).trim())));
                continue;
            }

            if (trimmed.length() > 4 && trimmed.startsWith("**") && trimmed.endsWith("**")) {
                raw.add(new RawLine(LineType.BOLD_LINE, trimmed.substring(2, trimmed.length() - 2).trim()));
                continue;
            }

            if (trimmed.length() > 2 && trimmed.startsWith("_") && trimmed.endsWith("_")) {
                raw.add(new RawLine(LineType.ITALIC_LINE, trimmed.substring(1, trimmed.length() - 1).trim()));
                continue;
            }

            if (trimmed.startsWith("\u2022 ") || trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                raw.add(new RawLine(LineType.BULLET, stripInlineBold(trimmed.substring(2).trim())));
                continue;
            }

            boolean looksLikeHeader = trimmed.equals(trimmed.toUpperCase())
                    && trimmed.matches("[A-Z0-9 &/,.'-]{2,40}")
                    && trimmed.chars().anyMatch(Character::isLetter);

            if (looksLikeHeader) {
                raw.add(new RawLine(LineType.HEADER, stripInlineBold(trimmed)));
                continue;
            }

            raw.add(new RawLine(LineType.BODY, stripInlineBold(trimmed)));
        }

        return raw;
    }

    private LayoutResult layout(List<RawLine> rawLines, float scale, PDDocument document, boolean draw) throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        float pageHeight = PDRectangle.A4.getHeight();
        float printableWidth = pageWidth - 2 * MARGIN_X;
        float topY = pageHeight - MARGIN_TOP;
        float bottomY = MARGIN_BOTTOM;

        PDPage page = null;
        PDPageContentStream cs = null;

        if (draw) {
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            cs = new PDPageContentStream(document, page);
        }

        float y = topY;

        for (RawLine r : rawLines) {
            switch (r.type) {

                case BLANK: {
                    y -= BLANK_HEIGHT * scale;
                    break;
                }

                case NAME: {
                    float size = NAME_SIZE * scale;
                    List<String> wrapped = wrapText(r.text, boldFont, size, printableWidth);
                    for (String line : wrapped) {
                        if (draw) {
                            if (y < bottomY) {
                                cs.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                cs = new PDPageContentStream(document, page);
                                y = topY;
                            }
                            float tw = boldFont.getStringWidth(line) / 1000 * size;
                            float x = MARGIN_X + (printableWidth - tw) / 2f;
                            cs.beginText();
                            cs.setFont(boldFont, size);
                            cs.newLineAtOffset(x, y);
                            cs.showText(line);
                            cs.endText();
                        }
                        y -= NAME_LEADING * scale;
                    }
                    break;
                }

                case CONTACT: {
                    float size = CONTACT_SIZE * scale;
                    List<String> wrapped = wrapText(r.text, regularFont, size, printableWidth);
                    for (String line : wrapped) {
                        if (draw) {
                            if (y < bottomY) {
                                cs.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                cs = new PDPageContentStream(document, page);
                                y = topY;
                            }
                            float tw = regularFont.getStringWidth(line) / 1000 * size;
                            float x = MARGIN_X + (printableWidth - tw) / 2f;
                            cs.beginText();
                            cs.setFont(regularFont, size);
                            cs.newLineAtOffset(x, y);
                            cs.showText(line);
                            cs.endText();
                            addLinkAnnotations(page, line, x, y, size, regularFont);
                        }
                        y -= CONTACT_LEADING * scale;
                    }
                    break;
                }

                case HEADER: {
                    y -= HEADER_SPACE_BEFORE * scale;
                    float size = HEADER_SIZE * scale;
                    List<String> wrapped = wrapText(r.text.toUpperCase(), boldFont, size, printableWidth);
                    for (String line : wrapped) {
                        if (draw) {
                            if (y < bottomY) {
                                cs.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                cs = new PDPageContentStream(document, page);
                                y = topY;
                            }
                            cs.beginText();
                            cs.setFont(boldFont, size);
                            cs.newLineAtOffset(MARGIN_X, y);
                            cs.showText(line);
                            cs.endText();
                        }
                        y -= HEADER_LEADING * scale;
                    }
                    if (draw) {
                        if (y < bottomY) {
                            cs.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            cs = new PDPageContentStream(document, page);
                            y = topY;
                        }
                        float ruleY = y + (HEADER_LEADING * scale * 0.3f);
                        cs.setLineWidth(0.8f);
                        cs.moveTo(MARGIN_X, ruleY);
                        cs.lineTo(MARGIN_X + printableWidth, ruleY);
                        cs.stroke();
                    }
                    y -= RULE_SPACE_AFTER * scale;
                    break;
                }

                case BOLD_LINE: {
                    y -= BOLD_SPACE_BEFORE * scale;
                    float size = BOLD_LINE_SIZE * scale;
                    List<String> wrapped = wrapText(r.text, boldFont, size, printableWidth);
                    for (String line : wrapped) {
                        if (draw) {
                            if (y < bottomY) {
                                cs.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                cs = new PDPageContentStream(document, page);
                                y = topY;
                            }
                            cs.beginText();
                            cs.setFont(boldFont, size);
                            cs.newLineAtOffset(MARGIN_X, y);
                            cs.showText(line);
                            cs.endText();
                        }
                        y -= BOLD_LINE_LEADING * scale;
                    }
                    break;
                }

                case ITALIC_LINE: {
                    float size = ITALIC_SIZE * scale;
                    List<String> wrapped = wrapText(r.text, obliqueFont, size, printableWidth);
                    for (String line : wrapped) {
                        if (draw) {
                            if (y < bottomY) {
                                cs.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                cs = new PDPageContentStream(document, page);
                                y = topY;
                            }
                            cs.beginText();
                            cs.setFont(obliqueFont, size);
                            cs.newLineAtOffset(MARGIN_X, y);
                            cs.showText(line);
                            cs.endText();
                        }
                        y -= ITALIC_LEADING * scale;
                    }
                    y -= ITALIC_SPACE_AFTER * scale;
                    break;
                }

                case BULLET: {
                    float size = BULLET_SIZE * scale;
                    float textWidth = printableWidth - BULLET_INDENT * scale;
                    List<String> wrapped = wrapText(r.text, regularFont, size, textWidth);
                    boolean first = true;
                    for (String line : wrapped) {
                        if (draw) {
                            if (y < bottomY) {
                                cs.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                cs = new PDPageContentStream(document, page);
                                y = topY;
                            }
                            if (first) {
                                cs.beginText();
                                cs.setFont(regularFont, size);
                                cs.newLineAtOffset(MARGIN_X, y);
                                cs.showText("\u2022");
                                cs.endText();
                            }
                            cs.beginText();
                            cs.setFont(regularFont, size);
                            cs.newLineAtOffset(MARGIN_X + BULLET_INDENT * scale, y);
                            cs.showText(line);
                            cs.endText();
                            addLinkAnnotations(page, line, MARGIN_X + BULLET_INDENT * scale, y, size, regularFont);
                        }
                        y -= BULLET_LEADING * scale;
                        first = false;
                    }
                    y -= BULLET_SPACE_AFTER * scale;
                    break;
                }

                case BODY: {
                    float size = BODY_SIZE * scale;
                    List<String> wrapped = wrapText(r.text, regularFont, size, printableWidth);
                    for (String line : wrapped) {
                        if (draw) {
                            if (y < bottomY) {
                                cs.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                cs = new PDPageContentStream(document, page);
                                y = topY;
                            }
                            cs.beginText();
                            cs.setFont(regularFont, size);
                            cs.newLineAtOffset(MARGIN_X, y);
                            cs.showText(line);
                            cs.endText();
                            addLinkAnnotations(page, line, MARGIN_X, y, size, regularFont);
                        }
                        y -= BODY_LEADING * scale;
                    }
                    y -= BODY_SPACE_AFTER * scale;
                    break;
                }
            }
        }

        if (draw) {
            cs.close();
        }

        float consumed = topY - y;
        return new LayoutResult(consumed);
    }

    private void addLinkAnnotations(PDPage page, String text, float xStart, float y, float size, PDType1Font font) throws IOException {
        addLinksForPattern(page, text, xStart, y, size, font, EMAIL_PATTERN, true);
        addLinksForPattern(page, text, xStart, y, size, font, URL_PATTERN, false);
    }

    private void addLinksForPattern(PDPage page, String text, float xStart, float y, float size,
                                     PDType1Font font, Pattern pattern, boolean isEmail) throws IOException {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String matched = matcher.group();
            if (matched == null || matched.isBlank()) {
                continue;
            }

            float preWidth = font.getStringWidth(text.substring(0, matcher.start())) / 1000 * size;
            float matchWidth = font.getStringWidth(matched) / 1000 * size;

            PDRectangle rect = new PDRectangle();
            rect.setLowerLeftX(xStart + preWidth);
            rect.setLowerLeftY(y - 2);
            rect.setUpperRightX(xStart + preWidth + matchWidth);
            rect.setUpperRightY(y + size);

            PDAnnotationLink link = new PDAnnotationLink();
            link.setRectangle(rect);

            PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
            borderStyle.setWidth(0);
            link.setBorderStyle(borderStyle);

            PDActionURI action = new PDActionURI();
            if (isEmail) {
                action.setURI("mailto:" + matched);
            } else {
                String target = matched;
                if (!target.toLowerCase().startsWith("http")) {
                    target = "https://" + target;
                }
                action.setURI(target);
            }
            link.setAction(action);

            page.getAnnotations().add(link);
        }
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();

        if (text.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            float width = font.getStringWidth(candidate) / 1000 * fontSize;

            if (width > maxWidth && !currentLine.isEmpty()) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(candidate);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}