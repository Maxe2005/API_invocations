package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.client.ApiGenerateGatchaClient;
import com.imt.api_invocations.client.dto.gatcha.SignedUrlResponse;
import com.imt.api_invocations.config.ImportProperties;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperMonster;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonsterImportExportService - Tests Unitaires")
class MonsterImportExportServiceTest {

  @Mock private MonsterService monsterService;

  @Mock private DtoMapperMonster dtoMapper;

  @Mock private ApiGenerateGatchaClient generateGatchaClient;

  private final ImportProperties importProperties = new ImportProperties();
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private MonsterImportExportService newService() {
    MonsterImportExportService service =
        new MonsterImportExportService(
            monsterService, dtoMapper, generateGatchaClient, importProperties, validator);
    // Points the image fallback at a closed local port: fast, deterministic
    // "connection refused" (RestClientException) instead of a real network call.
    setAssetsHost(service, "http://127.0.0.1:1");
    return service;
  }

  private void setAssetsHost(MonsterImportExportService service, String host) {
    try {
      var field = MonsterImportExportService.class.getDeclaredField("assetsHost");
      field.setAccessible(true);
      field.set(service, host);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private MonsterEntity sampleEntity(String id, String imageUrl) {
    return MonsterEntity.builder().id(id).name("Pyrolosse-" + id).imageUrl(imageUrl).build();
  }

  private Map<String, byte[]> unzip(byte[] zipBytes) throws Exception {
    Map<String, byte[]> entries = new HashMap<>();
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        entries.put(entry.getName(), zis.readAllBytes());
        zis.closeEntry();
      }
    }
    return entries;
  }

  @Test
  @DisplayName("writeMonstersExport écrit un monster.json par monstre sans image")
  void should_WriteMonsterJsonOnly_When_MonstersHaveNoImage() throws Exception {
    MonsterImportExportService service = newService();
    MonsterEntity monster = sampleEntity("m-1", null);
    when(monsterService.getAllMonsters(true)).thenReturn(List.of(monster));
    when(dtoMapper.toGlobalMonsterWithIdDto(monster, true))
        .thenReturn(
            GlobalMonsterWithIdDto.builder()
                .id("m-1")
                .name("Pyrolosse-m-1")
                .skills(List.of())
                .build());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    service.writeMonstersExport(out, null);

    Map<String, byte[]> entries = unzip(out.toByteArray());
    assertThat(entries).containsKey("Pyrolosse-m-1/monster.json");
    assertThat(entries).hasSize(1);
  }

  @Test
  @DisplayName("writeMonstersExport n'inclut pas d'image si aucune signed URL n'est disponible")
  void should_OmitImage_When_NoSignedUrlAvailable() throws Exception {
    MonsterImportExportService service = newService();
    MonsterEntity monster = sampleEntity("m-2", "monsters/m-2.png");
    when(monsterService.getAllMonsters(true)).thenReturn(List.of(monster));
    when(dtoMapper.toGlobalMonsterWithIdDto(monster, true))
        .thenReturn(
            GlobalMonsterWithIdDto.builder()
                .id("m-2")
                .name("Pyrolosse-m-2")
                .skills(List.of())
                .build());

    SignedUrlResponse noSignedUrl = new SignedUrlResponse();
    noSignedUrl.setId("m-2");
    noSignedUrl.setSignedUrl(null);
    when(generateGatchaClient.getSignedUrls(anyList())).thenReturn(List.of(noSignedUrl));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    service.writeMonstersExport(out, null);

    Map<String, byte[]> entries = unzip(out.toByteArray());
    assertThat(entries).containsKey("Pyrolosse-m-2/monster.json");
    assertThat(entries).hasSize(1);
  }

  @Test
  @DisplayName("writeMonstersExport filtre par IDs quand une liste est fournie")
  void should_FilterByIds_When_IdsProvided() throws Exception {
    MonsterImportExportService service = newService();
    MonsterEntity monster = sampleEntity("m-3", null);
    when(monsterService.getMonsterById("m-3", true)).thenReturn(monster);
    when(dtoMapper.toGlobalMonsterWithIdDto(monster, true))
        .thenReturn(
            GlobalMonsterWithIdDto.builder()
                .id("m-3")
                .name("Pyrolosse-m-3")
                .skills(List.of())
                .build());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    service.writeMonstersExport(out, List.of("m-3"));

    Map<String, byte[]> entries = unzip(out.toByteArray());
    assertThat(entries).containsKey("Pyrolosse-m-3/monster.json");
  }

  private byte[] zipOf(Map<String, String> jsonEntriesByFolder) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      for (var e : jsonEntriesByFolder.entrySet()) {
        zos.putNextEntry(new ZipEntry(e.getKey() + "/monster.json"));
        zos.write(e.getValue().getBytes());
        zos.closeEntry();
      }
    }
    return baos.toByteArray();
  }

  private String validMonsterJson(String name) {
    return "{"
        + "\"name\":\""
        + name
        + "\","
        + "\"element\":\"FIRE\","
        + "\"stats\":{\"hp\":100,\"atk\":50,\"def\":30,\"vit\":40},"
        + "\"rank\":\"COMMON\","
        + "\"visualDescription\":\"desc\","
        + "\"cardDescription\":\"card\","
        + "\"imageUrl\":\"url\","
        + "\"skills\":[]"
        + "}";
  }

  @Test
  @DisplayName("importMonstersFromStream importe un monstre valide")
  void should_ImportValidMonster_When_ZipContainsValidJson() throws Exception {
    MonsterImportExportService service = newService();
    when(dtoMapper.toMonsterEntity(any())).thenReturn(MonsterEntity.builder().build());
    when(monsterService.createMonster(any())).thenReturn("m-new");

    byte[] zip = zipOf(Map.of("folder1", validMonsterJson("Pyrolosse")));

    int created = service.importMonstersFromStream(new ByteArrayInputStream(zip));

    assertThat(created).isEqualTo(1);
  }

  @Test
  @DisplayName("importMonstersFromStream ignore un monstre.json malformé")
  void should_SkipMalformedJson_When_ImportingArchive() throws Exception {
    MonsterImportExportService service = newService();

    byte[] zip = zipOf(Map.of("folder1", "{not valid json"));

    int created = service.importMonstersFromStream(new ByteArrayInputStream(zip));

    assertThat(created).isEqualTo(0);
  }

  @Test
  @DisplayName("importMonstersFromStream ignore un monstre avec un champ requis manquant")
  void should_SkipInvalidMonster_When_RequiredFieldMissing() throws Exception {
    MonsterImportExportService service = newService();

    String invalidJson =
        "{\"name\":\"Pyrolosse\",\"stats\":{\"hp\":100,\"atk\":50,\"def\":30,\"vit\":40}}";
    byte[] zip = zipOf(Map.of("folder1", invalidJson));

    int created = service.importMonstersFromStream(new ByteArrayInputStream(zip));

    assertThat(created).isEqualTo(0);
  }

  @Test
  @DisplayName("importMonstersFromStream rejette une archive avec trop d'entrées")
  void should_RejectArchive_When_TooManyEntries() throws Exception {
    ImportProperties limited = new ImportProperties();
    limited.setMaxEntries(1);
    MonsterImportExportService service =
        new MonsterImportExportService(
            monsterService, dtoMapper, generateGatchaClient, limited, validator);

    Map<String, String> twoEntries = new java.util.LinkedHashMap<>();
    twoEntries.put("folder1", validMonsterJson("Pyrolosse"));
    twoEntries.put("folder2", validMonsterJson("Terrastone"));
    byte[] zip = zipOf(twoEntries);

    assertThatThrownBy(() -> service.importMonstersFromStream(new ByteArrayInputStream(zip)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nombre d'entrées");
  }

  @Test
  @DisplayName("importMonstersFromStream rejette une entrée dépassant la taille maximale autorisée")
  void should_RejectArchive_When_EntryExceedsMaxSize() throws Exception {
    ImportProperties limited = new ImportProperties();
    limited.setMaxEntryUncompressedBytes(10);
    MonsterImportExportService service =
        new MonsterImportExportService(
            monsterService, dtoMapper, generateGatchaClient, limited, validator);

    byte[] zip = zipOf(Map.of("folder1", validMonsterJson("Pyrolosse-avec-un-nom-tres-long")));

    assertThatThrownBy(() -> service.importMonstersFromStream(new ByteArrayInputStream(zip)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dépasse la taille décompressée maximale");
  }

  @Test
  @DisplayName("importMonstersFromStream rejette une archive dépassant la taille cumulée autorisée")
  void should_RejectArchive_When_TotalUncompressedSizeExceedsLimit() throws Exception {
    ImportProperties limited = new ImportProperties();
    limited.setMaxTotalUncompressedBytes(10);
    MonsterImportExportService service =
        new MonsterImportExportService(
            monsterService, dtoMapper, generateGatchaClient, limited, validator);

    byte[] zip = zipOf(Map.of("folder1", validMonsterJson("Pyrolosse")));

    assertThatThrownBy(() -> service.importMonstersFromStream(new ByteArrayInputStream(zip)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("taille décompressée cumulée");
  }
}
