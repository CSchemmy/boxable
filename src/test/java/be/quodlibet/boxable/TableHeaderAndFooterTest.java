/*
 Quodlibet.be
 */
package be.quodlibet.boxable;

import be.quodlibet.boxable.page.NewPageInitializer;
import be.quodlibet.boxable.page.NewPageProviderImpl;
import be.quodlibet.boxable.page.PageProvider;
import be.quodlibet.boxable.utils.FontUtils;
import be.quodlibet.boxable.utils.ImageUtils;
import be.quodlibet.boxable.utils.PDStreamUtils;
import be.quodlibet.boxable.utils.PageContentStreamOptimized;
import com.google.common.io.Files;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TableHeaderAndFooterTest {
    @Test
    public void SampleHeaderAndFooter() throws IOException {

        // Set margins
        final float margin = 10;
        float pageHeaderMargin = 30;

        List<String[]> facts = getFacts();

        // Initialize Document
        PDDocument doc = new PDDocument();
        final PDPage page = addNewPage(doc);
        float yStartNewPage = page.getMediaBox().getHeight() - (margin);

        // Initialize table
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
        boolean drawContent = true;
        float yStart = yStartNewPage - pageHeaderMargin;
        float footerMargin = 15;
        float tableBottomMargin = footerMargin + margin;

        NewPageInitializer newPageInitializer = getNewPageInitializer();
        PageProvider<PDPage> pageProvider = new NewPageProviderImpl(doc, page.getMediaBox(), page, newPageInitializer,
                new BoundingBox(margin, yStart
                        , page.getMediaBox().getWidth() - margin
                        , page.getMediaBox().getHeight() - margin),
                new BoundingBox(margin, margin
                        , page.getMediaBox().getWidth() - margin
                        , tableBottomMargin)
        );

        BaseTable table = new BaseTable(yStart, yStartNewPage, pageHeaderMargin, tableBottomMargin, tableWidth, margin, doc, page, true,
                drawContent, pageProvider);

        // Create Header row
        Row<PDPage> headerRow = table.createRow(15f);
        Cell<PDPage> cell = headerRow.createCell(100, "Awesome Facts About Belgium");
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFillColor(Color.BLACK);
        cell.setTextColor(Color.WHITE);

        table.addHeaderRow(headerRow);

        // Create 2 column row
        Row<PDPage> row = table.createRow(15f);
        cell = row.createCell(30, "Source:");
        cell.setFont(PDType1Font.HELVETICA);

        cell = row.createCell(70, "http://www.factsofbelgium.com/");
        cell.setFont(PDType1Font.HELVETICA_OBLIQUE);

        // Create Fact header row
        Row<PDPage> factHeaderrow = table.createRow(15f);

        cell = factHeaderrow.createCell((100 / 3f) * 2, "Fact");
        cell.setFont(PDType1Font.HELVETICA);
        cell.setFontSize(6);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell((100 / 3f), "Tags");
        cell.setFillColor(Color.LIGHT_GRAY);
        cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
        cell.setFontSize(6);

        // Add multiple rows with random facts about Belgium
        for (String[] fact : facts) {

            row = table.createRow(10f);
            cell = row.createCell((100 / 3f) * 2, fact[0]);
            cell.setFont(PDType1Font.HELVETICA);
            cell.setFontSize(6);

            for (int i = 1; i < fact.length; i++) {
                if (fact[i].startsWith("image:")) {
                    File imageFile;
                    try {
                        imageFile = new File(
                                TableTest.class.getResource("/" + fact[i].substring("image:".length())).toURI());
                        row.createImageCell((100 / 9f), ImageUtils.readImage(imageFile));
                    } catch (final URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    cell = row.createCell((100 / 9f), fact[i]);
                    cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
                    cell.setFontSize(6);
                    // Set colors
                    if (fact[i].contains("beer"))
                        cell.setFillColor(Color.yellow);
                    if (fact[i].contains("champion"))
                        cell.setTextColor(Color.GREEN);
                }
            }
        }

        table.draw();

        // Close Stream and save pdf
        File file = new File("target/BoxableSamplePageHeaderAndFooter.pdf");
        System.out.println("Sample file saved at : " + file.getAbsolutePath());
        Files.createParentDirs(file);
        doc.save(file);
        doc.close();

    }

    private NewPageInitializer getNewPageInitializer(){
       return new NewPageInitializer() {
            @Override
            public void initNewPage(PDDocument document, PDPage currentPage, int pageNumber, BoundingBox headerSize, BoundingBox bottomSize) {
                try {
                    PageContentStreamOptimized cos = new PageContentStreamOptimized(new PDPageContentStream(document, currentPage));
                    float counterLength = FontUtils.getStringWidth(PDType1Font.HELVETICA, "Page " + (pageNumber + 1), 8);
                    PDStreamUtils.write(cos, "Page " + (pageNumber + 1), PDType1Font.HELVETICA, 8, bottomSize.getUpperRightX() - counterLength, bottomSize.getLowerLeftY() + 8,
                            Color.BLACK);
                    PDStreamUtils.write(cos, "Left in the footer", PDType1Font.HELVETICA, 8, bottomSize.getLowerLeftX(), bottomSize.getLowerLeftY() + 8,
                            Color.BLACK);
                    cos.close();
                    final float margin = headerSize.getLowerLeftX();
                    float yStartNewPage = headerSize.getUpperRightY();
                    float yStart = yStartNewPage;
                    float tableWidth = headerSize.getWidth();
                    boolean drawContent = true;
                    BaseTable table = new BaseTable(yStart, yStartNewPage, 2 * margin, tableWidth, margin, document, currentPage, true,
                            drawContent);
                    Row<PDPage> headerRow = table.createRow(15f);
                    Cell<PDPage> cell = headerRow.createCell(100 , "PageHeader");
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFillColor(Color.WHITE);
                    cell.setTextColor(Color.BLACK);
                    cell.setValign(VerticalAlignment.MIDDLE);
                    cell.setAlign(HorizontalAlignment.CENTER);
                    headerRow.removeAllBorders();
                    table.addHeaderRow(headerRow);
                    table.draw();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

    }

    private static List<String[]> getFacts() {
        List<String[]> facts = new ArrayList<>();
        facts.add(new String[]{"Oil Painting was invented by the Belgian van Eyck brothers", "art", "inventions",
                "science"});
        facts.add(new String[]{"The Belgian Adolphe Sax invented the Saxophone", "inventions", "music", ""});
        facts.add(new String[]{"11 sites in Belgium are on the UNESCO World Heritage List", "art", "history", ""});
        facts.add(new String[]{"Belgium was the second country in the world to legalize same-sex marriage",
                "politics", "image:150dpi.png", ""});
        facts.add(new String[]{"In the seventies, schools served light beer during lunch", "health", "school",
                "beer"});
        facts.add(new String[]{"Belgium has the sixth fastest domestic internet connection in the world", "science",
                "technology", ""});
        facts.add(new String[]{"Belgium hosts the World's Largest Sand Sculpture Festival", "art", "festivals",
                "world championship"});
        facts.add(
                new String[]{"Belgium has compulsary education between the ages of 6 and 18", "education", "", ""});
        facts.add(new String[]{
                "Belgium also has more comic makers per square kilometer than any other country in the world", "art",
                "social", "world championship"});
        facts.add(new String[]{
                "Belgium has one of the lowest proportion of McDonald's restaurants per inhabitant in the developed world",
                "food", "health", ""});
        facts.add(new String[]{"Belgium has approximately 178 beer breweries", "beer", "food", ""});
        facts.add(new String[]{"Gotye was born in Bruges, Belgium", "music", "celebrities", ""});
        facts.add(new String[]{"The Belgian Coast Tram is the longest tram line in the world", "technology",
                "world championship", ""});
        facts.add(new String[]{"Stefan Everts is the only motocross racer with 10 World Championship titles.",
                "celebrities", "sports", "world champions"});
        facts.add(new String[]{"Tintin was conceived by Belgian artist Hergé", "art", "celebrities", "inventions"});
        facts.add(new String[]{"Brussels Airport is the world's biggest selling point of chocolate", "food",
                "world champions", ""});
        facts.add(new String[]{"Tomorrowland is the biggest electronic dance music festival in the world",
                "festivals", "music", "world champion"});
        facts.add(new String[]{"French Fries are actually from Belgium", "food", "inventions", "image:300dpi.png"});
        facts.add(new String[]{"Herman Van Rompy is the first full-time president of the European Council",
                "politics", "", ""});
        facts.add(new String[]{"Belgians are the fourth most money saving people in the world", "economy", "social",
                ""});
        facts.add(new String[]{
                "The Belgian highway system is the only man-made structure visible from the moon at night",
                "technology", "world champions", ""});
        facts.add(new String[]{"Andreas Vesalius, the founder of modern human anatomy, is from Belgium",
                "celebrities", "education", "history"});
        facts.add(
                new String[]{"Napoleon was defeated in Waterloo, Belgium", "celebrities", "history", "politicians"});
        facts.add(new String[]{
                "The first natural color picture in National Geographic was of a flower garden in Gent, Belgium in 1914",
                "art", "history", "science"});
        facts.add(new String[]{"Rock Werchter is the Best Festival in the World", "festivals", "music",
                "world champions"});

        // Make the table a bit bigger
        facts.addAll(facts);
        facts.addAll(facts);
        facts.addAll(facts);

        return facts;
    }

    private static PDPage addNewPage(PDDocument doc) {
        PDPage page = new PDPage();
        doc.addPage(page);
        return page;
    }
}
