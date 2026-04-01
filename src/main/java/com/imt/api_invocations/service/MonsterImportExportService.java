package com.imt.api_invocations.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imt.api_invocations.controller.mapper.DtoMapperMonster;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class MonsterImportExportService {

    private static final Logger log = LoggerFactory.getLogger(MonsterImportExportService.class);

    private final MonsterService monsterService;
    private final DtoMapperMonster dtoMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public MonsterImportExportService(MonsterService monsterService, DtoMapperMonster dtoMapper) {
        this.monsterService = monsterService;
        this.dtoMapper = dtoMapper;
    }

    public void writeMonstersExport(OutputStream out, java.util.List<String> ids) throws IOException {
        List<MonsterEntity> monsters;
        if (ids == null || ids.isEmpty()) {
            monsters = monsterService.getAllMonsters(true);
        } else {
            monsters = new java.util.ArrayList<>();
            for (String id : ids) {
                MonsterEntity m = monsterService.getMonsterById(id, true);
                if (m != null) {
                    monsters.add(m);
                }
            }
        }
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            for (MonsterEntity monster : monsters) {
                String folder = sanitizeFolder(monster.getName() == null ? monster.getId() : monster.getName());

                byte[] jsonBytes = objectMapper.writeValueAsBytes(dtoMapper.toGlobalMonsterWithIdDto(monster, true));
                zos.putNextEntry(new ZipEntry(folder + "/monster.json"));
                zos.write(jsonBytes);
                zos.closeEntry();

                if (monster.getImageUrl() != null) {
                    String highRes = monster.getImageUrl().replace("/game-assets/", "/raw-assets/monsters/")
                            .replaceAll("\\.webp$", ".png");
                    try {
                        byte[] imageBytes = restTemplate.getForObject(highRes, byte[].class);
                        if (imageBytes != null && imageBytes.length > 0) {
                            String imageName = highRes.substring(highRes.lastIndexOf('/') + 1);
                            zos.putNextEntry(new ZipEntry(folder + "/" + imageName));
                            zos.write(imageBytes);
                            zos.closeEntry();
                        }
                    } catch (RestClientException e) {
                        log.debug("Could not fetch image {} : {}", highRes, e.getMessage());
                    }
                }
            }
            zos.finish();
        }
    }

    public int importMonstersFromStream(InputStream is) throws IOException {
        Map<String, byte[]> jsonMap = new HashMap<>();
        Map<String, byte[]> imageMap = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                String name = entry.getName();
                int idx = name.indexOf('/');
                String folder = idx > 0 ? name.substring(0, idx) : "root";
                String filename = idx > 0 ? name.substring(idx + 1) : name;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] data = baos.toByteArray();

                if ("monster.json".equalsIgnoreCase(filename)) {
                    jsonMap.put(folder, data);
                } else if (filename.toLowerCase().endsWith(".png") || filename.toLowerCase().endsWith(".jpg")
                        || filename.toLowerCase().endsWith(".jpeg")) {
                    imageMap.put(folder + "/" + filename, data);
                }
                zis.closeEntry();
            }
        }

        int created = 0;
        for (Map.Entry<String, byte[]> e : jsonMap.entrySet()) {
            try {
                var dto = objectMapper.readValue(e.getValue(),
                        com.imt.api_invocations.controller.dto.input.MonsterHttpDto.class);
                monsterService.createMonster(dtoMapper.toMonsterEntity(dto));
                created++;

                for (var imgKey : imageMap.keySet()) {
                    if (imgKey.startsWith(e.getKey() + "/")) {
                        String imageName = imgKey.substring(imgKey.indexOf('/') + 1);
                        String uploadUrl = "http://localhost:9000/raw-assets/monsters/" + imageName;
                        try {
                            restTemplate.put(uploadUrl, imageMap.get(imgKey));
                        } catch (RestClientException ex) {
                            log.debug("Failed to upload image {}: {}", uploadUrl, ex.getMessage());
                        }
                        break;
                    }
                }
            } catch (Exception ex) {
                log.debug("Skipping malformed monster json for folder {}: {}", e.getKey(), ex.getMessage());
            }
        }

        return created;
    }

    private String sanitizeFolder(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9\\-_. ]", "").replace(' ', '-');
    }
}
