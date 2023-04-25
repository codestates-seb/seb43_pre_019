package backend.com.backend.comment.entity;

import backend.com.backend.answer.entity.Answer;
import backend.com.backend.audit.Auditable;
import backend.com.backend.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Comment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3000)
    private String text;

    @Column
    private String writtenBy;

    @JsonBackReference("answer-comments")
    @ManyToOne
    @JoinColumn(name = "ANSWER_ID")
    private Answer answer;
}
