package com.havluj.github.languageanalyzer.dao;

import com.havluj.github.languageanalyzer.model.LanguageStats;
import lombok.NonNull;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StorageDao {

    private final DB inMemDb;
    private final DB fileDb;

    public StorageDao() {
        inMemDb = DBMaker
                .memoryDB()
                .make();
        fileDb = DBMaker
                .tempFileDB()   //("....") // TODO
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

    public void updateLanguageStats(@NonNull final String orgName, @NonNull final LanguageStats stats) {
        // HTreeMap provides HashMap and HashSet collections for MapDB. It is thread-safe and scales under
        // parallel updates. No need to synchronize access.
        HTreeMap<String, Map<String, String>> map = inMemDb.hashMap("test")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

        map.put(orgName, stats.getLanguageMap());

        // todo persistence
    }

    public Map<String, String> getLanguageStats(@NonNull final String orgName) {
        HTreeMap<String, Map<String, String>> map = inMemDb.hashMap("test")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

        Map<String, String> stats = map.get(orgName);
        return stats;
    }

}
