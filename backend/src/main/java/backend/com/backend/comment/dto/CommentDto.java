package backend.com.backend.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class CommentDto {
    @Getter
    public static class Post {
        @NotBlank(message = "원하시는 댓글내용을 입력해주세요.")
        private String text;

    }
    @Getter
    @Setter
    public static class Patch {
        private long commentId;

        @NotBlank(message = "댓글내용을 입력해주세요.")
        private String text;

        public void setCommentId(long commentId) {
            this.commentId = commentId;
        }
    }
    @Getter
    @Setter
    public static class Response {
        private long id;
        private String text;
        private String writtenBy;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
