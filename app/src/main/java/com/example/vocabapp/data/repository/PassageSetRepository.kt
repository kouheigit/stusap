package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.PassageSet

/**
 * 長文読解問題セットの取得口。
 *
 * 供給元（[PassageSetSource]）を差し替えることで fixture / 取り込みデータの双方に対応する。
 */
interface PassageSetRepository {
    /** 利用可能な全セットを提示順に返す。 */
    suspend fun loadSets(): List<PassageSet>

    /** 指定 ID のセットを返す。存在しなければ null。 */
    suspend fun getSet(id: String): PassageSet?
}
