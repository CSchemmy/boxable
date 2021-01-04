package be.quodlibet.boxable.page;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class NewPageProviderImpl implements PageProvider<PDPage> {

    private final PDDocument document;

    private final PDRectangle size;

    private final BoundingBox headerSize;

    private final BoundingBox footerSize;

    private int currentPageIndex = -1;

    private NewPageInitializer newPageInitializer;

    public NewPageProviderImpl(final PDDocument document, final PDRectangle size, PDPage currentPage, NewPageInitializer newPageInitializer, BoundingBox headerSize, BoundingBox footerSize) {
        this.document = document;
        this.size = size;
        this.headerSize = headerSize;
        this.footerSize = footerSize;
        this.newPageInitializer = newPageInitializer;
        initNewPage(currentPage, 0);
    }

    @Override
    public PDDocument getDocument() {
        return document;
    }

    @Override
    public PDPage createPage() {
        currentPageIndex = document.getNumberOfPages();
        return getCurrentPage();
    }

    @Override
    public PDPage nextPage() {
        if (currentPageIndex == -1) {
            currentPageIndex = document.getNumberOfPages();
        } else {
            currentPageIndex++;
        }

        return getCurrentPage();
    }

    @Override
    public PDPage previousPage() {
        currentPageIndex--;
        if (currentPageIndex < 0) {
            currentPageIndex = 0;
        }

        return getCurrentPage();
    }

    private PDPage getCurrentPage() {
        if (currentPageIndex >= document.getNumberOfPages()) {
            final PDPage newPage = new PDPage(size);
            initNewPage(newPage, currentPageIndex);

            document.addPage(newPage);
            return newPage;
        }

        return document.getPage(currentPageIndex);
    }

    private void initNewPage(PDPage currentPage, int index) {
        if (this.newPageInitializer != null) {
            this.newPageInitializer.initNewPage(document, currentPage, index,headerSize,footerSize);
        }
    }

}
