package com.lenU.perpustakaan.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lenU.perpustakaan.model.BukuEntity;
import com.lenU.perpustakaan.model.MahasiswaEntity;
import com.lenU.perpustakaan.model.PeminjamanEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PeminjamanRepository extends JpaRepository<PeminjamanEntity, Long> {
    List<PeminjamanEntity> findByMahasiswaAndStatus(MahasiswaEntity mahasiswa, PeminjamanEntity.Status status);

    List<PeminjamanEntity> findByBukuAndStatus(BukuEntity buku, PeminjamanEntity.Status status);

    List<PeminjamanEntity> findByMahasiswa(MahasiswaEntity mahasiswa);

    List<PeminjamanEntity> findByBuku(BukuEntity buku);

    void deleteByMahasiswa(MahasiswaEntity mahasiswa);

    void deleteByBuku(BukuEntity buku);

    long countByMahasiswaNimAndTanggalPeminjamanBetweenAndStatus(String mahasiswaNim, LocalDate startDate,
            LocalDate endDate, PeminjamanEntity.Status status);
}
