package be.quodlibet.boxable.page;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public interface NewPageInitializer {
    void initNewPage(PDDocument document, PDPage currentPage, int pageNumber, BoundingBox headerSize, BoundingBox footerSize);
}
