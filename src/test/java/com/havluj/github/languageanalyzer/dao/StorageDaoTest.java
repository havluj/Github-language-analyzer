package com.havluj.github.languageanalyzer.dao;

import com.havluj.github.languageanalyzer.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class StorageDaoTest extends BaseTest {

    private static final String KEY = "test";

    @Autowired
    private StorageDao storageDao;

    @BeforeEach
    void setUp() {
        storageDao.getInMemMap().clear();
        storageDao.getOnDiskMap().clear();
    }

    @Test
    void testUpdateLanguageStats() {
        storageDao.updateLanguageStats(KEY, LANGUAGE_STATS);

        assertEquals(1, storageDao.getInMemMap().size());
        assertEquals(LANGUAGE_STAT_MAP, storageDao.getInMemMap().get(KEY));

        assertEquals(1, storageDao.getOnDiskMap().size());
        assertEquals(LANGUAGE_STAT_MAP, storageDao.getOnDiskMap().get(KEY));
    }

    @Test
    void testUpdateLanguageStatsEmptyMap() {
        storageDao.updateLanguageStats(KEY, EMPTY_LANGUAGE_STATS);

        assertEquals(1, storageDao.getInMemMap().size());
        assertEquals(EMPTY_LANGUAGE_STAT_MAP, storageDao.getInMemMap().get(KEY));

        assertEquals(1, storageDao.getOnDiskMap().size());
        assertEquals(EMPTY_LANGUAGE_STAT_MAP, storageDao.getOnDiskMap().get(KEY));
    }

    @Test
    void testUpdatingExistingValue() {
        storageDao.updateLanguageStats(KEY, EMPTY_LANGUAGE_STATS);
        storageDao.updateLanguageStats(KEY, LANGUAGE_STATS);

        assertEquals(1, storageDao.getInMemMap().size());
        assertEquals(LANGUAGE_STAT_MAP, storageDao.getInMemMap().get(KEY));

        assertEquals(1, storageDao.getOnDiskMap().size());
        assertEquals(LANGUAGE_STAT_MAP, storageDao.getOnDiskMap().get(KEY));
    }

    @Test
    void testGettingLanguageStatsFromMemory() {
        storageDao.updateLanguageStats(KEY, LANGUAGE_STATS);
        assertEquals(LANGUAGE_STAT_MAP, storageDao.getLanguageStats(KEY));
    }

    @Test
    void testGettingLanguageStatsFromDisk() {
        storageDao.getOnDiskMap().put(KEY, LANGUAGE_STAT_MAP);

        assertEquals(LANGUAGE_STAT_MAP, storageDao.getLanguageStats(KEY));

        assertEquals(1, storageDao.getInMemMap().size());
        assertEquals(LANGUAGE_STAT_MAP, storageDao.getInMemMap().get(KEY));
    }

    @Test
    void testGettingLanguageStatsNotInDb() {
        assertEquals(null, storageDao.getLanguageStats("invalid"));
    }
}