package com.assessment.demo.repository;

import com.assessment.demo.entity.Notify;
import com.assessment.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotifyRepository extends JpaRepository<Notify, UUID> {

    List<Notify> getByUser(User user);
}
