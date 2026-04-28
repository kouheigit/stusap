package com.example.vocabapp.data.seed

import com.example.vocabapp.data.local.entity.LessonEntity
import com.example.vocabapp.data.local.entity.TrainingEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.data.local.entity.WordRelationEntity

object SeedData {
    private val baseWords = listOf(
        SeedWord("department", "部門", "[dɪpɑːrtmənt]", "名詞", "The sales department is on the second floor.", "営業部は2階にあります。", "division", "部署"),
        SeedWord("against", "〜に対して", "[əɡénst]", "前置詞", "We are against the new policy.", "私たちはその新しい方針に反対しています。", "opposed", "反対の"),
        SeedWord("material", "材料、資料", "[mətíəriəl]", "名詞", "Please prepare the meeting materials.", "会議資料を準備してください。", "document", "資料"),
        SeedWord("annual", "年1回の", "[ǽnjuəl]", "形容詞", "The annual report will be released tomorrow.", "年次報告書は明日公開されます。", "yearly", "毎年の"),
        SeedWord("confirm", "確認する", "[kənfɜ́ːrm]", "動詞", "Please confirm your reservation by Friday.", "金曜日までに予約を確認してください。", "verify", "確かめる"),
        SeedWord("available", "利用できる", "[əvéɪləbl]", "形容詞", "Seats are available on the morning flight.", "午前便には座席があります。", "accessible", "利用可能な"),
        SeedWord("purchase", "購入する", "[pɜ́ːrtʃəs]", "動詞", "Customers can purchase tickets online.", "顧客はオンラインでチケットを購入できます。", "buy", "買う"),
        SeedWord("require", "必要とする", "[rɪkwáɪər]", "動詞", "This position requires strong communication skills.", "この職には高いコミュニケーション能力が必要です。", "need", "必要とする"),
        SeedWord("estimate", "見積もる", "[éstəmèɪt]", "動詞", "We estimate the project will take two weeks.", "そのプロジェクトには2週間かかると見積もっています。", "approximate", "概算する"),
        SeedWord("policy", "方針、規定", "[pɑ́ːləsi]", "名詞", "The company changed its travel policy.", "会社は出張規定を変更しました。", "rule", "規則"),
        SeedWord("submit", "提出する", "[səbmít]", "動詞", "Submit the application before the deadline.", "締切前に申請書を提出してください。", "file", "提出する"),
        SeedWord("facility", "施設", "[fəsíləti]", "名詞", "The new facility opened near the station.", "新しい施設が駅の近くに開業しました。", "site", "施設"),
        SeedWord("efficient", "効率的な", "[ɪfíʃənt]", "形容詞", "The new system is more efficient.", "新しいシステムはより効率的です。", "productive", "生産的な"),
        SeedWord("increase", "増加する", "[ɪnkríːs]", "動詞", "Sales increased during the holiday season.", "休日シーズンに売上が増加しました。", "rise", "上昇する"),
        SeedWord("reduce", "減らす", "[rɪdúːs]", "動詞", "We need to reduce shipping costs.", "配送費を減らす必要があります。", "decrease", "減少させる"),
        SeedWord("maintain", "維持する", "[meɪntéɪn]", "動詞", "The team maintains high service standards.", "そのチームは高いサービス基準を維持しています。", "keep", "保つ"),
        SeedWord("inspect", "検査する", "[ɪnspékt]", "動詞", "Engineers inspect the equipment every month.", "技術者は毎月設備を点検します。", "check", "確認する"),
        SeedWord("deliver", "配達する", "[dɪlívər]", "動詞", "The package will be delivered today.", "荷物は本日配達されます。", "ship", "発送する"),
        SeedWord("candidate", "候補者", "[kǽndədèɪt]", "名詞", "Three candidates were invited for interviews.", "3人の候補者が面接に招かれました。", "applicant", "応募者"),
        SeedWord("negotiate", "交渉する", "[nɪgóʊʃièɪt]", "動詞", "They negotiated a better contract.", "彼らはより良い契約を交渉しました。", "discuss", "協議する")
    )

    val lessons: List<LessonEntity> = listOf(600, 730, 860, 990).mapIndexed { index, score ->
        val start = index * 100 + 1
        LessonEntity(
            id = index + 1,
            scoreTarget = score,
            title = "目標${score}点 Lesson ${index + 1}",
            wordStartNumber = start,
            wordEndNumber = start + 99,
            displayOrder = index + 1
        )
    }

    val trainings: List<TrainingEntity> = lessons.flatMap { lesson ->
        (1..10).map { number ->
            val globalIndex = (lesson.id - 1) * 10 + number
            val start = lesson.wordStartNumber + (number - 1) * 10
            TrainingEntity(
                id = globalIndex,
                lessonId = lesson.id,
                title = "Training ${number.toString().padStart(2, '0')}",
                wordStartNumber = start,
                wordEndNumber = start + 9,
                displayOrder = number
            )
        }
    }

    val words: List<WordEntity> = trainings.flatMap { training ->
        (1..10).map { offset ->
            val id = (training.id - 1) * 10 + offset
            val seed = baseWords[(id - 1) % baseWords.size]
            val scoreSuffix = when ((training.lessonId - 1) % 4) {
                0 -> ""
                1 -> "を的確に"
                2 -> "を詳しく"
                else -> "を高度に"
            }
            WordEntity(
                id = id,
                trainingId = training.id,
                english = seed.english,
                meaning = if (scoreSuffix.isBlank()) seed.meaning else "${seed.meaning}${scoreSuffix}",
                phonetic = seed.phonetic,
                partOfSpeech = seed.partOfSpeech,
                exampleSentence = seed.exampleSentence,
                exampleTranslation = seed.exampleTranslation,
                audioUrl = null,
                exampleAudioUrl = null,
                displayOrder = offset
            )
        }
    }

    val choices: List<WordChoiceEntity> = words.flatMap { word ->
        val correct = word.meaning
        val distractors = baseWords
            .map { it.meaning }
            .filterNot { correct.startsWith(it) || it == correct }
            .drop(word.id % 8)
            .take(3)
        (listOf(correct) + distractors).mapIndexed { index, choice ->
            WordChoiceEntity(
                id = (word.id - 1) * 4 + index + 1,
                wordId = word.id,
                choiceText = choice,
                isCorrect = index == 0,
                displayOrder = index + 1
            )
        }
    }

    val relations: List<WordRelationEntity> = words.map { word ->
        val seed = baseWords[(word.id - 1) % baseWords.size]
        WordRelationEntity(
            id = word.id,
            wordId = word.id,
            relatedWord = seed.relatedWord,
            relatedMeaning = seed.relatedMeaning
        )
    }

    private data class SeedWord(
        val english: String,
        val meaning: String,
        val phonetic: String,
        val partOfSpeech: String,
        val exampleSentence: String,
        val exampleTranslation: String,
        val relatedWord: String,
        val relatedMeaning: String
    )
}
