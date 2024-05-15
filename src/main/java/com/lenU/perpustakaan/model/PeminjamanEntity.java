package com.lenU.perpustakaan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "peminjaman")
public class PeminjamanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mahasiswa_id", referencedColumnName = "nim")
    private MahasiswaEntity mahasiswa;

    @ManyToOne
    @JoinColumn(name = "buku_id")
    private BukuEntity buku;

    private LocalDate tanggalPeminjaman;
    private LocalDate tanggalBatasPengembalian;
    private LocalDate tanggalPengembalian;

    public enum Status {
        BELUM_DIKEMBALIKAN,
        SUDAH_DIKEMBALIKAN
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    public PeminjamanEntity(MahasiswaEntity mahasiswa, BukuEntity buku, LocalDate tanggalPeminjaman,
            LocalDate tanggalBatasPengembalian, LocalDate tanggalPengembalian, Status status) {
        this.mahasiswa = mahasiswa;
        this.buku = buku;
        this.tanggalPeminjaman = tanggalPeminjaman;
        this.tanggalBatasPengembalian = tanggalBatasPengembalian;
        this.tanggalPengembalian = tanggalPengembalian;
        this.status = status;
    }

    public int hitungDenda(LocalDate tanggalPengembalian) {
        if (tanggalPengembalian.isBefore(this.tanggalBatasPengembalian) || tanggalPengembalian.isEqual(this.tanggalBatasPengembalian)) {
            return 0;
        }

        long daysLate = ChronoUnit.DAYS.between(this.tanggalBatasPengembalian, tanggalPengembalian);
        int denda = 0;
        int multiplier = 1;

        for (int i = 1; i <= daysLate; i++) {
            denda += 1000 * multiplier;
            if (i % 2 == 0) {
                multiplier++;
            }
        }
        
        return denda;
    }
}
