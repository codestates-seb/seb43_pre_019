package backend.com.backend.member.entity;

import javax.persistence.*;


import backend.com.backend.answer.entity.Answer;
import backend.com.backend.audit.Auditable;
import backend.com.backend.comment.entity.Comment;
import backend.com.backend.question.entity.Question;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Member extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, updatable = false, unique = true)
    private String email;
    @Column(nullable = true, updatable = true)
    private String fullName;
    @Column(nullable = false, unique = true)
    private String displayName;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(updatable = true)
    private String location;

    @Column
    private int totalQuestions;
    @Column
    private int totalAnswers;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Enumerated(value=EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;
//    @OneToMany(mappedBy = "user")
//    private List<Question> question;
//
//    @OneToMany(mappedBy = "user")
//    private List<Answer> answer;
//
//    @OneToMany(mappedBy ="user")
//    private List<Comment> comment;
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnoreProperties("answers")
    private List<Question> questions = new ArrayList<>();

    public void setQuestion(Question question) {
        questions.add(question);
        totalQuestions++;
        if(question.getMember() != this){
            question.setMember(this);
        }
    }
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnoreProperties("comments")
    private List<Answer> answers = new ArrayList<>();

    public void setAnswer(Answer answer) {
        answers.add(answer);
        totalAnswers++;
        if(answer.getMember() != this){
            answer.setMember(this);
        }
    }
    @OneToMany(mappedBy = "member")
    @JsonManagedReference
    @JsonIgnoreProperties({"member", "answer"})
    private List<Comment> comments;

    public void setComment(Comment comment) {
        comments.add(comment);
        if(comment.getMember() != this){
            comment.setMember(this);
        }
    }

    /*@OneToMany
    private User_anal useranal;*/
    public enum MemberStatus{
        MEMBER_ACTIVE("활동 중"),
        MEMBER_SLEEP("휴면 상태"),
        MEMBER_QUIT("탈퇴 상태");
        @Getter
        private String statusDescription;

        MemberStatus(String statusDescription) {
            this.statusDescription = statusDescription;
        }
    }
}