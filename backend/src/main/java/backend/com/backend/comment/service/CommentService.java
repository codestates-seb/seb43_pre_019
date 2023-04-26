package backend.com.backend.comment.service;

import backend.com.backend.answer.entity.Answer;
import backend.com.backend.answer.service.AnswerService;
import backend.com.backend.comment.entity.Comment;
import backend.com.backend.comment.repository.CommentRepository;
import backend.com.backend.exception.BusinessLogicException;
import backend.com.backend.exception.ExceptionCode;
import backend.com.backend.member.entity.Member;
import backend.com.backend.member.repository.MemberRepository;
import backend.com.backend.utils.CustomBeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final AnswerService answerService;
    private final CustomBeanUtils<Comment> beanUtils;

    public CommentService(CommentRepository commentRepository, MemberRepository memberRepository, AnswerService answerService, CustomBeanUtils<Comment> beanUtils) {
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.answerService = answerService;
        this.beanUtils = beanUtils;
    }

    public Comment createComment(Comment comment, UserDetails user) {
        Optional<Member> member = memberRepository.findByEmail(user.getUsername());
        Member findMember = member.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        //Comment 테이블에 있는 글쓴이 필드를 세팅해준다.
        comment.setWrittenBy(findMember.getDisplayName());
       return commentRepository.save(comment);
    }


    public List<Comment> findComments(long answerId) {
        Answer answer = answerService.findVerifiedAnswer(answerId);
       List<Comment> comments = new ArrayList<>(answer.getComments());
       return comments;
    }

    public Comment updateComment(Comment comment, Authentication authentication) {
        //아래 코드는 인증정보 Authentication을 바탕으로 유저 정보를 끌어낸다.
        Optional<Member> optionalMember = memberRepository.findByDisplayName(findVerifiedComment(comment.getId()).getWrittenBy());
        Member findMember = optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        String originalUser = findMember.getEmail();
        String user = authentication.getName();

        if(!originalUser.equals(user)) {
            throw new AccessDeniedException("댓글은 원래 작성자만 수정이 가능합니다.");
        } else {
            Comment findComment = findVerifiedComment(comment.getId());
            Comment updatedComment = beanUtils.copyNonNullProperties(comment, findComment);

            return commentRepository.save(updatedComment);
        }
    }
    public Comment findVerifiedComment(long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment findComment =
                optionalComment.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND));

        return findComment;
    }

    public void clearComment(long commentId, Authentication authentication) {
        Comment findComment = findVerifiedComment(commentId);
        String user = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByDisplayName(findComment.getWrittenBy());
        Member findMember = optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        String originalUser = findMember.getEmail();
        if(!user.equals(originalUser)) {
            throw new AccessDeniedException("댓글의 삭제는 원래 작성자만 가능합니다.");
        } else {
            commentRepository.delete(findComment);
        }
    }
}
