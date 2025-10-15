package com.mycompany.ths.controller;

import com.mycompany.ths.model.VitalSign;
import com.mycompany.ths.repo.VitalsService;
import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Session;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;

import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VitalsController {

    // ---------- FXML ----------
    @FXML private TextField pulseField;
    @FXML private TextField tempField;
    @FXML private TextField respField;
    @FXML private TextField bpField;

    @FXML private TableView<VitalSign> table;
    @FXML private TableColumn<VitalSign, String> colDate;
    @FXML private TableColumn<VitalSign, String> colPulse;
    @FXML private TableColumn<VitalSign, String> colTemp;
    @FXML private TableColumn<VitalSign, String> colResp;
    @FXML private TableColumn<VitalSign, String> colBP;

    @FXML private AnchorPane chartHost;

    // ---------- Services & constants ----------
    private final VitalsService service = new VitalsService();
    private static final PDFont FONT       = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    // ---------- Init ----------
    @FXML
    public void initialize() {
        // Bind columns defensively
        colPulse.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getPulse())));
        colTemp.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getTemperature())));
        colResp.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getRespiration())));
        colBP.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getBloodPressure())));
        colDate.setCellValueFactory(c -> {
            LocalDateTime at = c.getValue().getRecordedAt();
            return new ReadOnlyStringWrapper(at == null ? "" : at.toString());
        });

        refreshTable();
    }

    // ---------- Actions ----------
    @FXML
    private void saveVitals() {
        String user = Session.getUsername();
        if (user == null || user.isBlank()) { error("Please login again."); return; }

        try {
            int    pulse = Integer.parseInt(pulseField.getText().trim());
            double temp  = Double.parseDouble(tempField.getText().trim());
            int    resp  = Integer.parseInt(respField.getText().trim());
            String bp    = bpField.getText().trim();
            if (bp.isEmpty()) { info("Enter BP e.g., 120/80"); return; }

            Task<String> t = new Task<>() {
                @Override protected String call() throws Exception { return service.add(user, pulse, temp, resp, bp); }
            };
            t.setOnSucceeded(e -> {
                String res = t.getValue();
                if (res.startsWith("OK")) { info("Vitals saved."); clearForm(); refreshTable(); }
                else { error("Save failed: " + res); }
            });
            t.setOnFailed(e -> error("Network error: " + t.getException().getMessage()));
            new Thread(t, "add-vital").start();
        } catch (NumberFormatException ex) {
            error("Pulse/Temp/Respiration must be numeric.");
        }
    }

    @FXML private void backToDashboard() { Navigator.to("dashboard.fxml"); }

    private void refreshTable() {
        String user = Session.getUsername();
        if (user == null || user.isBlank()) {
            table.setItems(FXCollections.observableArrayList(new ArrayList<>()));
            return;
        }
        Task<List<VitalSign>> t = new Task<>() {
            @Override protected List<VitalSign> call() throws Exception { return service.listParsed(user); }
        };
        t.setOnSucceeded(e -> table.setItems(FXCollections.observableArrayList(t.getValue())));
        t.setOnFailed(e -> error("Load failed: " + t.getException().getMessage()));
        new Thread(t, "list-vitals").start();
    }

    // ---------- Chart ----------
    @FXML
    private void showTrends() {
        try {
            var user = Session.getUsername();
            if (user == null || user.isBlank()) { info("Please log in again."); return; }

            var rows = service.listParsed(user);
            int n = Math.min(30, rows.size());
            var recent = rows.subList(rows.size() - n, rows.size());

            var x = new CategoryAxis(); x.setLabel("Recorded at");
            var y = new NumberAxis();  y.setLabel("Value");

            var chart = new LineChart<String, Number>(x, y);
            chart.setLegendVisible(true);
            chart.setAnimated(false);
            chart.setCreateSymbols(false);
            chart.setTitle("Vital Trends");

            var sPulse = new XYChart.Series<String, Number>(); sPulse.setName("Pulse");
            var sTemp  = new XYChart.Series<String, Number>(); sTemp.setName("Temp °C");
            var sResp  = new XYChart.Series<String, Number>(); sResp.setName("Resp");

            var fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            for (var v : recent) {
                String t = v.getRecordedAt() == null ? "" : v.getRecordedAt().format(fmt);
                sPulse.getData().add(new XYChart.Data<>(t, v.getPulse()));
                sTemp.getData().add(new XYChart.Data<>(t, v.getTemperature()));
                sResp.getData().add(new XYChart.Data<>(t, v.getRespiration()));
            }
            chart.getData().setAll(sPulse, sTemp, sResp);

            chartHost.getChildren().setAll(chart);
            AnchorPane.setTopAnchor(chart, 0.0);
            AnchorPane.setRightAnchor(chart, 0.0);
            AnchorPane.setBottomAnchor(chart, 0.0);
            AnchorPane.setLeftAnchor(chart, 0.0);
        } catch (Exception e) {
            error("Trend load failed: " + e.getMessage());
        }
    }

    // ---------- PDF Export ----------
    @FXML
    private void exportPdf() {
        String user = Session.getUsername();
        if (user == null || user.isBlank()) { error("No active session."); return; }

        try {
            // Ensure a chart exists; if not, render it quickly
            if (chartHost.getChildren().isEmpty()) { showTrends(); }
            var nodeToSnap = chartHost.getChildren().isEmpty() ? null : chartHost.getChildren().get(0);

            // Snapshot to PNG temp file
            java.io.File chartPng = null;
            if (nodeToSnap != null) {
                var fxImg = nodeToSnap.snapshot(null, null);
                chartPng = java.io.File.createTempFile("vitals-chart-", ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(fxImg, null), "png", chartPng);
            }

            // Pull recent rows
            var rows = service.listParsed(user);
            int n = Math.min(30, rows.size());
            var recent = rows.subList(rows.size() - n, rows.size());

            // Build PDF
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float margin = 50f;
                    float y = page.getMediaBox().getHeight() - margin;

                    // Title
                    cs.beginText();
                    cs.setFont(FONT_BOLD, 18);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("THS Health Report — " + user);
                    cs.endText();
                    y -= 24;

                    // Date
                    cs.beginText();
                    cs.setFont(FONT, 11);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Generated: " + LocalDateTime.now());
                    cs.endText();
                    y -= 18;

                    // Chart image
                    if (chartPng != null && chartPng.exists()) {
                        var awtImg = ImageIO.read(chartPng);
                        var pdImg = LosslessFactory.createFromImage(doc, awtImg);
                        float maxW = page.getMediaBox().getWidth() - 2 * margin;
                        float imgW = pdImg.getWidth();
                        float imgH = pdImg.getHeight();
                        float scale = Math.min(1f, maxW / imgW);
                        float drawW = imgW * scale;
                        float drawH = imgH * scale;
                        y -= (drawH + 10);
                        cs.drawImage(pdImg, margin, y, drawW, drawH);
                        y -= 14;
                    }

                    // Section header
                    cs.beginText();
                    cs.setFont(FONT_BOLD, 12);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Recent Vitals (last " + n + ")");
                    cs.endText();
                    y -= 12;
                }

                // Table (separate helper so we can append/add pages)
                writeVitalsTable(doc, page, recent);

                // Save to Downloads
                String outPath = System.getProperty("user.home") + "/Downloads/THS_" + user + "_HealthReport.pdf";
                doc.save(outPath);
                info("PDF saved to: " + outPath);
            } finally {
                if (chartPng != null) chartPng.delete();
            }
        } catch (Exception e) {
            error("Export failed: " + e.getMessage());
        }
    }

    private void writeVitalsTable(PDDocument doc, PDPage startPage, List<VitalSign> rows) throws Exception {
        float margin = 50f, rowH = 16f;
        float[] widths = {120, 60, 60, 80, 90}; // Date, Pulse, Temp, Resp, BP
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        PDPage page = startPage;
        float y = page.getMediaBox().getHeight() - margin - 84; // below title/chart

        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            for (var v : rows) {
                if (y < margin + rowH) {
                    // new page
                    cs.close();
                    page = new PDPage(PDRectangle.LETTER);
                    doc.addPage(page);
                    y = page.getMediaBox().getHeight() - margin;

                    try (PDPageContentStream cs2 = new PDPageContentStream(doc, page)) {
                        y = drawVitalsRow(cs2, margin, y, rowH, widths, FONT, fmt, v);
                    }
                    continue;
                }
                y = drawVitalsRow(cs, margin, y, rowH, widths, FONT, fmt, v);
            }
        }
    }

    private float drawVitalsRow(PDPageContentStream cs,
                                float margin, float y, float rowH,
                                float[] widths, PDFont font,
                                DateTimeFormatter fmt, VitalSign v) throws Exception {

        String[] cols = {
            v.getRecordedAt() == null ? "" : v.getRecordedAt().format(fmt),
            String.valueOf(v.getPulse()),
            String.valueOf(v.getTemperature()),
            String.valueOf(v.getRespiration()),
            String.valueOf(v.getBloodPressure())
        };

        float x = margin;
        cs.setLineWidth(0.3f);
        for (int i = 0; i < cols.length; i++) {
            cs.addRect(x, y, widths[i], rowH);
            cs.stroke();
            cs.beginText();
            cs.setFont(font, 10);
            cs.newLineAtOffset(x + 2, y + 4);
            cs.showText(cols[i]);
            cs.endText();
            x += widths[i];
        }
        return y - rowH;
    }

    // ---------- Helpers ----------
    private void clearForm() {
        pulseField.clear();
        tempField.clear();
        respField.clear();
        bpField.clear();
    }
    private static String nz(String s) { return s == null ? "" : s; }
    private void info(String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void error(String m) { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
}
