package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Long>, CrudRepository<User, Long> {

    @Modifying
    @Query("update User u set u.name = :name, u.email = :email where u.id = :id")
    User update(@Param(value = "id") long id,
                @Param(value = "name") String name,
                @Param(value = "email") String email);
}
