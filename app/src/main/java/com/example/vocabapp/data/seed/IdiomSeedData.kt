package com.example.vocabapp.data.seed

import com.example.vocabapp.data.local.entity.LessonEntity
import com.example.vocabapp.data.local.entity.TrainingEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.data.local.entity.WordRelationEntity

object IdiomSeedData {
    // ID ranges: lesson=100, trainings=100-102, words=10001-10030, choices=40001-40120, relations=10001-10030

    val lessons = listOf(
        LessonEntity(
            id = 100,
            scoreTarget = 0,
            title = "英熟語 Lesson 1",
            wordStartNumber = 1,
            wordEndNumber = 30,
            displayOrder = 1
        )
    )

    val trainings = listOf(
        TrainingEntity(id = 100, lessonId = 100, title = "英熟語", wordStartNumber = 1, wordEndNumber = 10, displayOrder = 1),
        TrainingEntity(id = 101, lessonId = 100, title = "英熟語", wordStartNumber = 11, wordEndNumber = 20, displayOrder = 2),
        TrainingEntity(id = 102, lessonId = 100, title = "英熟語", wordStartNumber = 21, wordEndNumber = 30, displayOrder = 3)
    )

    private data class IdiomEntry(
        val english: String,
        val meaning: String,
        val exampleSentence: String,
        val exampleTranslation: String,
        val relatedWord: String,
        val relatedMeaning: String
    )

    private val idiomEntries = listOf(
        // Training 1: 1-10語
        IdiomEntry("as soon as possible", "できるだけ早く", "Please reply as soon as possible.", "できるだけ早く返信してください。", "ASAP", "できる限り早く"),
        IdiomEntry("in advance", "前もって", "Please register in advance.", "前もって登録してください。", "beforehand", "事前に"),
        IdiomEntry("at least", "少なくとも", "You need at least five years of experience.", "少なくとも5年の経験が必要です。", "at minimum", "最低限"),
        IdiomEntry("keep in mind", "心に留めておく", "Please keep in mind that the deadline is Friday.", "締切が金曜日であることを心に留めておいてください。", "bear in mind", "覚えておく"),
        IdiomEntry("take part in", "〜に参加する", "All staff should take part in the training.", "全スタッフが研修に参加すべきです。", "participate in", "参加する"),
        IdiomEntry("on behalf of", "〜を代表して", "I am writing on behalf of the manager.", "部長を代表してご連絡しています。", "in place of", "代わりに"),
        IdiomEntry("in charge of", "〜の担当で", "She is in charge of customer service.", "彼女は顧客サービスの担当です。", "responsible for", "担当している"),
        IdiomEntry("make sure", "確認する", "Make sure all doors are locked before leaving.", "退出前に全てのドアが施錠されているか確認してください。", "ensure", "確実にする"),
        IdiomEntry("look forward to", "〜を楽しみにする", "We look forward to hearing from you.", "ご連絡をお待ちしております。", "anticipate", "期待する"),
        IdiomEntry("in terms of", "〜の観点から", "In terms of cost, this option is better.", "コストの観点から、このオプションの方が優れています。", "regarding", "〜に関して"),

        // Training 2: 11-20語
        IdiomEntry("result in", "〜という結果になる", "The merger resulted in higher profits.", "合併により利益が増加した。", "lead to", "〜につながる"),
        IdiomEntry("make use of", "〜を活用する", "We should make use of available resources.", "利用可能なリソースを活用すべきです。", "utilize", "活用する"),
        IdiomEntry("due to", "〜のために", "The event was canceled due to bad weather.", "悪天候のためイベントが中止されました。", "because of", "のために"),
        IdiomEntry("in the meantime", "その間に", "The report is being prepared. In the meantime, please review the summary.", "報告書を作成中です。その間に、要約を確認してください。", "meanwhile", "一方で"),
        IdiomEntry("on a regular basis", "定期的に", "Please submit reports on a regular basis.", "定期的に報告書を提出してください。", "regularly", "定期的に"),
        IdiomEntry("regardless of", "〜にかかわらず", "All employees are eligible regardless of their position.", "全従業員はポジションにかかわらず対象となります。", "despite", "〜にもかかわらず"),
        IdiomEntry("at the same time", "同時に", "We can start both projects at the same time.", "両プロジェクトを同時に開始できます。", "simultaneously", "同時に"),
        IdiomEntry("be responsible for", "〜の責任がある", "Each team leader is responsible for daily reports.", "各チームリーダーは日報の責任があります。", "be in charge of", "担当している"),
        IdiomEntry("carry out", "実行する", "The team will carry out the plan next week.", "チームは来週その計画を実行します。", "execute", "遂行する"),
        IdiomEntry("in accordance with", "〜に従って", "Please act in accordance with company policy.", "会社の方針に従って行動してください。", "according to", "〜によれば"),

        // Training 3: 21-30語
        IdiomEntry("speaking of ~", "〜と言えば", "Speaking of the project, have you finished the report?", "プロジェクトと言えば、報告書は完成しましたか？", "talking of", "〜と言えば"),
        IdiomEntry("go bankrupt", "破産する", "The company went bankrupt last year.", "その会社は昨年破産しました。", "go broke", "一文無しになる"),
        IdiomEntry("by any chance", "ひょっとして", "Do you, by any chance, know Mr. Tanaka?", "ひょっとして、田中さんをご存じですか？", "perhaps", "もしかして"),
        IdiomEntry("convert A into B", "AをBに変換する", "We can convert the file into PDF format.", "ファイルをPDF形式に変換できます。", "change A to B", "AをBに変える"),
        IdiomEntry("interest rates", "金利", "The bank raised interest rates last month.", "銀行は先月金利を引き上げました。", "rates of interest", "金利"),
        IdiomEntry("human resources", "人材、人事部", "Contact the human resources department for details.", "詳細は人事部にお問い合わせください。", "HR department", "人事部門"),
        IdiomEntry("succeed to ~", "〜を引き継ぐ、〜を継承する", "She will succeed to the presidency next year.", "彼女は来年社長職を引き継ぎます。", "inherit", "〜を引き継ぐ"),
        IdiomEntry("business district", "ビジネス街", "Our office is located in the business district.", "当社のオフィスはビジネス街に位置しています。", "commercial area", "商業地区"),
        IdiomEntry("settle in", "落ち着く", "It took him a month to settle in at the new company.", "新しい会社に落ち着くのに1ヶ月かかった。", "get used to", "慣れる"),
        IdiomEntry("side by side", "並んで", "The two companies worked side by side on the project.", "2つの会社はそのプロジェクトで並んで取り組みました。", "together", "一緒に")
    )

    val words: List<WordEntity> = idiomEntries.mapIndexed { index, entry ->
        val wordId = 10001 + index
        val trainingId = when (index) {
            in 0..9 -> 100
            in 10..19 -> 101
            else -> 102
        }
        WordEntity(
            id = wordId,
            trainingId = trainingId,
            english = entry.english,
            meaning = entry.meaning,
            phonetic = "",
            partOfSpeech = "熟語",
            exampleSentence = entry.exampleSentence,
            exampleTranslation = entry.exampleTranslation,
            audioUrl = null,
            exampleAudioUrl = null,
            displayOrder = (index % 10) + 1
        )
    }

    val choices: List<WordChoiceEntity> = idiomEntries.mapIndexed { index, entry ->
        val wordId = 10001 + index
        val correct = entry.meaning
        val distractors = idiomEntries
            .filterIndexed { i, _ -> i != index }
            .map { it.meaning }
            .drop((index * 3) % 27)
            .take(3)
        (listOf(correct) + distractors).mapIndexed { choiceIndex, choice ->
            WordChoiceEntity(
                id = 40001 + index * 4 + choiceIndex,
                wordId = wordId,
                choiceText = choice,
                isCorrect = choiceIndex == 0,
                displayOrder = choiceIndex + 1
            )
        }
    }.flatten()

    val relations: List<WordRelationEntity> = idiomEntries.mapIndexed { index, entry ->
        val wordId = 10001 + index
        WordRelationEntity(
            id = wordId,
            wordId = wordId,
            relatedWord = entry.relatedWord,
            relatedMeaning = entry.relatedMeaning
        )
    }
}
