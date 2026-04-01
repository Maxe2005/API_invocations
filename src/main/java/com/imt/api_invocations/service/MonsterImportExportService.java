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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.imt.api_invocations.client.ApiGenerateGatchaClient;
import com.imt.api_invocations.client.dto.gatcha.SignedUrlRequest;

@Service
public class MonsterImportExportService {

    private static final Logger log = LoggerFactory.getLogger(MonsterImportExportService.class);

    private final MonsterService monsterService;
    private final DtoMapperMonster dtoMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${assets.host:http://host.docker.internal:9000}")
    private String assetsHost;
    private static final int SIGNED_URL_BATCH = 40;

    private final ApiGenerateGatchaClient generateGatchaClient;

    public MonsterImportExportService(MonsterService monsterService, DtoMapperMonster dtoMapper,
            ApiGenerateGatchaClient generateGatchaClient) {
        this.monsterService = monsterService;
        this.dtoMapper = dtoMapper;
        this.generateGatchaClient = generateGatchaClient;
    }

    public void writeMonstersExport(OutputStream out, java.util.List<String> ids)
            throws IOException {
        log.info("Starting export");
        List<MonsterEntity> monsters = collectMonsters(ids);

        Map<String, MonsterEntity> withImage = filterMonstersWithImage(monsters);
        Map<String, byte[]> imageBytesById = new java.util.HashMap<>();
        Map<String, String> imageNameById = new java.util.HashMap<>();

        if (!withImage.isEmpty()) {
            var requests = buildSignedUrlRequests(withImage);
            var downloaded = fetchSignedAndDownloadImages(requests);
            imageBytesById.putAll(downloaded.imageBytes);
            imageNameById.putAll(downloaded.imageNames);
        }

        writeZip(out, monsters, imageBytesById, imageNameById);
    }

    private List<MonsterEntity> collectMonsters(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return monsterService.getAllMonsters(true);
        }
        List<MonsterEntity> monsters = new java.util.ArrayList<>();
        for (String id : ids) {
            MonsterEntity m = monsterService.getMonsterById(id, true);
            if (m != null) {
                monsters.add(m);
            }
        }
        return monsters;
    }

    private Map<String, MonsterEntity> filterMonstersWithImage(List<MonsterEntity> monsters) {
        Map<String, MonsterEntity> map = new java.util.LinkedHashMap<>();
        for (MonsterEntity m : monsters) {
            if (m.getImageUrl() != null && !m.getImageUrl().isBlank()) {
                map.put(m.getId(), m);
            }
        }
        return map;
    }

    private List<SignedUrlRequest> buildSignedUrlRequests(Map<String, MonsterEntity> withImage) {
        return withImage.values().stream()
                .map(m -> new SignedUrlRequest(m.getId(), m.getImageUrl())).toList();
    }

    private record DownloadResult(Map<String, byte[]> imageBytes, Map<String, String> imageNames) {
    }

    private DownloadResult fetchSignedAndDownloadImages(java.util.List<SignedUrlRequest> requests) {
        Map<String, byte[]> imageBytesById = new java.util.HashMap<>();
        Map<String, String> imageNameById = new java.util.HashMap<>();

        for (int i = 0; i < requests.size(); i += SIGNED_URL_BATCH) {
            int end = Math.min(i + SIGNED_URL_BATCH, requests.size());
            List<SignedUrlRequest> batch = requests.subList(i, end);
            try {
                var responses = generateGatchaClient.getSignedUrls(batch);
                for (var r : responses) {
                    String id = r.getId();
                    String signed = r.getSignedUrl();
                    if (signed != null && !signed.isBlank()) {
                        try {
                            byte[] img = restTemplate.getForObject(signed, byte[].class);
                            if (img != null && img.length > 0) {
                                imageBytesById.put(id, img);
                                String name = signed.split("\\?")[0];
                                name = name.substring(name.lastIndexOf('/') + 1);
                                imageNameById.put(id, name);
                            }
                        } catch (RestClientException ex) {
                            log.info("Could not fetch signed image {} : {}", signed,
                                    ex.getMessage());
                        }
                    }
                }
            } catch (Exception ex) {
                log.info("Error while requesting signed URLs: {}", ex.getMessage());
            }
        }

        return new DownloadResult(imageBytesById, imageNameById);
    }

    private void writeZip(OutputStream out, List<MonsterEntity> monsters,
            Map<String, byte[]> imageBytesById, Map<String, String> imageNameById)
            throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            for (MonsterEntity monster : monsters) {
                String folder = sanitizeFolder(
                        monster.getName() == null ? monster.getId() : monster.getName());

                byte[] jsonBytes = objectMapper
                        .writeValueAsBytes(dtoMapper.toGlobalMonsterWithIdDto(monster, true));
                zos.putNextEntry(new ZipEntry(folder + "/monster.json"));
                zos.write(jsonBytes);
                zos.closeEntry();

                if (imageBytesById.containsKey(monster.getId())) {
                    byte[] imageBytes = imageBytesById.get(monster.getId());
                    String imageName = imageNameById.get(monster.getId());
                    zos.putNextEntry(new ZipEntry(folder + "/" + imageName));
                    zos.write(imageBytes);
                    zos.closeEntry();
                } else if (monster.getImageUrl() != null) {
                    tryFallbackAndWrite(zos, monster);
                }
            }
            zos.finish();
        }
    }

    private void tryFallbackAndWrite(ZipOutputStream zos, MonsterEntity monster) {
        String original = monster.getImageUrl();
        java.util.List<String> candidates = new java.util.ArrayList<>();
        if (original.startsWith("http")) {
            candidates.add(original);
        } else if (original.startsWith("/")) {
            candidates.add(assetsHost + original);
        } else {
            candidates.add(assetsHost + "/" + original);
        }
        for (String url : candidates) {
            try {
                byte[] img = restTemplate.getForObject(url, byte[].class);
                if (img != null && img.length > 0) {
                    String name = url.substring(url.lastIndexOf('/') + 1);
                    zos.putNextEntry(new ZipEntry(sanitizeFolder(
                            monster.getName() == null ? monster.getId() : monster.getName()) + "/"
                            + name));
                    zos.write(img);
                    zos.closeEntry();
                    return;
                }
            } catch (RestClientException e) {
                log.info("Could not fetch fallback image {} : {}", url, e.getMessage());
            } catch (IOException e) {
                log.info("IO error writing fallback image for {}: {}", monster.getId(),
                        e.getMessage());
            }
        }
        log.info("No image included for monster {}", monster.getId());
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
                } else if (filename.toLowerCase().endsWith(".png")
                        || filename.toLowerCase().endsWith(".jpg")
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
                            log.info("Failed to upload image {}: {}", uploadUrl, ex.getMessage());
                        }
                        break;
                    }
                }
            } catch (Exception ex) {
                log.info("Skipping malformed monster json for folder {}: {}", e.getKey(),
                        ex.getMessage());
            }
        }

        return created;
    }

    private String sanitizeFolder(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9\\-_. ]", "").replace(' ', '-');
    }
}
