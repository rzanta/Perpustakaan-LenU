package com.lenU.perpustakaan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lenU.perpustakaan.model.MahasiswaEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface MahasiswaRepository extends JpaRepository<MahasiswaEntity, String> {
}
