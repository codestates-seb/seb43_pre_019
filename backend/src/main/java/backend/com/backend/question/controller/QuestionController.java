package backend.com.backend.question.controller;

import backend.com.backend.answer.controller.AnswerController;
import backend.com.backend.auth.userdetails.MemberDetailsService;
import backend.com.backend.question.dto.QuestionDto;
import backend.com.backend.question.entity.Question;
import backend.com.backend.question.mapper.QuestionMapper;
import backend.com.backend.question.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
@Validated
@CrossOrigin(origins = "*")
public class QuestionController {
    private final static String QUESTION_DEFAULT_URL = "/api/questions";
    private final QuestionService questionService;
    private final MemberDetailsService memberDetailsService;
    private final QuestionMapper questionMapper;
    private final AnswerController answerController;

    public QuestionController(QuestionService questionService, MemberDetailsService memberDetailsService, QuestionMapper questionMapper, AnswerController answerController) {
        this.questionService = questionService;
        this.memberDetailsService = memberDetailsService;
        this.questionMapper = questionMapper;
        this.answerController = answerController;
    }

    @PostMapping
    public ResponseEntity postQuestion(@Valid @RequestBody QuestionDto.Post postDto,
                                       Authentication authentication) {
        //아래 두 줄의 코드는 인증정보 Authentication을 바탕으로 유저 정보를 끌어낸다.
        String username = authentication.getName();
        UserDetails user = memberDetailsService.loadUserByUsername(username);
        //그리고는 service단으로 넘어가서 질문이 save()되기 전 Member 외래키 필드를 채워주는 역할을 한다.
        Question question = questionService.createQuestion(questionMapper.questionPostDtoToQuestion(postDto), user);

        URI location =
                UriComponentsBuilder
                        .newInstance()
                        .path(QUESTION_DEFAULT_URL + "/{question-id}")
                        .buildAndExpand(question.getId())
                        .toUri(); // "questions/{question-id}"
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{question-id}")
    public ResponseEntity patchQuestion(@PathVariable("question-id") @Positive long questionId,
                                        @Valid @RequestBody QuestionDto.Patch patchDto,
                                        Authentication authentication) {

        patchDto.setQuestionId(questionId);
        Question question = questionMapper.questionPatchDtoToQuestion(patchDto);

        Question updatedQuestion = questionService.updateQuestion(question, authentication);
        return new ResponseEntity<>(questionMapper.questionToQuestionResponseDto(updatedQuestion), HttpStatus.OK);

    }

    @GetMapping("/{question-id}")
    public ResponseEntity getQuestion(@PathVariable("question-id") @Positive long questionId,
                                      @Positive @RequestParam(defaultValue = "1") int page,
                                      @Positive @RequestParam(defaultValue = "3") int size,
                                      @RequestParam(defaultValue = "newest") String sortOption) {
        Question question = questionService.findQuestion(questionId);

        return answerController.bySortOption(questionId, page, size, sortOption, question);
        //질문컨트롤러에 getQuestion메소드 내부에서 위 로직으로 실행해야 할 듯해.
        //일단 질문이 품고 있는 답변리스트의 size()를 재서 0이면 그냥 response 리턴하면 될테고
        // 0 초과이면 multi리스폰스로 내보내서 질문 한 개에 답변 리스트들을 stream으로 구성해보면 좋겠지
        // postman

    }


    @GetMapping
    public ResponseEntity getQuestions() {
        List<Question> questions = questionService.findQuestions();

        List<QuestionDto.Response> response = questionMapper.questionsToQuestionResponseDtos(questions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{question-id}")
    public ResponseEntity deleteQuestion(@PathVariable("question-id") @Positive long questionId,
                                         Authentication authentication) {
        questionService.deleteQuestion(questionId, authentication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
