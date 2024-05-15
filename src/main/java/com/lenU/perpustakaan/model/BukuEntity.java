package com.lenU.perpustakaan.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "buku")
public class BukuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String judul;
    private String penulis;
    private int kuantitas;
    private String tempatPenyimpanan;

    public BukuEntity(String judul, String penulis, int kuantitas, String tempatPenyimpanan) {
        this.judul = judul;
        this.penulis = penulis;
        this.kuantitas = kuantitas;
        this.tempatPenyimpanan = tempatPenyimpanan;
    }
}
