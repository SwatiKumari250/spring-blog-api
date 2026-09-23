package com.huseynovvusal.springblogapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huseynovvusal.springblogapi.exception.BlogNotFoundException;
import com.huseynovvusal.springblogapi.model.Blog;
import com.huseynovvusal.springblogapi.model.User;
import com.huseynovvusal.springblogapi.repository.BlogRepository;
import com.huseynovvusal.springblogapi.repository.BookmarkRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkService Unit Tests")
class BookmarkServiceTest {

  private static final Long USER_ID = 42L;
  private static final Long BLOG_ID = 1L;

  @Mock private BookmarkRepository bookmarkRepository;
  @Mock private BlogRepository blogRepository;
  @Mock private EntityManager entityManager;

  private BookmarkService bookmarkService;

  @BeforeEach
  void setup() {
    bookmarkService = spy(new BookmarkService(bookmarkRepository, blogRepository, entityManager, null));
    doReturn(USER_ID).when(bookmarkService).currentUserId();
  }

  @Test
  @DisplayName("should not save when bookmark already exists")
  void shouldNotSaveWhenBookmarkAlreadyExists() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(true);

    bookmarkService.addBookmark(BLOG_ID);

    verify(bookmarkRepository, never()).save(any());
    verify(blogRepository, never()).findById(any());
  }

  @Test
  @DisplayName("should throw when blog does not exist")
  void shouldThrowWhenBlogDoesNotExist() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(false);
    when(blogRepository.findById(BLOG_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookmarkService.addBookmark(BLOG_ID))
        .isInstanceOf(BlogNotFoundException.class);

    verify(bookmarkRepository, never()).save(any());
  }

  @Test
  @DisplayName("should remove bookmark for current user")
  void shouldRemoveBookmarkForCurrentUser() {
    bookmarkService.removeBookmark(BLOG_ID);

    verify(bookmarkRepository).deleteByUser_IdAndBlog_Id(USER_ID, BLOG_ID);
  }

  @Test
  @DisplayName("should return repository bookmark status")
  void shouldReturnRepositoryBookmarkStatus() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(true);

    boolean result = bookmarkService.isBookmarked(BLOG_ID);

    assertThat(result).isTrue();
    verify(bookmarkRepository).existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID);
  }

  @Test
  @DisplayName("should add bookmark when toggling an unbookmarked blog")
  void shouldAddBookmarkWhenTogglingOff() {
    Blog blog = new Blog();
    User user = new User();

    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(false);
    when(blogRepository.findById(BLOG_ID)).thenReturn(Optional.of(blog));
    when(entityManager.getReference(User.class, USER_ID)).thenReturn(user);

    boolean result = bookmarkService.toggle(BLOG_ID);

    assertThat(result).isTrue();
    verify(bookmarkRepository).save(any());
    verify(blogRepository).findById(BLOG_ID);
  }

  @Test
  @DisplayName("should remove bookmark when toggling a bookmarked blog")
  void shouldRemoveBookmarkWhenTogglingOn() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(true);

    boolean result = bookmarkService.toggle(BLOG_ID);

    assertThat(result).isFalse();
    verify(bookmarkRepository).deleteByUser_IdAndBlog_Id(USER_ID, BLOG_ID);
    verify(bookmarkRepository, never()).save(any());
  }
}
