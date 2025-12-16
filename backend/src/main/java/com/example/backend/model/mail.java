package com.example.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.example.backend.DTOS.attachementDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class mail implements Cloneable {
    private int id;
    private Queue<String> to;
    private String from;
    private String subject;
    private String body;
    private String preview;
    private boolean starred;
    private boolean hasAttachment;
    private LocalDateTime timestamp;
    private int priority;
    private List<attachementDTO> attachments;
    private LocalDateTime trashedAt;
    private String folder; 
    // ADD THIS FIELD - for custom folder support
    private String customFolderId;

    // null = system folder, "folder_123" = custom folder
    @Override
    public Object clone() throws CloneNotSupportedException {
        mail cloned = (mail) super.clone();

        // Deep copy for mutable objects
        if (this.attachments != null) {
            cloned.attachments = new ArrayList<>(this.attachments);
        }

        // LocalDateTime is immutable, so no need to clone
        // Strings are also immutable

        return cloned;
    }
}
