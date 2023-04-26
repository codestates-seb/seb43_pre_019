package backend.com.backend.question.service;

import backend.com.backend.answer.entity.Answer;
import backend.com.backend.exception.BusinessLogicException;
import backend.com.backend.exception.ExceptionCode;
import backend.com.backend.member.entity.Member;
import backend.com.backend.member.repository.MemberRepository;
import backend.com.backend.question.entity.Question;
import backend.com.backend.question.mapper.QuestionMapper;
import backend.com.backend.question.repository.QuestionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final QuestionMapper mapper;

    public QuestionService(QuestionRepository questionRepository, MemberRepository memberRepository, QuestionMapper mapper) {
        this.questionRepository = questionRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
    }

    public Question createQuestion(Question question, UserDetails user) {
        Optional<Member> member = memberRepository.findByEmail(user.getUsername());
        Member findMember = member.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        //question필드를 findMember를 통해 쌍방으로 세팅해준다.
        findMember.setQuestion(question);
        //Question 테이블에 있는 글쓴이 필드를 세팅해준다.
        question.setWrittenBy(findMember.getDisplayName());
        // memberRepository.save()를 하면 의존성에 의해 같은 내용의 엔티티가 두 번 등록되고 애꿎은
        //PK만 올라간다. 그러니 밑에 코드 한번으로도 충분하다. 저러면
        //Member 엔티티와 Answer엔티티 모두 영속성 컨텍스트에 저장되고 DB반영된다.
       return questionRepository.save(question);

    }
    public Question updateQuestion(Question question, Authentication authentication) {
        //요청자의 유저정보 이메일을 authentication으로부터 끌어온다.
        String username = authentication.getName();
        //해당 포스트의 원래 유저의 이메일을 불러온다.
        String originalUser = findByQuestionId(question.getId()).getMember().getEmail();
        //두 개를 비교해서 같지 않다면 에러가 뜬다. 즉 원래 작성자에게만 허용된 요청이다.
        Question finalQuestion;
        if (!username.equals(originalUser)) {
            throw new AccessDeniedException("해당 포스트의 작성자에게만 허용된 요청입니다.");
        } else {
            Question findQuestion = findByQuestionId(question.getId());
            Optional.ofNullable(question.getTitle())
                    .ifPresent(title -> findQuestion.setTitle(title));
            Optional.ofNullable(question.getBody())
                    .ifPresent(body -> findQuestion.setBody(body));

            finalQuestion = questionRepository.save(findQuestion);
        }
        return finalQuestion;
    }
    public Question findQuestion(long questionId) {
        return findByQuestionId(questionId);
    }

    public List<Question> findQuestions() {

        return (List<Question>) questionRepository.findAll();
    }

    public void deleteQuestion(long questionId, Authentication authentication) {
        Question findQuestion = findByQuestionId(questionId);

        //요청자의 유저정보 이메일을 authentication으로부터 끌어온다.
        String username = authentication.getName();
        //해당 포스트의 원래 유저의 이메일을 불러온다.
        String originalUser = findQuestion.getMember().getEmail();
        //두 개를 비교해서 같지 않다면 에러가 뜬다. 즉 원래 작성자에게만 허용된 요청이다.
        if(!username.equals(originalUser)) {
            throw new AccessDeniedException("해당 포스트의 작성자에게만 허용된 요청입니다.");
        }
        else {

            Member findMember = findQuestion.getMember();
            findMember.setTotalQuestions(findMember.getTotalQuestions() - 1);
            //질문을 삭제한 당사자 멤버의 전체질문수를 1 차감한다.

            List<Member> members = findQuestion.getAnswers().stream().map(Answer::getMember).collect(Collectors.toList());
            for (Member member : members) {
                member.setTotalAnswers(member.getTotalAnswers() - 1);
            }
            memberRepository.saveAll(members);
            //질문이 삭제되면 자동으로 그에 딸린 답변들 또한 삭제된다. 그렇다면 그 질문에 답변들을 단 각각의 다른 유저들의
            //프로필상 Answers의 리스트 또한 변경이 되므로(질문이 삭제되며 관련 답변이 삭제되므로)
            //totalAnswers의 갯수도 변화해야 한다. 따라서 위 로직으로 적용한다.
            //또한 자바 엔티티 상으론 totalAnswers가 변했지만 아직 DB 테이블상엔 적용이 안 되었다.
            //따라서 memberRepository를 DI로 끌어온다. 그 후 저렇게 저장해준다.

            //한편 질문이 삭제된다고해서 그에 딸린 답변들까지 삭제된다면 그 각각의 답변을 쓰기 위해
            //고생했던 다른 멤버들의 답변리스트기록들은 삭제되는 것이기에 불합리하다.
            //히자만 지금은 클론코딩이고 요구사항에 따르면 질문삭제시 답변들또한 자동삭제(CascadeType.REMOVE)
            //이므로 저렇게 구현하였다.
            //다만 1질문 삭제함에도 거기에 답변들을 단 다른 멤버들의 각 프로필에는
            //여전히 답변리스트와 totalAnswers를 보존시키고
            //답변 한개를 눌렀을때 "해당 답변의 상위질문은 삭제되었습니다."팝업을 띄워서 기삭제된 GET 질문요청에
            //대한 예외처리를 하는 방법도 있을 것이다. 그렇지만,, 지금 내 수준으로는 관련 아키텍쳐와 필요한 개념과 로직들이
            //감도 안 잡힌다. 이 부분은 나중에 공부해봐야겠다.
            questionRepository.delete(findQuestion);
        }
    }

    public Question findByQuestionId(long questionId) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        Question findQuestion = optionalQuestion.orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
        return findQuestion;
    }
}
