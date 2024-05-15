package com.lenU.perpustakaan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lenU.perpustakaan.model.BukuEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface BukuRepository extends JpaRepository<BukuEntity, Long> {
}
