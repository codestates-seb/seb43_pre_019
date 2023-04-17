package backend.com.backend.users.mapper;

import backend.com.backend.users.dto.UserPatchDto;
import backend.com.backend.users.dto.UserPostDto;
import backend.com.backend.users.dto.UserResponseDto;
import backend.com.backend.users.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    User userpostDtoToUser(UserPostDto userPostDto);
    User userPatchDtoToUser(UserPatchDto userPatchDto);

    User userTOResponseDto(UserResponseDto userResponseDto);

}
