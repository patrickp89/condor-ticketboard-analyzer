package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.util.Pair;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CondorizerTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @Ignore
    public void testTrelloBoardFetching() throws IOException, ClassNotFoundException {
        final File testFolder = temporaryFolder.newFolder("test-condor-csv-files");
        final TicketBoardCondorizer condorizer = new DefaultTicketBoardCondorizer();
        final Pair<File, File> csvFiles = condorizer.ticketBoardToCsvFiles(
                TicketBoard.TRELLO,
                null,
                "lgaJQMYA",
                testFolder.getAbsolutePath()
        );

        assertThat(csvFiles, is(not(nullValue())));
        assertThat(csvFiles.getFirst(), is(not(nullValue())));
        assertThat(csvFiles.getSecond(), is(not(nullValue())));
    }

    @Test
    @Ignore
    public void testGitHubRepoFetching() throws IOException, ClassNotFoundException {
        final File testFolder = temporaryFolder.newFolder("test-condor-csv-files");
        final TicketBoardCondorizer condorizer = new DefaultTicketBoardCondorizer();
        final Pair<File, File> csvFiles = condorizer.ticketBoardToCsvFiles(
                TicketBoard.GITHUB,
                "linuxmint",
                "cinnamon-spices-extensions",
                testFolder.getAbsolutePath()
        );

        assertThat(csvFiles, is(not(nullValue())));
        assertThat(csvFiles.getFirst(), is(not(nullValue())));
        assertThat(csvFiles.getSecond(), is(not(nullValue())));
    }
}
