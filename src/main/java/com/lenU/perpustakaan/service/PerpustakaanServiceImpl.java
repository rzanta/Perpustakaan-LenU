package com.lenU.perpustakaan.service;

import com.lenU.perpustakaan.lib.*;
import com.lenU.perpustakaan.model.BukuEntity;
import com.lenU.perpustakaan.model.MahasiswaEntity;
import com.lenU.perpustakaan.model.PeminjamanEntity;
import com.lenU.perpustakaan.repository.BukuRepository;
import com.lenU.perpustakaan.repository.MahasiswaRepository;
import com.lenU.perpustakaan.repository.PeminjamanRepository;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class PerpustakaanServiceImpl extends PerpustakaanServiceGrpc.PerpustakaanServiceImplBase {

        @Autowired
        private MahasiswaRepository mahasiswaRepository;

        @Autowired
        private BukuRepository bukuRepository;

        @Autowired
        private PeminjamanRepository peminjamanRepository;

        // CRUD Mahasiswa
        @Override
        public void getMahasiswa(GetMahasiswaRequest request, StreamObserver<GetMahasiswaResponse> responseObserver) {
                System.out.println("getMahasiswa method called");

                MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findById(request.getNim())
                                .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));

                Mahasiswa mahasiswaResponse = Mahasiswa.newBuilder()
                                .setNama(mahasiswaEntity.getNama())
                                .setNim(mahasiswaEntity.getNim())
                                .setJurusan(mahasiswaEntity.getJurusan())
                                .build();

                GetMahasiswaResponse response = GetMahasiswaResponse.newBuilder().setMahasiswa(mahasiswaResponse)
                                .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void getAllMahasiswa(GetAllMahasiswaRequest request, StreamObserver<GetAllMahasiswaResponse> responseObserver) {
                System.out.println("getAllMahasiswa method called");

                List<MahasiswaEntity> mahasiswaEntities = mahasiswaRepository.findAll();

                List<Mahasiswa> mahasiswaList = mahasiswaEntities.stream()
                        .map(mahasiswaEntity -> Mahasiswa.newBuilder()
                                .setNama(mahasiswaEntity.getNama())
                                .setNim(mahasiswaEntity.getNim())
                                .setJurusan(mahasiswaEntity.getJurusan())
                                .build())
                        .collect(Collectors.toList());

                GetAllMahasiswaResponse response = GetAllMahasiswaResponse.newBuilder()
                                .addAllMahasiswa(mahasiswaList)
                                .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void createMahasiswa(CreateMahasiswaRequest request,
                        StreamObserver<CreateMahasiswaResponse> responseObserver) {
                MahasiswaEntity mahasiswaEntity = new MahasiswaEntity();
                mahasiswaEntity.setNim(request.getMahasiswa().getNim());
                mahasiswaEntity.setNama(request.getMahasiswa().getNama());
                mahasiswaEntity.setJurusan(request.getMahasiswa().getJurusan());

                mahasiswaRepository.save(mahasiswaEntity);

                CreateMahasiswaResponse response = CreateMahasiswaResponse.newBuilder()
                                .setMahasiswa(request.getMahasiswa())
                                .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void updateMahasiswa(UpdateMahasiswaRequest request,
                        StreamObserver<UpdateMahasiswaResponse> responseObserver) {
                try {
                        Mahasiswa mahasiswa = request.getMahasiswa();

                        MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findById(mahasiswa.getNim())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Mahasiswa not found: " + mahasiswa.getNim()));

                        if (!mahasiswaEntity.getNim().equals(mahasiswa.getNim())) {
                                responseObserver.onError(new RuntimeException("Cannot update NIM"));
                                return;
                        }

                        if (!mahasiswa.getNama().isEmpty()) {
                                mahasiswaEntity.setNama(mahasiswa.getNama());
                        }

                        // Check if jurusan is provided, if not, use the existing jurusan
                        if (!mahasiswa.getJurusan().isEmpty()) {
                                mahasiswaEntity.setJurusan(mahasiswa.getJurusan());
                        }

                        mahasiswaRepository.save(mahasiswaEntity);

                        Mahasiswa updatedMahasiswaResponse = Mahasiswa.newBuilder()
                                        .setNama(mahasiswaEntity.getNama())
                                        .setNim(mahasiswaEntity.getNim())
                                        .setJurusan(mahasiswaEntity.getJurusan())
                                        .build();

                        UpdateMahasiswaResponse response = UpdateMahasiswaResponse.newBuilder()
                                        .setMahasiswa(updatedMahasiswaResponse)
                                        .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public void deleteMahasiswa(DeleteMahasiswaRequest request,
                        StreamObserver<DeleteMahasiswaResponse> responseObserver) {
                System.out.println("deleteMahasiswa method called");

                String nim = request.getNim();
                try {
                        MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findById(nim)
                                        .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));

                        List<PeminjamanEntity> peminjamanEntities = peminjamanRepository.findByMahasiswaAndStatus(
                                        mahasiswaEntity, PeminjamanEntity.Status.BELUM_DIKEMBALIKAN);
                        if (!peminjamanEntities.isEmpty()) {
                                responseObserver.onError(new RuntimeException(
                                                "Mahasiswa cannot be deleted, there are unreturned books."));
                                return;
                        }

                        List<PeminjamanEntity> peminjamans = peminjamanRepository.findByMahasiswa(mahasiswaEntity);
                        if (!peminjamans.isEmpty()) {
                                peminjamanRepository.deleteAll(peminjamans);
                        }

                        mahasiswaRepository.delete(mahasiswaEntity);

                        DeleteMahasiswaResponse response = DeleteMahasiswaResponse.newBuilder().setSuccess(true)
                                        .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        System.err.println(e.getMessage());
                }
        }


        // CRUD Buku
        @Override
        public void getBuku(GetBukuRequest request, StreamObserver<GetBukuResponse> responseObserver) {
                System.out.println("getBuku method called");

                BukuEntity bukuEntity = bukuRepository.findById(request.getId())
                                .orElseThrow(() -> new RuntimeException("Buku not found"));

                Buku bukuResponse = Buku.newBuilder()
                                .setId(bukuEntity.getId())
                                .setJudul(bukuEntity.getJudul())
                                .setKuantitas(bukuEntity.getKuantitas())
                                .setPenulis(bukuEntity.getPenulis())
                                .setTempatPenyimpanan(bukuEntity.getTempatPenyimpanan())
                                .build();

                GetBukuResponse response = GetBukuResponse.newBuilder().setBuku(bukuResponse)
                                .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void getAllBuku(GetAllBukuRequest request,
                        StreamObserver<GetAllBukuResponse> responseObserver) {
                System.out.println("getAllMahasiswa method called");

                List<BukuEntity> bukuEntities = bukuRepository.findAll();

                List<Buku> bukuList = bukuEntities.stream()
                                .map(bukuEntity -> Buku.newBuilder()
                                                .setId(bukuEntity.getId())
                                                .setJudul(bukuEntity.getJudul())
                                                .setKuantitas(bukuEntity.getKuantitas())
                                                .setPenulis(bukuEntity.getPenulis())
                                                .setTempatPenyimpanan(bukuEntity.getTempatPenyimpanan())
                                                .build())
                                .collect(Collectors.toList());

                GetAllBukuResponse response = GetAllBukuResponse.newBuilder()
                                .addAllBuku(bukuList)
                                .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void createBuku(CreateBukuRequest request, StreamObserver<CreateBukuResponse> responseObserver) {
                System.out.println("createBuku method called");
                Buku bukuRequest = request.getBuku();
                BukuEntity bukuEntity = new BukuEntity(
                                bukuRequest.getJudul(),
                                bukuRequest.getPenulis(),
                                bukuRequest.getKuantitas(),
                                bukuRequest.getTempatPenyimpanan());

                BukuEntity savedBuku = bukuRepository.save(bukuEntity);

                Buku bukuResponse = Buku.newBuilder()
                                .setJudul(savedBuku.getJudul())
                                .setPenulis(savedBuku.getPenulis())
                                .setKuantitas(savedBuku.getKuantitas())
                                .setTempatPenyimpanan(savedBuku.getTempatPenyimpanan())
                                .build();

                CreateBukuResponse response = CreateBukuResponse.newBuilder().setBuku(bukuResponse).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void updateBuku(UpdateBukuRequest request, StreamObserver<UpdateBukuResponse> responseObserver) {
                try {
                        Buku buku = request.getBuku();

                        BukuEntity bukuEntity = bukuRepository.findById(buku.getId())
                                        .orElseThrow(() -> new RuntimeException("Buku not found: " + buku.getId()));

                        if (!buku.getJudul().isEmpty()) {
                                bukuEntity.setJudul(buku.getJudul());
                        }

                        if (!buku.getPenulis().isEmpty()) {
                                bukuEntity.setPenulis(buku.getPenulis());
                        }

                        if (buku.getKuantitas() != 0) {
                                bukuEntity.setKuantitas(buku.getKuantitas());
                        }

                        if (!buku.getTempatPenyimpanan().isEmpty()) {
                                bukuEntity.setTempatPenyimpanan(buku.getTempatPenyimpanan());
                        }

                        bukuRepository.save(bukuEntity);

                        Buku updatedBukuResponse = Buku.newBuilder()
                                        .setId(bukuEntity.getId())
                                        .setJudul(bukuEntity.getJudul())
                                        .setPenulis(bukuEntity.getPenulis())
                                        .setKuantitas(bukuEntity.getKuantitas())
                                        .setTempatPenyimpanan(bukuEntity.getTempatPenyimpanan())
                                        .build();

                        UpdateBukuResponse response = UpdateBukuResponse.newBuilder()
                                        .setBuku(updatedBukuResponse)
                                        .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        System.err.println(e.getMessage());
                }
        }

        @Override
        public void deleteBuku(DeleteBukuRequest request, StreamObserver<DeleteBukuResponse> responseObserver) {
                System.out.println("deleteBuku method called");

                Long id = request.getId();
                try {
                        BukuEntity bukuEntity = bukuRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Buku not found"));

                        List<PeminjamanEntity> peminjamanEntities = peminjamanRepository.findByBukuAndStatus(bukuEntity,
                                        PeminjamanEntity.Status.BELUM_DIKEMBALIKAN);
                        if (!peminjamanEntities.isEmpty()) {
                                responseObserver.onError(new RuntimeException(
                                                "Buku cannot be deleted, there are unreturned books."));
                                return;
                        }

                        List<PeminjamanEntity> peminjamans = peminjamanRepository.findByBuku(bukuEntity);
                        if (!peminjamans.isEmpty()) {
                                peminjamanRepository.deleteAll(peminjamans);
                        }

                        bukuRepository.delete(bukuEntity);

                        DeleteBukuResponse response = DeleteBukuResponse.newBuilder().setSuccess(true).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        System.err.println(e.getMessage());
                }
        }


        // CRUD Peminjaman
        @Override
        public void getPeminjaman(GetPeminjamanRequest request, StreamObserver<GetPeminjamanResponse> responseObserver) {
                System.out.println("getPeminjaman method called");

                PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(request.getId())
                                .orElseThrow(() -> new RuntimeException("Peminjaman not found"));

                Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                                .setId(peminjamanEntity.getId())
                                .setMahasiswaNim(peminjamanEntity.getMahasiswa().getNim())
                                .setBukuId(peminjamanEntity.getBuku().getId())
                                .setTanggalPeminjaman(
                                                peminjamanEntity.getTanggalPeminjaman().toString())
                                .setTanggalBatasPengembalian(peminjamanEntity
                                                .getTanggalBatasPengembalian().toString())
                                .setTanggalPengembalian(peminjamanEntity
                                                .getTanggalPengembalian() != null ? peminjamanEntity
                                                                .getTanggalPengembalian().toString()
                                                                : "")
                                .setStatus(peminjamanEntity.getStatus().name())
                                .build();

                GetPeminjamanResponse response = GetPeminjamanResponse.newBuilder().setPeminjaman(peminjamanResponse)
                                .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void getAllPeminjaman(GetAllPeminjamanRequest request,
                        StreamObserver<GetAllPeminjamanResponse> responseObserver) {
                System.out.println("getAllPeminjaman method called");

                List<PeminjamanEntity> peminjamanEntities = peminjamanRepository.findAll();

                List<Peminjaman> peminjamanList = peminjamanEntities.stream()
                                .map(peminjamanEntity -> Peminjaman.newBuilder()
                                                .setId(peminjamanEntity.getId())
                                                .setMahasiswaNim(peminjamanEntity.getMahasiswa().getNim())
                                                .setBukuId(peminjamanEntity.getBuku().getId())
                                                .setTanggalPeminjaman(
                                                                peminjamanEntity.getTanggalPeminjaman().toString())
                                                .setTanggalBatasPengembalian(peminjamanEntity
                                                                .getTanggalBatasPengembalian().toString())
                                                .setTanggalPengembalian(peminjamanEntity
                                                                .getTanggalPengembalian() != null ? peminjamanEntity
                                                                                .getTanggalPengembalian().toString()
                                                                                : "")
                                                .setStatus(peminjamanEntity.getStatus().name())
                                                .build())
                                .collect(Collectors.toList());

                GetAllPeminjamanResponse response = GetAllPeminjamanResponse.newBuilder()
                                .addAllPeminjaman(peminjamanList)
                                .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void createPeminjaman(CreatePeminjamanRequest request,
                        StreamObserver<CreatePeminjamanResponse> responseObserver) {
                try {
                        System.out.println("createPeminjaman method called");

                        Peminjaman peminjamanRequest = request.getPeminjaman();

                        System.out.println("Received peminjaman request: " + peminjamanRequest);

                        MahasiswaEntity mahasiswaEntity = mahasiswaRepository
                                        .findById(peminjamanRequest.getMahasiswaNim())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Mahasiswa not found: " + peminjamanRequest.getMahasiswaNim()));

                        System.out.println("Found Mahasiswa: " + mahasiswaEntity);

                        BukuEntity bukuEntity = bukuRepository.findById(peminjamanRequest.getBukuId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Buku not found: " + peminjamanRequest.getBukuId()));

                        System.out.println("Found Buku: " + bukuEntity);

                        if (bukuEntity.getKuantitas() <= 0) {
                                throw new RuntimeException("Buku not available for borrowing");
                        }

                        bukuEntity.setKuantitas(bukuEntity.getKuantitas() - 1);
                        bukuRepository.save(bukuEntity);

                        LocalDate tanggalPeminjaman = LocalDate.parse(peminjamanRequest.getTanggalPeminjaman());

                        // Cek jumlah peminjaman dalam satu bulan dengan status BELUM_DIKEMBALIKAN
                        long jumlahPeminjamanDalamBulan = peminjamanRepository
                                        .countByMahasiswaNimAndTanggalPeminjamanBetweenAndStatus(
                                                        mahasiswaEntity.getNim(),
                                                        tanggalPeminjaman.withDayOfMonth(1),
                                                        tanggalPeminjaman.withDayOfMonth(
                                                                        tanggalPeminjaman.lengthOfMonth()),
                                                        PeminjamanEntity.Status.BELUM_DIKEMBALIKAN);

                        if (jumlahPeminjamanDalamBulan == 10) {
                                System.out.println("Mahasiswa sudah meminjam 10 buku dalam bulan ini");
                                responseObserver.onError(new RuntimeException(
                                                "Mahasiswa sudah meminjam 10 buku dalam bulan ini"));
                                return;
                        }

                        LocalDate tanggalPengembalian = peminjamanRequest.getTanggalPengembalian().isEmpty() ? null
                                        : LocalDate.parse(peminjamanRequest.getTanggalPengembalian());

                        PeminjamanEntity peminjamanEntity = new PeminjamanEntity(
                                        mahasiswaEntity,
                                        bukuEntity,
                                        LocalDate.parse(peminjamanRequest.getTanggalPeminjaman()),
                                        LocalDate.parse(peminjamanRequest.getTanggalBatasPengembalian()),
                                        tanggalPengembalian,
                                        PeminjamanEntity.Status.BELUM_DIKEMBALIKAN);

                        System.out.println("PeminjamanEntity to be saved: " + peminjamanEntity);

                        PeminjamanEntity savedPeminjaman = peminjamanRepository.save(peminjamanEntity);

                        System.out.println("Saved peminjaman entity: " + savedPeminjaman);

                        Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                                        .setMahasiswaNim(savedPeminjaman.getMahasiswa().getNim())
                                        .setBukuId(savedPeminjaman.getBuku().getId())
                                        .setTanggalPeminjaman(savedPeminjaman.getTanggalPeminjaman().toString())
                                        .setTanggalBatasPengembalian(
                                                        savedPeminjaman.getTanggalBatasPengembalian().toString())
                                        .setTanggalPengembalian(savedPeminjaman.getTanggalPengembalian() != null
                                                        ? savedPeminjaman.getTanggalPengembalian().toString()
                                                        : "")
                                        .setStatus(savedPeminjaman.getStatus().name())
                                        .build();

                        CreatePeminjamanResponse response = CreatePeminjamanResponse.newBuilder()
                                        .setPeminjaman(peminjamanResponse)
                                        .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public void updatePeminjaman(UpdatePeminjamanRequest request,
                        StreamObserver<UpdatePeminjamanResponse> responseObserver) {
                System.out.println("updatePeminjaman method called");

                Peminjaman peminjamanRequest = request.getPeminjaman();
                PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(peminjamanRequest.getId())
                                .orElseThrow(() -> new RuntimeException("Peminjaman not found"));

                if (peminjamanRequest.getMahasiswaNim() != null) {
                        MahasiswaEntity mahasiswaEntity = mahasiswaRepository
                                        .findById(peminjamanRequest.getMahasiswaNim())
                                        .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));
                        peminjamanEntity.setMahasiswa(mahasiswaEntity);
                }

                if (peminjamanRequest.getBukuId() != 0) {
                        BukuEntity bukuEntity = bukuRepository.findById(peminjamanRequest.getBukuId())
                                        .orElseThrow(() -> new RuntimeException("Buku not found"));

                        boolean isBukuIdChanged = !peminjamanEntity.getBuku().getId()
                                        .equals(peminjamanRequest.getBukuId());

                        if (isBukuIdChanged) {
                                if (bukuEntity.getKuantitas() < 1) {
                                        responseObserver.onError(
                                                        new RuntimeException("Buku not available for borrowing"));
                                        return;
                                }

                                BukuEntity bukuLama = peminjamanEntity.getBuku();
                                bukuLama.setKuantitas(bukuLama.getKuantitas() + 1);
                                bukuRepository.save(bukuLama);

                                bukuEntity.setKuantitas(bukuEntity.getKuantitas() - 1);
                                bukuRepository.save(bukuEntity);

                                peminjamanEntity.setBuku(bukuEntity);
                        }
                }

                if (!peminjamanRequest.getTanggalPeminjaman().isEmpty()) {
                        peminjamanEntity.setTanggalPeminjaman(
                                        LocalDate.parse(peminjamanRequest.getTanggalPeminjaman()));
                }

                if (!peminjamanRequest.getTanggalBatasPengembalian().isEmpty()) {
                        peminjamanEntity.setTanggalBatasPengembalian(
                                        LocalDate.parse(peminjamanRequest.getTanggalBatasPengembalian()));
                }

                peminjamanRepository.save(peminjamanEntity);

                Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                                .setId(peminjamanEntity.getId())
                                .setMahasiswaNim(peminjamanEntity.getMahasiswa().getNim())
                                .setBukuId(peminjamanEntity.getBuku().getId())
                                .setTanggalPeminjaman(peminjamanEntity.getTanggalPeminjaman().toString())
                                .setTanggalBatasPengembalian(peminjamanEntity.getTanggalBatasPengembalian().toString())
                                .setStatus(peminjamanEntity.getStatus().name())
                                .build();

                UpdatePeminjamanResponse response = UpdatePeminjamanResponse.newBuilder()
                                .setPeminjaman(peminjamanResponse).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        public void deletePeminjaman(DeletePeminjamanRequest request,
                        StreamObserver<DeletePeminjamanResponse> responseObserver) {
                try {
                        Long id = request.getId();

                        PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Peminjaman not found: " + id));

                        if (peminjamanEntity.getStatus() == PeminjamanEntity.Status.BELUM_DIKEMBALIKAN) {
                                BukuEntity bukuEntity = peminjamanEntity.getBuku();
                                bukuEntity.setKuantitas(bukuEntity.getKuantitas() + 1);
                                bukuRepository.save(bukuEntity);
                        }

                        peminjamanRepository.delete(peminjamanEntity);

                        DeletePeminjamanResponse response = DeletePeminjamanResponse.newBuilder()
                                        .setSuccess(true)
                                        .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public void pengembalianBuku(PengembalianBukuRequest request,
                        StreamObserver<PengembalianBukuResponse> responseObserver) {
                System.out.println("pengembalianBuku method called");

                Long peminjamanId = request.getPeminjamanId();
                LocalDate tanggalPengembalian = LocalDate.parse(request.getTanggalPengembalian());

                try {
                        PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(peminjamanId)
                                        .orElseThrow(() -> new RuntimeException("Peminjaman not found"));

                        if (peminjamanEntity.getStatus() == PeminjamanEntity.Status.SUDAH_DIKEMBALIKAN) {
                                responseObserver.onError(new RuntimeException("Peminjaman sudah dikembalikan"));
                                return;
                        }

                        peminjamanEntity.setTanggalPengembalian(tanggalPengembalian);
                        peminjamanEntity.setStatus(PeminjamanEntity.Status.SUDAH_DIKEMBALIKAN);
                        peminjamanRepository.save(peminjamanEntity);

                        BukuEntity bukuEntity = peminjamanEntity.getBuku();
                        bukuEntity.setKuantitas(bukuEntity.getKuantitas() + 1);
                        bukuRepository.save(bukuEntity);

                        int denda = peminjamanEntity.hitungDenda(tanggalPengembalian);

                        Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                                        .setId(peminjamanEntity.getId())
                                        .setMahasiswaNim(peminjamanEntity.getMahasiswa().getNim())
                                        .setBukuId(peminjamanEntity.getBuku().getId())
                                        .setTanggalPeminjaman(peminjamanEntity.getTanggalPeminjaman().toString())
                                        .setTanggalBatasPengembalian(
                                                        peminjamanEntity.getTanggalBatasPengembalian().toString())
                                        .setTanggalPengembalian(peminjamanEntity.getTanggalPengembalian().toString())
                                        .setStatus(peminjamanEntity.getStatus().name())
                                        .build();

                        PengembalianBukuResponse response = PengembalianBukuResponse.newBuilder()
                                        .setPeminjaman(peminjamanResponse)
                                        .setDenda(denda)
                                        .build();

                        System.out.println("PengembalianBukuResponse built successfully");

                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        System.err.println("Error returning book: " + e.getMessage());
                        responseObserver.onError(new RuntimeException("Error returning book: " + e.getMessage()));
                }
        }
}