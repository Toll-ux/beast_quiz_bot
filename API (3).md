

| HTTP метод | endpoint              | Что делает                        |
| ---------- | --------------------- | --------------------------------- |
| POST       | /api/start            | Начинает квиз                     |
| POST       | /api/answer           | Принимает ответ пользователя      |
| POST       | /api/finish           | Завершает квиз                    |
| POST       | /api/abort            | Прерывает квиз                    |
| GET        | /api/leaderboard      | Возвращает таблицу лидеров        |
| GET        | /api/current_question | Возвращает обзор текущего вопроса |
## Структуры JSON, который backend принимает или возвращает
в DTO определяются java классы, как эти данные будут представлены (long, ArrayList и тп)
Spring и Jackson преобразуют DTO в JSON и обратно
### Начало квиза
#### Запрос
``` json
{
	"userId": 12345
	"questionCount": 2
}
```

#### Ответ
У нас start вызывает startQuiz.execute, который возвращает, создавался ли новый квиз (и сессия).

``` json
{
	"newQuizCreated": true 
}
```

### Принятие ответа пользователя
#### Запрос

``` json
{
	"userId": 12345,
	"questionId": 2,
	"chosen": [ 1, 2, 3]
}
```

#### Ответ
Все то же, что и в AnswerQuestionResult
``` json
{
	"correct": true,  
	"correctAnswers": [1, 3, 4],
	"currentScore": 44,
	"finished": true 
}
```

### Завершение квиза
#### Запрос
``` json
{
	"userId": 12345
}
```
#### Ответ
Все то же, что в FinishQuizResult
``` json
{
	"sessionId": 3, Можно убрать?
	"quizScore":  44,  
	"userTotalScore": 34
}
```

### Прерывание квиза
#### Запрос
``` json
{
	"userId": 12345
}
```
#### Ответ
Все то же, что в FinishQuizResult
``` json
{
	"sessionId": 2, Можно убрать?  
	"quizScore": 40,  
	"userTotalScore": 50 
}
```

### Обзор текущего вопроса
#### Запрос
``` json
{
	"userId": 12345
}
```
#### Ответ
по answers: JSON массив упорядочен :)
``` json
{
	"questionId": 2,
	"text": "Какой бог был богом грома в скандинавской мифологии?",
	"answers": [
		"Один",
		"Тор",
		"Локи",
		"Фрейя"
	]
}
```
### Возврат таблицы лидеров
#### Запрос
``` json
{
	"userId": 12345
}
```
#### Ответ 
Список записей таблицы лидеров. Array of LeaderboardEntry
``` json
{
"leaderboard": [
	{
		"place": 1,
		"score": 120,
		"nickname": "Alice"
	},
	{
		"place": 2,
		"score": 110,
		"nickname": "Bob"
	},
	{
		"place": 3,
		"score": 110,
		"nickname": "Michael"
	},
	{
		"place": 12,
		"score": 100,
		"nickname": "Lol"
	},
	{
		"place": 13,
		"score": 90,
		"nickname": "Cringe"
	},
]
}
```

