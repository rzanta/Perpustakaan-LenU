package com.lenU.perpustakaan.scheduler;

import com.lenU.perpustakaan.controller.NotificationWebSocketHandler;
import com.lenU.perpustakaan.model.PeminjamanEntity;
import com.lenU.perpustakaan.repository.PeminjamanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PeminjamanScheduler {

    private final PeminjamanRepository peminjamanRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public PeminjamanScheduler(PeminjamanRepository peminjamanRepository,
            NotificationWebSocketHandler notificationWebSocketHandler) {
        this.peminjamanRepository = peminjamanRepository;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkOverduePeminjaman() {
        LocalDate today = LocalDate.now();
        List<PeminjamanEntity> overduePeminjaman = peminjamanRepository
                .findByTanggalBatasPengembalianBeforeAndStatus(today, PeminjamanEntity.Status.BELUM_DIKEMBALIKAN);

        for (PeminjamanEntity peminjaman : overduePeminjaman) {
            String message = "Peminjaman ID " + peminjaman.getId() + " oleh Mahasiswa ID "
                    + peminjaman.getMahasiswa().getNim() + " telah melewati tanggal batas pengembalian.";
            notificationWebSocketHandler.sendNotification(message);
        }
    }
}