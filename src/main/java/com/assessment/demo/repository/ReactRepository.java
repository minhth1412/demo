package com.assessment.demo.repository;

import com.assessment.demo.entity.React;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReactRepository extends JpaRepository<React, UUID> {

}
