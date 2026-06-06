package com.example.vocabapp.ui.screen.passage

internal object PassagePracticeFixtures {
    val sets = listOf(
        PassageSet(
            id = "coffee-article-001",
            instruction = "Read the article and choose the best answer to each question.",
            timeLimitSec = 300,
            documents = listOf(
                PassageDocument(
                    kind = PassageDocumentKind.Article,
                    title = "A Small Coffee Shop Goes Local",
                    body = "Green Cup Coffee is a small shop near Central Station. Last month, the owner, Maya Lee, started buying coffee beans from a local roasting company instead of a large national supplier. The new beans cost a little more, but customers say the coffee tastes fresher. To keep prices almost the same, the shop reduced the number of paper cups it buys and began offering a discount to customers who bring their own cups. Maya says the change has helped the shop build a stronger relationship with people in the neighborhood."
                )
            ),
            questions = listOf(
                PassageQuestion(
                    number = "1-1",
                    stem = "What change did Green Cup Coffee make last month?",
                    options = listOf(
                        "It moved closer to Central Station.",
                        "It started buying beans from a local roaster.",
                        "It stopped selling coffee in the morning.",
                        "It hired a new national supplier."
                    ),
                    answerIndex = 1,
                    explanation = "The article says the shop started buying beans from a local roasting company."
                ),
                PassageQuestion(
                    number = "1-2",
                    stem = "Why does the shop give a discount to some customers?",
                    options = listOf(
                        "To encourage customers to bring their own cups.",
                        "To sell more paper cups.",
                        "To introduce a new station menu.",
                        "To raise the price of coffee."
                    ),
                    answerIndex = 0,
                    explanation = "The discount is offered to customers who bring their own cups."
                ),
                PassageQuestion(
                    number = "1-3",
                    stem = "What is one result of the change, according to Maya?",
                    options = listOf(
                        "The shop has closed on weekends.",
                        "The shop has built a stronger neighborhood relationship.",
                        "Customers now buy only iced coffee.",
                        "The national supplier lowered its prices."
                    ),
                    answerIndex = 1,
                    explanation = "Maya says the change helped the shop build a stronger relationship with local people."
                )
            )
        ),
        PassageSet(
            id = "pool-notice-email-001",
            instruction = "Read the notice and the email. Then choose the best answer to each question.",
            timeLimitSec = 420,
            documents = listOf(
                PassageDocument(
                    kind = PassageDocumentKind.Notice,
                    title = "City Pool Maintenance Notice",
                    body = "The City Pool will be closed for maintenance from Monday, July 8 through Wednesday, July 10. During this period, workers will repair the locker room floor and clean the main pool. Morning swim classes scheduled for these dates will be moved to the East Sports Center. Regular public swimming will restart at 10:00 a.m. on Thursday, July 11. Visitors with monthly passes may use the East Sports Center pool at no extra cost while the City Pool is closed."
                ),
                PassageDocument(
                    kind = PassageDocumentKind.Email,
                    header = PassageEmailHeader(
                        to = "City Pool Members",
                        from = "Lena Ortiz, Pool Manager",
                        date = "July 3",
                        subject = "Class location during pool closure"
                    ),
                    body = "Dear members,\n\nThank you for your patience during next week's maintenance. If you are registered for a morning swim class on July 8, 9, or 10, please go directly to the East Sports Center. Your class time will not change. Please bring your City Pool membership card so the front desk can check your registration. Afternoon private lessons are canceled and will be rescheduled after the pool reopens. If you have questions, please reply to this email by Friday.\n\nSincerely,\nLena Ortiz"
                )
            ),
            questions = listOf(
                PassageQuestion(
                    number = "2-1",
                    stem = "Why will the City Pool be closed?",
                    options = listOf(
                        "A swimming race will be held there.",
                        "The pool will be used by a school.",
                        "Maintenance work will be done.",
                        "The manager will be away."
                    ),
                    answerIndex = 2,
                    explanation = "The notice says the pool will be closed for maintenance."
                ),
                PassageQuestion(
                    number = "2-2",
                    stem = "When will regular public swimming restart at the City Pool?",
                    options = listOf(
                        "Monday, July 8",
                        "Wednesday, July 10",
                        "Thursday, July 11 at 10:00 a.m.",
                        "Friday, July 12 in the afternoon"
                    ),
                    answerIndex = 2,
                    explanation = "The notice states that regular public swimming restarts at 10:00 a.m. on Thursday, July 11."
                ),
                PassageQuestion(
                    number = "2-3",
                    stem = "Where should morning swim class students go on July 9?",
                    options = listOf(
                        "The City Pool locker room",
                        "The East Sports Center",
                        "The city office",
                        "The outdoor tennis court"
                    ),
                    answerIndex = 1,
                    explanation = "The notice and email both say morning classes on closure dates will move to the East Sports Center."
                ),
                PassageQuestion(
                    number = "2-4",
                    stem = "What should class members bring to the East Sports Center?",
                    options = listOf(
                        "A printed maintenance notice",
                        "A private lesson ticket",
                        "A City Pool membership card",
                        "A new monthly pass"
                    ),
                    answerIndex = 2,
                    explanation = "The email asks members to bring their City Pool membership card."
                ),
                PassageQuestion(
                    number = "2-5",
                    stem = "What will happen to afternoon private lessons during the closure?",
                    options = listOf(
                        "They will be canceled and rescheduled.",
                        "They will be held one hour earlier.",
                        "They will move to the main pool.",
                        "They will be free for monthly pass holders."
                    ),
                    answerIndex = 0,
                    explanation = "The email says afternoon private lessons are canceled and will be rescheduled after reopening."
                )
            )
        )
    )
}
