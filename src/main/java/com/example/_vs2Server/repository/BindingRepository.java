package com.example._vs2Server.repository;

import com.example._vs2Server.model.Binding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BindingRepository extends JpaRepository<Binding, Long> {

}