package backend.com.backend.answer.entity;

import backend.com.backend.audit.Auditable;
import backend.com.backend.comment.entity.Comment;
import backend.com.backend.member.entity.Member;
import backend.com.backend.question.entity.Question;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Answer extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column
    private String writtenBy;


    @JsonBackReference("question-answers")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUESTION_ID")
    private Question question;

    @JsonManagedReference("answer-comments")
    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public void setComment(Comment comment) {
        comments.add(comment);
        if(comment.getAnswer() != this){
            comment.setAnswer(this);
        }
    }
}
//writtenBy, totalQuestions totalAnswers view 삭제