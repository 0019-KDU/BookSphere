package com.chiradev.book.feedback;

import com.chiradev.book.book.Book;
import com.chiradev.book.book.BookRepository;
import com.chiradev.book.book.PageResponse;
import com.chiradev.book.exception.OperationNotPermittedException;
import com.chiradev.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final BookRepository bookRepository;
    private final FeedbackMapper feedbackMapper;

    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        Book book=bookRepository.findById(request.bookId())
                .orElseThrow(()-> new EntityNotFoundException("No book found for id "+request.bookId()));

        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("You can not give a feedback for an archived book and not a shareable book");
        }

        User user=((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermittedException("You can not give a feedback to your own book");
        }

        Feedback feedback=feedbackMapper.toFeedback(request);
         return feedbackRepository.save(feedback).getId();
    }

    public PageResponse<FeedbackResponse> findAllFeedbackByBook(Integer bookId, int page, int size, Authentication connectedUser) {
        Pageable pageable= PageRequest.of(page,size);
        User user=((User) connectedUser.getPrincipal());
        Page<Feedback> feedbacks=feedbackRepository.findAllByBookId(bookId,pageable);
        List<FeedbackResponse> feedbackResponses=feedbacks.stream()
                .map(f->feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();

        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );
    }
}
