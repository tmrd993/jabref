package org.jabref.logic.pdf.search.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.jabref.logic.pdf.search.indexing.PdfIndexer;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.pdf.search.PdfSearchResults;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PdfSearcherTest {

    private PdfSearcher search;

    @BeforeEach
    public void setUp() throws IOException {
        search = new PdfSearcher();

        // given
        PdfIndexer indexer = new PdfIndexer();
        BibDatabase database = new BibDatabase();
        BibDatabaseContext context = mock(BibDatabaseContext.class);
        when(context.getFileDirectories(Mockito.any())).thenReturn(Collections.singletonList(Path.of("src/test/resources/pdfs")));
        BibEntry examplePdf = new BibEntry(StandardEntryType.Article);
        examplePdf.setFiles(Collections.singletonList(new LinkedFile("Example Entry", "example.pdf", "pdf")));
        database.insertEntry(examplePdf);

        BibEntry metaDataEntry = new BibEntry(StandardEntryType.Article);
        metaDataEntry.setFiles(Collections.singletonList(new LinkedFile("Metadata Entry", "metaData.pdf", "pdf")));
        metaDataEntry.setCitationKey("MetaData2017");
        database.insertEntry(metaDataEntry);

        BibEntry exampleThesis = new BibEntry(StandardEntryType.PhdThesis);
        exampleThesis.setFiles(Collections.singletonList(new LinkedFile("Example Thesis", "thesis-example.pdf", "pdf")));
        exampleThesis.setCitationKey("ExampleThesis");
        database.insertEntry(exampleThesis);

        indexer.createIndex(database, context);
    }

    @Test
    public void searchForTest() throws IOException, ParseException {
        PdfSearchResults result = search.search("test", 10);
        assertEquals(2, result.numSearchResults());
    }

    @Test
    public void searchForUniversity() throws IOException, ParseException {
        PdfSearchResults result = search.search("University", 10);
        assertEquals(1, result.numSearchResults());
    }

    @Test
    public void searchForStopWord() throws IOException, ParseException {
        PdfSearchResults result = search.search("and", 10);
        assertEquals(0, result.numSearchResults());
    }

    @Test
    public void searchForSecond() throws IOException, ParseException {
        PdfSearchResults result = search.search("second", 10);
        assertEquals(2, result.numSearchResults());
    }

    @Test
    public void searchForEmptyString() throws IOException {
        PdfSearchResults result = search.search("", 10);
        assertEquals(0, result.numSearchResults());
    }

    @Test
    public void searchWithNullString() throws IOException {
        assertThrows(NullPointerException.class, () -> search.search(null, 10));
    }

    @Test
    public void searchForZeroResults() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> search.search("test", 0));
    }
}