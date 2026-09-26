package com.huseynovvusal.springblogapi.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.huseynovvusal.springblogapi.repository.LikeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

  private static final Long USER_ID = 42L;
  private static final Long BLOG_ID = 10L;

  @Mock private BookmarkRepository bookmarkRepository;
  @Mock private BlogRepository blogRepository;
  @Mock private EntityManager entityManager;
  @Mock private LikeRepository likeRepository;

  private BookmarkService bookmarkService;

  @BeforeEach
  void setUp() {
    bookmarkService =
        spy(new BookmarkService(bookmarkRepository, blogRepository, entityManager, likeRepository));
    doReturn(USER_ID).when(bookmarkService).currentUserId();
  }

  @Test
  void addBookmarkShouldDoNothingWhenBookmarkAlreadyExists() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(true);

    bookmarkService.addBookmark(BLOG_ID);

    verify(bookmarkRepository, never()).save(any());
    verify(blogRepository, never()).findById(BLOG_ID);
  }

  @Test
  void addBookmarkShouldThrowWhenBlogDoesNotExist() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(false);
    when(blogRepository.findById(BLOG_ID)).thenReturn(java.util.Optional.empty());

    assertThrows(BlogNotFoundException.class, () -> bookmarkService.addBookmark(BLOG_ID));

    verify(bookmarkRepository, never()).save(any());
  }

  @Test
  void removeBookmarkShouldDeleteBookmarkForCurrentUser() {
    bookmarkService.removeBookmark(BLOG_ID);

    verify(bookmarkRepository).deleteByUser_IdAndBlog_Id(USER_ID, BLOG_ID);
  }

  @Test
  void isBookmarkedShouldReturnRepositoryResult() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(true);

    boolean result = bookmarkService.isBookmarked(BLOG_ID);

    assertTrue(result);
    verify(bookmarkRepository).existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID);
  }

  @Test
  void toggleShouldRemoveExistingBookmarkAndReturnFalse() {
    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(true);

    boolean result = bookmarkService.toggle(BLOG_ID);

    assertFalse(result);
    verify(bookmarkRepository).deleteByUser_IdAndBlog_Id(USER_ID, BLOG_ID);
    verify(bookmarkRepository, never()).save(any());
  }

  @Test
  void toggleShouldAddBookmarkWhenItDoesNotExistAndReturnTrue() {
    Blog blog = new Blog();
    User user = new User();

    when(bookmarkRepository.existsByUser_IdAndBlog_Id(USER_ID, BLOG_ID)).thenReturn(false);
    when(blogRepository.findById(BLOG_ID)).thenReturn(java.util.Optional.of(blog));
    when(entityManager.getReference(User.class, USER_ID)).thenReturn(user);

    boolean result = bookmarkService.toggle(BLOG_ID);

    assertTrue(result);
    verify(bookmarkRepository).save(any());
    verify(bookmarkRepository, never()).deleteByUser_IdAndBlog_Id(USER_ID, BLOG_ID);
  }
}
