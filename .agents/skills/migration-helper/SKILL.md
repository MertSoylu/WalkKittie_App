---
name: migration-helper
description: Room migration olusturma sablonu ve versiyon guncelleme adimlari. PatiCat DB schema degisikliklerinde kullan.
---

# Migration Helper — PatiCat

Mevcut schema versiyonu: **11**
Database dosyasi: `app/src/main/java/com/mert/paticat/data/local/PatiCatDatabase.kt`

## Adimlar

### 1. Migration objesi olustur

`PatiCatDatabase.kt` companion object icine ekle:

```kotlin
val MIGRATION_11_12 = Migration(11, 12) { db ->
    // Ornek: db.execSQL("ALTER TABLE cat_table ADD COLUMN newField INTEGER NOT NULL DEFAULT 0")
}
```

### 2. Versiyon numarasini artir

```kotlin
@Database(
    entities = [/* ... */],
    version = 12,  // 11'den 12'ye
    exportSchema = false
)
```

### 3. Migration'i Room'a ekle

`DatabaseModule.kt` icinde `.addMigrations(MIGRATION_11_12)` ekle:

```kotlin
Room.databaseBuilder(context, PatiCatDatabase::class.java, "paticat_database")
    .addMigrations(PatiCatDatabase.MIGRATION_11_12)
    .build()
```

### 4. Entity'yi guncelle

Ilgili `*Entity.kt` dosyasina yeni field'i ekle.

### 5. Mapper'i guncelle

`data/local/Mappers.kt` icinde entity <-> domain donusumunu guncelle.

## Sik Yapilan Hatalar

- Migration eklemeden versiyon artirmak → uygulama crash (IllegalStateException)
- Entity guncelleme ama mapper guncellememek → derleme hatasi
- NOT NULL column icin DEFAULT deger belirtmemek → migration crash
