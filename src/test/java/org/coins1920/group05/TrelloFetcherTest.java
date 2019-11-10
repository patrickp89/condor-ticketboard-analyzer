package org.coins1920.group05;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.coins1920.group05.fetcher.TrelloBoardFetcher;
import org.coins1920.group05.fetcher.model.trello.Action;
import org.coins1920.group05.fetcher.model.trello.Board;
import org.coins1920.group05.fetcher.model.trello.Card;
import org.coins1920.group05.fetcher.model.trello.Member;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TrelloFetcherTest {

    private Logger logger = LoggerFactory.getLogger(TrelloFetcherTest.class);

    private static final String APPLICATION_JSON = "application/json";
    private static final String SAMPLE_BOARD_SHORTLINK = "lgaJQMYA";
    private static final String SAMPLE_CARD_ID1 = "5db19ed82bd7cd5b26346bd7";
    private static final String SAMPLE_CARD_ID2 = "5db19ed8256e14829baf66e0";
    private static final int WIREMOCK_PORT = 8089;

    private static TrelloBoardFetcher fetcher;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(WIREMOCK_PORT));

    @BeforeClass
    public static void setUpClass() {
        final String key = System.getenv("TRELLO_API_KEY");
        final String token = System.getenv("TRELLO_OAUTH_KEY");
        fetcher = new TrelloBoardFetcher(key, token, "http://localhost:" + WIREMOCK_PORT + "/");
    }

    @Before
    public void setUp() {
        final String singleBoard = readFromResourceFile("trello/single_board.json");
        stubFor(get(urlPathMatching("/1/boards/" + SAMPLE_BOARD_SHORTLINK + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(singleBoard)));

        final String boardMembers = readFromResourceFile("trello/board_members.json");
        stubFor(get(urlPathMatching("/1/boards/" + SAMPLE_BOARD_SHORTLINK + "/members" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(boardMembers)));

        final String cards = readFromResourceFile("trello/cards.json");
        stubFor(get(urlPathMatching("/1/boards/" + SAMPLE_BOARD_SHORTLINK + "/cards" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(cards)));

        final String cardActions = readFromResourceFile("trello/card_actions.json");
        stubFor(get(urlPathMatching("/1/cards/" + SAMPLE_CARD_ID1 + "/actions" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(cardActions)));

        final String cardMembers = readFromResourceFile("trello/card_members.json");
        stubFor(get(urlPathMatching("/1/cards/" + SAMPLE_CARD_ID2 + "/members" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(cardMembers)));
    }

    @Test
    public void testFetchSingleBoard() {
        final Board board = fetcher.fetchBoard(null, SAMPLE_BOARD_SHORTLINK);
        assertThat(board, is(not(nullValue())));
        assertThat(board.getId(), is("5db19ed8f8b54324663c159c"));
        assertThat(board.getName(), is("Team5Coin"));
    }

    @Test
    public void testFetchBoardMembers() {
        final List<Member> members = fetcher.fetchBoardMembers(null, SAMPLE_BOARD_SHORTLINK);
        assertThat(members, is(not(nullValue())));
        assertThat(members.size(), is(not(0)));
        logger.info("There is/are " + members.size() + " member(s)!");
        logger.info(" the first one is: " + members.get(0));

        final long membersCalledBugs = members
                .stream()
                .filter(m -> m.getFullName().equals("Bugs"))
                .count();
        assertThat(membersCalledBugs, is(1L));
    }

    @Test
    public void testFetchCards() {
        final List<Card> cards = fetcher.fetchTickets(null, SAMPLE_BOARD_SHORTLINK);
        assertThat(cards, is(not(nullValue())));
        assertThat(cards.size(), is(not(0)));
        logger.info("There is/are " + cards.size() + " card(s)!");
        logger.info(" the first one is: " + cards.get(0));

        final long cardsNamedRCsvStructure = cards
                .stream()
                .filter(m -> m.getName().equals("CSV structure"))
                .count();
        assertThat(cardsNamedRCsvStructure, is(1L));
    }

    @Test
    public void testFetchActionsForCard() {
        final List<Action> actions = fetcher.fetchActionsForTicket(SAMPLE_CARD_ID1);
        assertThat(actions, is(not(nullValue())));
        assertThat(actions.size(), is(not(0)));
        logger.info("There is/are " + actions.size() + " action(s)!");
        logger.info(" the first one is: " + actions.get(0));

        final long actionsWithCreateCardType = actions
                .stream()
                .filter(a -> a.getType().equals("createCard"))
                .count();
        assertThat(actionsWithCreateCardType, is(1L));
    }

    @Test
    public void testFetchMembersForCard() {
        final List<Member> members = fetcher.fetchMembersForTicket(SAMPLE_CARD_ID2);
        assertThat(members, is(not(nullValue())));
        assertThat(members.size(), is(not(0)));
        logger.info("There is/are " + members.size() + " member(s)!");
        logger.info(" the first one is: " + members.get(0));

        final long membersWithUsernamePp89 = members
                .stream()
                .filter(a -> a.getUsername().equals("patrickp89"))
                .count();
        assertThat(membersWithUsernamePp89, is(1L));
    }

    @Test
    @Ignore
    public void testFetchAllBoardsOfAMember() {
        final List<Board> boards = fetcher.fetchBoards();
        assertThat(boards, is(not(nullValue())));
        assertThat(boards.size(), is(not(0)));
        logger.info("There are " + boards.size() + " boards!");

        final Board firstBoard = boards.get(0);
        logger.info("boards[0] is: " + firstBoard);
        logger.info("boards[0].id = " + firstBoard.getId());
    }

    private static String readFromResourceFile(String fileName) {
        try {
            try (InputStream inputStream = TrelloFetcherTest
                    .class
                    .getClassLoader()
                    .getResourceAsStream(fileName)) {
                if (inputStream == null) {
                    return null;
                } else {
                    final BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(inputStream)
                    );
                    return bufferedReader
                            .lines()
                            .collect(Collectors.joining());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}