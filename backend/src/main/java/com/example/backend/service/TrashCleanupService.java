package com.example.backend.service;

import com.example.backend.Util.JsonFileManager;
import com.example.backend.model.mail;
import com.google.gson.reflect.TypeToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrashCleanupService {

    private final JsonFileManager jsonFileManager;
    private final String BASE_PATH = "data/users/";
    private static final Type MAIL_LIST_TYPE = new TypeToken<List<mail>>(){}.getType();
    private static final int DAYS_TO_KEEP = 30;

    public TrashCleanupService(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    //@Scheduled(cron = "0 */2 * * * *")
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldTrashEmails() {
        System.out.println("=== TRASH CLEANUP STARTED ===");
        System.out.println("Time: " + LocalDateTime.now());

        try {
            File usersDir = new File(BASE_PATH);
            File[] userFolders = usersDir.listFiles(File::isDirectory);

            if (userFolders == null || userFolders.length == 0) {
                System.out.println("No user folders found");
                return;
            }

            int totalCleaned = 0;

            for (File userFolder : userFolders) {
                String userEmail = userFolder.getName();
                int cleaned = cleanUserTrash(userEmail);
                totalCleaned += cleaned;
            }

            System.out.println("=== TRASH CLEANUP COMPLETED ===");
            System.out.println("Total emails deleted: " + totalCleaned);

        } catch (Exception e) {
            System.err.println("Error during trash cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int cleanUserTrash(String userEmail) {
        String trashPath = BASE_PATH + userEmail + "/trash.json";

        try {
            List<mail> trashEmails = jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);

            if (trashEmails == null || trashEmails.isEmpty()) {
                return 0;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DAYS_TO_KEEP);
            int originalSize = trashEmails.size();

            // Filter emails - keep only those newer than 30 days
            List<mail> filteredEmails = trashEmails.stream()
                    .filter(email -> {
                        LocalDateTime trashedDate = email.getTrashedAt();

                        if (trashedDate == null) {
                            trashedDate = email.getTimestamp();
                        }

                        return trashedDate.isAfter(cutoffDate);
                    })
                    .collect(Collectors.toList());

            int deletedCount = originalSize - filteredEmails.size();

            if (deletedCount > 0) {
                jsonFileManager.writeListToFile(trashPath, filteredEmails);
                System.out.println("User: " + userEmail + " - Deleted " + deletedCount + " old emails");
            }

            return deletedCount;

        } catch (Exception e) {
            System.err.println("Error cleaning trash for user " + userEmail + ": " + e.getMessage());
            return 0;
        }
    }
}