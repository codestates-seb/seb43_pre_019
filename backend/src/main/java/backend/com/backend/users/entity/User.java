package backend.com.backend.users.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private int id;
    private String email;
    private String full_name;
    private String display_name;
    private String password;
    private String location;
    private int total_questions;
    private int total_answers;
    private UserStatus user_status = UserStatus.USER_ACTIVE;
    public enum UserStatus{
        USER_ACTIVE("활동 중"),
        USER_SLEEP("휴면 상태"),
        USER_QUIT("탈퇴 상태");
        @Getter
        private String statusDesciption;

        UserStatus(String statusDesciption) {
            this.statusDesciption = statusDesciption;
        }
    }
}
