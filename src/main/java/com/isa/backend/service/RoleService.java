package com.isa.backend.service;

import com.isa.backend.model.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    Role findById(Long id);
    List<Role> findByName(String name);
}
