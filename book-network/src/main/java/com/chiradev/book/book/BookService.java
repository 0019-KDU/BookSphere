package com.chiradev.book.book;

import com.chiradev.book.exception.OperationNotPermittedException;
import com.chiradev.book.file.FileStorageService;
import com.chiradev.book.history.BookTransactionHistory;
import com.chiradev.book.history.BookTransactionHistoryRepository;
import com.chiradev.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.chiradev.book.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookTransactionHistoryRepository transactionHistoryRepository;
    private final FileStorageService fileStorageService;


    public Integer save(BookRequest request, Authentication connectedUser) {
        User user=((User) connectedUser.getPrincipal());
        Book book=bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(()->new EntityNotFoundException("No Book found with the Id:: "+bookId));

    }


    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user=((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books=bookRepository.findAllDisplayableBooks(pageable, user.getId());
        List<BookResponse> bookResponse= books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user=((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books=bookRepository.findAll(withOwnerId(user.getId()),pageable);
        List<BookResponse> bookResponse= books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user=((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks=transactionHistoryRepository.findAllBorrowedBooks(pageable,user.getId());
        List<BorrowedBookResponse> bookResponse=allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user=((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks=transactionHistoryRepository.findAllReturnedBooks(pageable,user.getId());
        List<BorrowedBookResponse> bookResponse=allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("Book not found with the Id:: "+bookId));
        User user=((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public  Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("Book not found with the Id:: "+bookId));
        User user=((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update Others books archived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("Book not found with the Id:: "+bookId));

        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or shareable");
        }

        User user=((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrowe your own book");
        }
        final boolean isAlreadyBorrowed= transactionHistoryRepository.isAlreadyBorrowedByUser(book.getId(),user.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is already borrowed");
        }

        BookTransactionHistory bookTransactionHistory= BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();

        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("Book not found with the Id:: "+bookId));

        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or shareable");
        }
        User user=((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        BookTransactionHistory bookTransactionHistory=transactionHistoryRepository.findByBookIdAndUserId(bookId,user.getId())
                .orElseThrow(()-> new OperationNotPermittedException("You did not return your own book"));

        bookTransactionHistory.setReturned(true);
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book=bookRepository.findById(bookId)
            .orElseThrow(()->new EntityNotFoundException("Book not found with the Id:: "+bookId));

        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or shareable");
        }
        User user=((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }
        BookTransactionHistory bookTransactionHistory=transactionHistoryRepository.findByBookIdAndOwnerId(bookId,user.getId())
                .orElseThrow(()-> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));

        bookTransactionHistory.setReturnApproved(true);
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("Book not found with the Id:: "+bookId));

        User user=((User) connectedUser.getPrincipal());

         var bookCover =fileStorageService.saveFile(file,user.getId());
         book.setBookCover(bookCover);
         bookRepository.save(book);
    }
}
