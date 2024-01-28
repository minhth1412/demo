package com.assessment.demo.repository;

import com.assessment.demo.entity.Notify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotifyRepository extends JpaRepository<Notify, UUID> {

}
