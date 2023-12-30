package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
