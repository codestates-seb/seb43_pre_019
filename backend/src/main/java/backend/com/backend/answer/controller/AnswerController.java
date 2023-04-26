package backend.com.backend.answer.controller;

import backend.com.backend.answer.dto.AnswerDto;
import backend.com.backend.answer.entity.Answer;
import backend.com.backend.answer.mapper.AnswerMapper;
import backend.com.backend.answer.service.AnswerService;
import backend.com.backend.auth.userdetails.MemberDetailsService;
import backend.com.backend.question.dto.QuestionDto;
import backend.com.backend.question.entity.Question;
import backend.com.backend.question.mapper.QuestionMapper;
import backend.com.backend.question.service.QuestionService;
import backend.com.backend.response.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/api/questions/{id}/answers")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
public class AnswerController {

    private final static String ANSWER_DEFAULT_URL = "/api/questions/{id}/answers";
    private final AnswerService answerService;

    private final QuestionService questionService;
    private final MemberDetailsService memberDetailsService;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;

    public AnswerController(AnswerService answerService, QuestionService questionService, MemberDetailsService memberDetailsService, AnswerMapper answerMapper, QuestionMapper questionMapper) {
        this.answerService = answerService;
        this.questionService = questionService;
        this.memberDetailsService = memberDetailsService;
        this.answerMapper = answerMapper;
        this.questionMapper = questionMapper;
    }

    @PostMapping
        public ResponseEntity postAnswer (@PathVariable("id") long questionId,
                                          @Valid @RequestBody AnswerDto.Post requestBody,
                                          Authentication authentication){
        //아래 두 줄의 코드는 인증정보 Authentication을 바탕으로 유저 정보를 끌어낸다.
        String username = authentication.getName();
        UserDetails user = memberDetailsService.loadUserByUsername(username);
        //이제 Question과의 관계설정차례
        //답변 상위의 관련된 질문객체를 생성
        Question relatedQuestion = questionService.findByQuestionId(questionId);

        //그리고는 service단으로 넘어가서 답변이 save()되기 전 Member 외래키 필드를 채워주는 역할을 한다.
            Answer createdAnswer = answerService.createAnswer(relatedQuestion, answerMapper.answerPostDtoToAnswer(requestBody), user);

            URI location = UriComponentsBuilder
                    .newInstance()
                    .path(ANSWER_DEFAULT_URL + createdAnswer.getId())
                    .buildAndExpand(createdAnswer.getId())
                    .toUri();

            return ResponseEntity.created(location).build();

        }


    @PatchMapping("/{answer-id}")
    public ResponseEntity patchAnswer(@PathVariable("answer-id") @Positive long answerId,
                                      @Valid @RequestBody AnswerDto.Patch requestBody,
                                      Authentication authentication) {
        requestBody.setId(answerId);

        Answer answer =
                answerService.updateAnswer(answerMapper.answerPatchDtoToAnswer(requestBody), authentication);

        return new ResponseEntity<>(answerMapper.answerToAnswerResponseDto(answer), HttpStatus.OK);
    }

    @GetMapping("/{answer-id}")
    public ResponseEntity getAnswer(@PathVariable("answer-id") @Positive long answerId) {
        Answer answer = answerService.findAnswer(answerId);
        return new ResponseEntity<>(answerMapper.answerToAnswerResponseDto(answer), HttpStatus.OK);
    }


    @DeleteMapping("/{answer-id}")
    public ResponseEntity deleteAnswer(@PathVariable("answer-id") @Positive long answerId,
                                       Authentication authentication) {
        answerService.clearAnswer(answerId, authentication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // for GET
    //사실 아래 메소드는 비즈니스로직에 가까워서 AnswerService에 작성이 원칙이다.
    //그렇다고 AnswerService에 작성하자면
    //questionMapper answerMapper를 또 AnswerService단에서 DI받아야한다.
    //그러니 차라리 예외적으로 컨트롤러 단에서 있도록 해주었다.
    public ResponseEntity<?> bySortOption(long questionId, int page, int size, String sortOption, Question question) {
        QuestionDto.Response questionResponse = questionMapper.questionToQuestionResponseDto(question);

        if(question.getAnswers().size() == 0) {
            return new ResponseEntity<>(questionResponse, HttpStatus.OK);
        }
        else {
            QuestionDto.ResponseWithPage questionWithPageResponse = questionMapper.questionToQuestionResponseWithPageDto(question);
            List<Answer> answers = answerService.findAnswers(questionId);
            Page<Answer> pageAnswers = answerService.makePageObject(page -1, size, answers);
            //상기의 내용들을 바탕으로 Pageinfo를 구성해준다.
            PageInfo pageInfo = new PageInfo(page, size, pageAnswers.getTotalElements(), pageAnswers.getTotalPages());
            questionWithPageResponse.setPageInfos(pageInfo);

            if(sortOption.equals("newest")){
                List<AnswerDto.Response> answerResponses =
                        pageAnswers.getContent().stream()
                                .sorted(comparing(Answer::getId).reversed())
                                .map(answer -> answerMapper.answerToAnswerResponseDto(answer))
                                .collect(Collectors.toList());
                //정렬된 답변리스폰트Dtos로 질문ResponseDto 필드를 세팅한다.
                questionWithPageResponse.setAnswers(answerResponses);

                return new ResponseEntity<>(questionWithPageResponse, HttpStatus.OK);
            }
            else if(sortOption.equals("oldest")){
                List<AnswerDto.Response> answerResponses =
                        pageAnswers.getContent().stream()
                                .map(answer -> answerMapper.answerToAnswerResponseDto(answer))
                                .collect(Collectors.toList());
                //정렬된 답변리스폰트Dtos로 질문ResponseDto 필드를 세팅한다.
                questionWithPageResponse.setAnswers(answerResponses);
                return new ResponseEntity<>(questionWithPageResponse, HttpStatus.OK);
            }
        }
        return null;
    }
}

/**/
