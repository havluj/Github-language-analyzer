package com.havluj.github.languageanalyzer.dao;

import com.havluj.github.languageanalyzer.model.LanguageStats;
import lombok.NonNull;
import lombok.val;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StorageDao {

    private static final String DB_HTREEMAP_NAME = "stats";

    private final DB inMemDb;
    private final DB fileDb;

    public StorageDao(@Value("${db.location}") final String dbLocation) {
        // HTreeMaps, which we use to store our data, provide HashMap and HashSet collections for MapDB. It is
        // thread-safe (it employs read-write locks) and scales under parallel updates. That's why there is no need to
        // synchronize access.

        inMemDb = DBMaker
                .memoryDB()
                .make();
        fileDb = DBMaker
                .fileDB(dbLocation)
                // To protect file from corruption, MapDB offers Write Ahead Log (WAL). It is reliable and simple way
                // to make file changes atomic and durable. However, WAL is slower, as data has to be copied and synced
                // multiple times between files. That is a tradeoff worth making, since we are not storing a lot of data
                // anyway.
                .transactionEnable()
                // This creates a shutdown hook to close the database automatically before JVM exits. This does not
                // protect us from JVM crashes or when it's killed.
                .closeOnJvmShutdown()
                // This can get us a 10% to 300% improvement in performance if supported. The trade-off is a 4GB size
                // limit, which we are easily going to fit into. Read more here: https://mapdb.org/book/performance/
                .fileMmapEnableIfSupported()
                .make();
    }

    /**
     * Save new language stats for a given org into persistent db and into memory.
     */
    public void updateLanguageStats(@NonNull final String orgName, @NonNull final LanguageStats stats) {
        // Write to disk first, as we are not blocking clients from reading (now stale) data from memory.
        val fileMap = getFileMap();
        fileMap.put(orgName, stats.getLanguageMap());
        fileDb.commit();

        // Update value in-memory.
        val inMemMap = getInMemMap();
        inMemMap.put(orgName, stats.getLanguageMap());
    }

    /**
     * Read language stats for a given org from memory. If data is not available in memory, it will be loaded from
     * disk.
     *
     * @return Languages stats if they exist. Null if they don't.
     */
    public Map<String, String> getLanguageStats(@NonNull final String orgName) {
        val inMemMap = getInMemMap();
        Map<String, String> stats = inMemMap.get(orgName);

        // reload from disk if not found
        if (stats == null) {
            stats = getFileMap().get(orgName);
            if (stats != null) {
                inMemMap.put(orgName, stats);
            }
        }

        return stats;
    }

    @SuppressWarnings("unchecked")
    private HTreeMap<String, Map<String, String>> getInMemMap() {
        return inMemDb.hashMap(DB_HTREEMAP_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    @SuppressWarnings("unchecked")
    private HTreeMap<String, Map<String, String>> getFileMap() {
        return fileDb.hashMap(DB_HTREEMAP_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }
}
