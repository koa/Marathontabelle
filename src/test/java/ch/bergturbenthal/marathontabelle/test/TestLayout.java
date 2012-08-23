package ch.bergturbenthal.marathontabelle.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class TestLayout {

  /**
   * @param args
   * @throws DocumentException
   * @throws FileNotFoundException
   */
  public static void main(final String[] args) throws FileNotFoundException, DocumentException {

    final Document document = new Document(PageSize.A4);
    final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File("target/test.pdf")));
    document.open();
    final Paragraph titleParagraph = new Paragraph("Phase A");
    titleParagraph.setAlignment(Element.ALIGN_CENTER);
    titleParagraph.setSpacingAfter(20);
    document.add(titleParagraph);

    final PdfPTable timesTable = new PdfPTable(2);
    timesTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    timesTable.getDefaultCell().setBackgroundColor(BaseColor.YELLOW);
    timesTable.addCell("Startzeit");
    timesTable.addCell("10:35:00 Uhr");
    timesTable.addCell("Ankunft Min");
    timesTable.addCell("10:42:14 Uhr");
    timesTable.addCell("Ankunft Max");
    timesTable.addCell("10:44:14 Uhr");
    timesTable.setWidthPercentage(30);
    timesTable.setSpacingAfter(20);
    document.add(timesTable);

    // a table with three columns
    final PdfPTable table = new PdfPTable(7);

    // we add a cell with colspan 3
    // the cell object
    final PdfPCell titleCell = new PdfPCell(new Phrase("Phase A"));
    titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    titleCell.setColspan(7);
    titleCell.setBorder(Rectangle.NO_BORDER);
    table.addCell(titleCell);
    table.completeRow();

    // now we add a cell with rowspan 2
    final PdfPCell cell = new PdfPCell(new Phrase("Cell with rowspan 2"));
    cell.setRowspan(2);
    table.addCell(cell);
    table.completeRow();
    // we add the four remaining cells with addCell()
    table.addCell("row 1; cell 1");
    table.addCell("row 1; cell 2");
    table.addCell("row 2; cell 1");
    table.addCell("row 2; cell 2");
    document.add(table);
    // PdfContentByte directContent = writer.getDirectContent();

    document.close();

  }
}
