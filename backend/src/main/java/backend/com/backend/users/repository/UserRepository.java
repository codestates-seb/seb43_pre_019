package backend.com.backend.users.repository;

import backend.com.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Integer, User> {

}
