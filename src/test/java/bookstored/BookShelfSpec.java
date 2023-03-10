package bookstored;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import bookstoread.Book;
import bookstoread.BookShelf;
import bookstoread.BookShelfCapacityReached;
import bookstoread.Progress;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("<= BookShelf Specification =>")
@ExtendWith(BooksParameterResolver.class)
@ExtendWith(LoggingTestExecutionExceptionHandler.class)
public class BookShelfSpec {

  private BookShelf shelf;
  private Book effectiveJava;
  private Book codeComplete;
  private Book mythicalManMonth;
  private Book cleanCode;

  @BeforeEach
  void init(Map<String, Book> books) throws Exception {
    shelf = new BookShelf();
    /*  effectiveJava = new Book("Effective Java", "Joshua Bloch", LocalDate.of(2008, Month.MAY, 8));
    codeComplete = new Book("Code Complete", "Steve McConnel", LocalDate.of(2004, Month.JUNE, 9));
    mythicalManMonth = new Book("The Mythical Man-Month", "Frederick Phillips Brooks",
        LocalDate.of(1975, Month.JANUARY, 1));
    cleanCode = new Book("Clean Code", "Robert C. Martin", LocalDate.of(2008, Month.AUGUST, 1));*/

    this.effectiveJava = books.get("Effective Java");
    this.codeComplete = books.get("Code Complete");
    this.mythicalManMonth = books.get("The Mythical Man-Month");
    this.cleanCode = books.get("Clean Code");
  }

  @Nested
  @DisplayName("is empty")
  class IsEmpty {

    @Test
    @DisplayName("when no book is added to it")
    public void emptyBookShelfWhenNoBookAdded() {
      List<Book> books = shelf.books();
      assertTrue(books.isEmpty(), () -> "BookShelf should be empty.");
    }

    @Test
    @DisplayName("when add is called without books")
    void emptyBookShelfWhenAddIsCalledWithoutBooks() {
      shelf.add();
      List<Book> books = shelf.books();
      assertTrue(books.isEmpty(), () -> "BookShelf should be empty.");
    }
  }

  @Nested
  @DisplayName("after adding books")
  class BooksAreAdded {

    @Test
    public void bookshelfContainsTwoBooksWhenTwoBooksAdded() {
      shelf.add(effectiveJava, codeComplete);
      List<Book> books = shelf.books();
      assertEquals(2, books.size(), () -> "BookShelf should have two books.");
    }

    @Test
    void booksReturnedFromBookShelfIsImmutableForClient() {
      shelf.add(effectiveJava, codeComplete);
      List<Book> books = shelf.books();
      try {
        books.add(mythicalManMonth);
        fail(() -> "Should not be able to add book to books");
      } catch (Exception e) {
        assertTrue(e instanceof UnsupportedOperationException,
            () -> "Should throw UnsupportedOperationException.");
      }
    }
  }

  @Nested
  @DisplayName("is arranged")
  class BooksArrange {

    /*
     * Second Feature
     * */
    @Test
    @DisplayName("bookshelf is arranged lexicographically by book title")
    void bookshelfArrangedByBookTitle() {
      shelf.add(effectiveJava, codeComplete, mythicalManMonth);
      List<Book> books = shelf.arrange();
      assertEquals(asList(codeComplete, effectiveJava, mythicalManMonth), books,
          () -> "Books in a bookshelf should be arranged lexicographically by book title");
    }

    @Test
    void booksInBookShelfAreInInsertionOrderAfterCallingArrange() {
      shelf.add(effectiveJava, codeComplete, mythicalManMonth);
      shelf.arrange();
      List<Book> books = shelf.books();
      assertEquals(asList(effectiveJava, codeComplete, mythicalManMonth), books,
          () -> "Books in bookshelf are in insertion order");
    }

    // @Disabled("Needs to implement Comparator")
    @Test
    void bookshelfArrangedByUserProvidedCriteria() {
      shelf.add(effectiveJava, codeComplete, mythicalManMonth);
      Comparator<Book> reversed = Comparator.<Book>naturalOrder().reversed();
      List<Book> books = shelf.arrange(reversed);
      assertEquals(
          asList(mythicalManMonth, effectiveJava, codeComplete),
          books,
          () -> "Books in a bookshelf are arranged in descending order of book title");

      assertThat(books).isSortedAccordingTo(reversed);
    }

    @Test
    @DisplayName("books inside bookshelf are grouped by publication year")
    void groupBooksInsideBookShelfByPublicationYear() {
      shelf.add(effectiveJava, codeComplete, mythicalManMonth, cleanCode);

      Map<Year, List<Book>> booksByPublicationYear = shelf.groupByPublicationYear();

      assertThat(booksByPublicationYear)
          .containsKey(Year.of(2008))
          .containsValues(Arrays.asList(effectiveJava, cleanCode));

      assertThat(booksByPublicationYear)
          .containsKey(Year.of(2004))
          .containsValues(singletonList(codeComplete));

      assertThat(booksByPublicationYear)
          .containsKey(Year.of(1975))
          .containsValues(singletonList(mythicalManMonth));

    }

    @Test
    @DisplayName("books inside bookshelf are grouped according to user provided criteria(group by author name)")
    void groupBooksByUserProvidedCriteria() {
      shelf.add(effectiveJava, codeComplete, mythicalManMonth, cleanCode);
      Map<String, List<Book>> booksByAuthor = shelf.groupBy(Book::getAuthor);

      assertThat(booksByAuthor)
          .containsKey("Joshua Bloch")
          .containsValues(singletonList(effectiveJava));

      assertThat(booksByAuthor)
          .containsKey("Steve McConnel")
          .containsValues(singletonList(codeComplete));

      assertThat(booksByAuthor)
          .containsKey("Frederick Phillips Brooks")
          .containsValues(singletonList(mythicalManMonth));

      assertThat(booksByAuthor)
          .containsKey("Robert C. Martin")
          .containsValues(singletonList(cleanCode));
    }
  }

  @Test
  @DisplayName("is 0% completed and 100% to-read when no book is read yet")
  void progress100PercentUnread() {
//    Progress progress = shelf.progress();
    //  assertThat(progress.completed()).isEqualTo(0);
    //  assertThat(progress.toRead()).isEqualTo(100);
  }

  @Test
  @DisplayName("is 40% completed and 60% to-read when 2 books are finished and 3 books not read yet")
  void progressWithCompletedAndToReadPercentages() {
    effectiveJava.startedReadingOn(LocalDate.of(2016, Month.JULY, 1));
    effectiveJava.finishedReadingOn(LocalDate.of(2016, Month.JULY, 31));
    cleanCode.startedReadingOn(LocalDate.of(2016, Month.AUGUST, 1));
    cleanCode.finishedReadingOn(LocalDate.of(2016, Month.AUGUST, 31));
    shelf.add(effectiveJava);
    //  shelf.add(cleanCode);

    Progress progress = shelf.progress();
    //  assertThat(progress.completed()).isEqualTo(40);
    //  assertThat(progress.toRead()).isEqualTo(60);
  }

  @Nested
  @DisplayName("search")
  class BookShelfSearchSpec {

    @BeforeEach
    void setup() {
      shelf.add(codeComplete, effectiveJava, mythicalManMonth, cleanCode);
    }

    @Test
    @DisplayName(" should find books with title containing text")
    void shouldFindBooksWithTitleContainingText() {
      List<Book> books = shelf.findBooksByTitle("code");
      assertThat(books.size()).isEqualTo(2);
    }

    @Test
    @DisplayName(" should find books with title containing text and published after specified date.")
    void shouldFilterSearchedBooksBasedOnPublishedDate() {
      List<Book> books = shelf.findBooksByTitle("code",
          b -> b.getPublishedOn().isBefore(LocalDate.of(2014, 12, 31)));
      assertThat(books.size()).isEqualTo(2);
    }
  }

  /*
  @Test
  void throwsExceptionWhenBooksAreAddedAfterCapacityIsReached() {
    BookShelf bookShelf = new BookShelf(2);
    bookShelf.add(effectiveJava, codeComplete);
    try {
      bookShelf.add(mythicalManMonth);
      fail("Should throw BookShelfCapacityReached exception as more books are added than shelf capacity.");
    } catch (BookShelfCapacityReached expected) {
      assertEquals("BookShelf capacity of 2 is reached. You can't add more books.",
          expected.getMessage());
    }
  }
   */

  @Test
  void throwsExceptionWhenBooksAreAddedAfterCapacityIsReached() {
    BookShelf bookShelf = new BookShelf(2);
    bookShelf.add(effectiveJava, codeComplete);
    BookShelfCapacityReached throwException = assertThrows(BookShelfCapacityReached.class, () -> bookShelf.add(mythicalManMonth));
    assertEquals("BookShelf capacity of 2 is reached. You can't add more books.", throwException.getMessage());
  }

  @Test
  @RepeatedTest(value = 10, name = "i_am_a_repeated_test__{currentRepetition}/{totalRepetitions}")
  void test_should_complete_in_one_second() {
 //   assertTimeout(Duration.of(1, ChronoUnit.SECONDS), () -> Thread.sleep(1000));

    String message = assertTimeout(Duration.of(1, ChronoUnit.SECONDS), () -> "Hello, World!");
    assertEquals("Hello, World!", message);
  }
}
