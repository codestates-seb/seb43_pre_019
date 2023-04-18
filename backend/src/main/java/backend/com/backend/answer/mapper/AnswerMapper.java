package backend.com.backend.answer.mapper;

import backend.com.backend.answer.dto.AnswerDto;
import backend.com.backend.answer.entity.Answer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    Answer answerPostDtoToAnswer(AnswerDto.Post requestBody);
    AnswerDto.Response answerToAnswerResponseDto(Answer answer);
}