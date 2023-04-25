package backend.com.backend.answer.service;

import backend.com.backend.answer.entity.Answer;
import backend.com.backend.answer.repository.AnswerRepository;
import backend.com.backend.exception.BusinessLogicException;
import backend.com.backend.exception.ExceptionCode;
import backend.com.backend.member.entity.Member;
import backend.com.backend.member.repository.MemberRepository;
import backend.com.backend.question.entity.Question;
import backend.com.backend.utils.CustomBeanUtils;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final CustomBeanUtils<Answer> beanUtils;

    public AnswerService(AnswerRepository answerRepository, MemberRepository memberRepository, CustomBeanUtils<Answer> beanUtils) {
        this.answerRepository = answerRepository;
        this.memberRepository = memberRepository;
        this.beanUtils = beanUtils;
    }

    public Answer createAnswer(Question question, Answer answer, UserDetails user) {
        Optional<Member> member = memberRepository.findByEmail(user.getUsername());
        Member findMember = member.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        //answer필드를 findMember를 통해 쌍방으로 세팅해준다.
        findMember.setAnswer(answer);
        //한편 Question 테이블엔 answers 리스트가 반영이 아직 안되었으니 세팅하자
        question.setAnswer(answer); //이러면 Answer테이블에 있는 question 외래키 또한 세팅된다.
        //Answer 테이블에 있는 글쓴이 필드를 세팅해준다.
        answer.setWrittenBy(findMember.getDisplayName());
        // memberRepository.save()를 하면 의존성에 의해 같은 내용의 엔티티가 두 번 등록되고 애꿎은
        //PK만 올라간다. 그러니 밑에 코드 한번으로도 충분하다. 저러면
        //Member 엔티티와 Answer엔티티 모두 영속성 컨텍스트에 저장되고 DB반영된다.
        return answerRepository.save(answer);
    }

    public Answer updateAnswer(Answer answer) {
        Answer findAnswer = findVerifiedAnswer(answer.getId());

        Answer updatedAnswer = beanUtils.copyNonNullProperties(answer, findAnswer);

        return answerRepository.save(updatedAnswer);
    }

    public List<Answer> findAnswers(long questionId) {
        //질문 엔티티는 delete시 완전삭제이니 안심하고 fidnAll해도
        //memberStatus로 삭제를 관리하는 유저 엔티티는 다른 방식의 접근이 필요
        List<Answer> answers = answerRepository.findAllByQuestionId(questionId);

        return answers;
    }

    public Answer findVerifiedAnswer(long answerId) {
        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
        Answer findAnswer =
                optionalAnswer.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND));

        return findAnswer;
    }

    public void clearAnswer(long answerId) {
        Member findMember = findVerifiedAnswer(answerId).getMember();
        findMember.setTotalAnswers(findMember.getTotalAnswers() - 1);
        answerRepository.delete(findVerifiedAnswer(answerId));
    }

    public Page<Answer> makePageObject(int page, int size, List<Answer> answers) {
        int pageNumber = page;
        int pageSize = size;
        int totalElements = answers.size();

        //Pageable object
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // calculate the start and end index of the sublist
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), totalElements);

        // create a sublist from the original list
        List<Answer> sublist = answers.subList(start, end);

        // create a PageImpl object
        Page<Answer> pageList = new PageImpl<>(sublist, pageable, totalElements);
        return pageList;
    }

    public Answer findAnswer(long answerId) {
        Answer foundAnswer = findVerifiedAnswer(answerId);
        return foundAnswer;
    }
}
