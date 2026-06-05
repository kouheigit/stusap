package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.PassageSet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/**
 * [PassageSetSource] が返す JSON を解析・検証して [PassageSet] を提供する Repository。
 *
 * 供給元を [PassageSetSource] に委譲しているため、fixture でも取り込みデータでも
 * 同じ解析・検証経路を再利用できる。
 */
@Singleton
class JsonPassageSetRepository @Inject constructor(
    private val source: PassageSetSource,
    private val json: Json = DEFAULT_JSON
) : PassageSetRepository {

    override suspend fun loadSets(): List<PassageSet> =
        json.decodeFromString<List<PassageSetDto>>(source.readRawJson())
            .map { it.toDomain() }

    override suspend fun getSet(id: String): PassageSet? =
        loadSets().firstOrNull { it.id == id }

    private companion object {
        // 取り込みデータには出題側が未知のフィールドが含まれうるため寛容に解析する
        private val DEFAULT_JSON = Json { ignoreUnknownKeys = true }
    }
}
