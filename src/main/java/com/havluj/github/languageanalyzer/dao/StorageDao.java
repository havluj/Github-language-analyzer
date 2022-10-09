package com.havluj.github.languageanalyzer.dao;

import com.havluj.github.languageanalyzer.model.LanguageStats;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DAO to store language stats in memory and on disk. The DAO will always try to return data from memory when it's
 * there, otherwise data will be reloaded form disk.
 *
 * Implementation wise, we use MapDB's HTreeMaps to store our data. They are thread-safe (it employs read-write locks)
 * and scale under parallel updates. Because of that, we don't need to synchronize access in this DAO.
 *
 * We also employ MapDB's eviction strategies. All data will be evicted after 25 hrs (which should never happen when
 * the server is up and running), but it's useful upon startup.
 */
@Service
@Slf4j
public class StorageDao {

    private static final String DB_HTREEMAP_NAME = "stats";

    private final DB inMemDb;
    private final DB onDiskDb;

    public StorageDao(@Value("${db.location}") final String dbLocation,
                      @Autowired Environment env) {
        inMemDb = DBMaker
                .memoryDB()
                .make();

        DBMaker.Maker onDiskDbBuilder;
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            onDiskDbBuilder = DBMaker.tempFileDB();
        } else {
            onDiskDbBuilder = DBMaker.fileDB(dbLocation);
        }
        onDiskDb = onDiskDbBuilder
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

    @PreDestroy
    public void destroy() {
        log.info("Closing the on disk DB");
        onDiskDb.close();
    }

    /**
     * Save new language stats for a given org into persistent db and into memory.
     */
    public void updateLanguageStats(@NonNull final String orgName, @NonNull final LanguageStats stats) {
        // Write to disk first, as we are not blocking clients from reading (now stale) data from memory.
        val onDiskMap = getOnDiskMap();
        onDiskMap.put(orgName, stats.getLanguageMap());
        onDiskDb.commit();

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
            stats = getOnDiskMap().get(orgName);
            if (stats != null) {
                inMemMap.put(orgName, stats);
            }
        }

        return stats;
    }

    @SuppressWarnings("unchecked")
    HTreeMap<String, Map<String, String>> getInMemMap() {
        return inMemDb.hashMap(DB_HTREEMAP_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .expireAfterCreate(25, TimeUnit.HOURS)
                .expireAfterUpdate(25, TimeUnit.HOURS)
                .createOrOpen();
    }

    @SuppressWarnings("unchecked")
    HTreeMap<String, Map<String, String>> getOnDiskMap() {
        return onDiskDb.hashMap(DB_HTREEMAP_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .expireAfterCreate(25, TimeUnit.HOURS)
                .expireAfterUpdate(25, TimeUnit.HOURS)
                .createOrOpen();
    }
}
