# Passage Set Fixture Spec

The later import feature will generate this model as-is.
The question side should not need changes when this shape is imported.

```text
PassageSet { id, instruction, documents[], questions[], timeLimitSec? }
Document   { kind:"article"|"email"|"notice", header?{to,from,date,subject}, title?, body }
Question   { number:"1-1", stem, options[2..4], answerIndex:int, explanation? }
SessionState { setId, currentIndex, selections[], remainingSec, finished, score }
```

## Fixtures

`fixtures/sets.json`

- Coffee article set: `article` x 1, 3 questions
- Pool set: `notice` + `email`, 5 questions

## Notes

- `answerIndex` is zero-based.
- `options` must contain 2 to 4 choices.
- `documents` order is the presentation order for the quiz screen.
