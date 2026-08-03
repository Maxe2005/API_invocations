package com.imt.api_invocations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bounds applied when reading a monster ZIP archive ({@code POST
 * /api/invocation/monsters/import}), to defend against zip-bomb style archives (few compressed
 * bytes expanding to a huge amount of data) or archives with an excessive number of entries.
 */
@Component
@ConfigurationProperties(prefix = "app.import")
@Getter
@Setter
public class ImportProperties {

  /** Maximum number of entries (files/directories) accepted in the archive. */
  private int maxEntries = 500;

  /** Maximum uncompressed size accepted for a single archive entry, in bytes. */
  private long maxEntryUncompressedBytes = 50L * 1024 * 1024;

  /** Maximum cumulated uncompressed size accepted for the whole archive, in bytes. */
  private long maxTotalUncompressedBytes = 200L * 1024 * 1024;
}
