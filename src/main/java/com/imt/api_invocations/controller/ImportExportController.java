package com.imt.api_invocations.controller;

import com.imt.api_invocations.service.MonsterImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/invocation/monsters")
@RequiredArgsConstructor
@Tag(name = "Monsters Import/Export", description = "Import et export des monstres en archive ZIP")
public class ImportExportController {

    private final MonsterImportExportService importExportService;

    @Operation(summary = "Exporter les monstres en ZIP")
    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportMonsters(
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.util.List<String> ids) {
        StreamingResponseBody stream = outputStream -> importExportService.writeMonstersExport(outputStream, ids);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=monsters-export.zip");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(stream);
    }

    @Operation(summary = "Importer des monstres depuis un ZIP")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Integer>> importMonsters(@RequestParam("file") MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("imported", 0));
        }
        int created = importExportService.importMonstersFromStream(file.getInputStream());
        return ResponseEntity.ok(Map.of("imported", created));
    }
}
