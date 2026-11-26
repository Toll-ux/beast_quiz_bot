-- Поведение delete: указано явно для каждого FOREIGN KEY в этом файле, чтобы оно было осознанным
-- решением, а не умолчанием SQLite (NO ACTION). RESTRICT — для ссылок на справочные/исторические
-- данные (users, questions, question_answers), CASCADE — только для строк, которые принадлежат
-- исключительно своей сессии (quiz_session_questions и answer_results по цепочке от quiz_sessions:
-- удаление сессии должно убирать её собственные дочерние строки).

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    telegram_id integer UNIQUE,
    username text,
    nickname text,
    role text NOT NULL DEFAULT 'NORMAL_PLAYER',
    total_score integer NOT NULL DEFAULT 0,
    is_user_real integer NOT NULL DEFAULT 1,
    CONSTRAINT users_role_allowed CHECK (role IN ('ADMIN', 'NORMAL_PLAYER')),
    CONSTRAINT users_total_score_nonnegative CHECK (total_score >= 0),
    CONSTRAINT users_is_real_boolean CHECK (is_user_real IN (0, 1)),
    CONSTRAINT users_real_telegram_id_consistency CHECK
      (((is_user_real = 1 AND telegram_id IS NOT NULL AND telegram_id > 0) OR (is_user_real = 0 AND telegram_id IS NULL)))
);

CREATE TABLE questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    text text NOT NULL
);

-- Минимум вариантов ответа и наличие хотя бы одного правильного варианта уже проверяется в домене
-- (Question.java: answers и correctAnswerIndexes не могут быть пустыми при создании вопроса).
-- Отдельный SQL-триггер здесь сознательно не добавлен: AFTER INSERT в SQLite видит только одну
-- вставляемую строку за раз и не может надёжно проверить агрегат по всем вариантам вопроса без
-- DEFERRED - констрейнтов (которых в SQLite нет). Когда появится настоящий репозиторий (замена
-- HardcodedQuestionRepository), эквивалентную проверку нужно делать в репозитории/сервисе перед
-- записью набора вариантов, а не триггером.
CREATE TABLE question_answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id integer NOT NULL,
    position integer NOT NULL,
    text text NOT NULL,
    is_correct integer NOT NULL DEFAULT 0,
    CONSTRAINT question_answers_position_nonnegative CHECK (position >= 0),
    CONSTRAINT question_answers_is_correct_boolean CHECK (is_correct IN (0, 1)),
    UNIQUE (question_id, position),
    UNIQUE (question_id, id),
    FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE RESTRICT DEFERRABLE INITIALLY IMMEDIATE
);


CREATE TABLE quiz_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id integer NOT NULL,
    started_at text NOT NULL,
    finished_at text,
    status text NOT NULL DEFAULT 'NOT_STARTED',
    score integer NOT NULL DEFAULT 0,
    current_question_position integer NOT NULL DEFAULT 0,
    CONSTRAINT quiz_sessions_status_allowed CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'FINISHED', 'ABORTED')),
    CONSTRAINT quiz_sessions_score_nonnegative CHECK (score >= 0),
    CONSTRAINT quiz_sessions_current_question_nonnegative CHECK (current_question_position >= 0),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_sessions_user_status ON quiz_sessions (user_id, status);

CREATE UNIQUE INDEX uq_one_active_session_per_user ON quiz_sessions(user_id) WHERE status = 'IN_PROGRESS';


CREATE TRIGGER trg_sync_total_score_on_finish
    AFTER UPDATE OF status ON quiz_sessions
    WHEN NEW.status = 'FINISHED' AND OLD.status != 'FINISHED'
    BEGIN
        UPDATE users SET total_score = total_score + NEW.score WHERE id = NEW.user_id;
    END;

CREATE TABLE quiz_session_questions (
    session_id integer NOT NULL,
    question_id integer NOT NULL,
    position integer NOT NULL,
    CONSTRAINT quiz_session_questions_position_nonnegative CHECK (position >= 0),
    PRIMARY KEY (session_id, question_id),
    UNIQUE (session_id, position),
    FOREIGN KEY (session_id) REFERENCES quiz_sessions (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE,
    FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE RESTRICT DEFERRABLE INITIALLY IMMEDIATE
);

-- answer_results не хранит снимок корректности выбранного варианта на момент ответа — на самом деле, это плохо,
-- но сейчас нигде нет возможности редактировать question_answers.is_correct после создания вопроса.
-- Когда появится администрирование надо будет поправить
CREATE TABLE answer_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id integer NOT NULL,
    question_id integer NOT NULL,
    selected_option_id integer NOT NULL,
    answered_at text NOT NULL,
    UNIQUE (session_id, question_id, selected_option_id),
    FOREIGN KEY (session_id, question_id) REFERENCES quiz_session_questions (session_id, question_id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE,
    FOREIGN KEY (question_id, selected_option_id) REFERENCES question_answers (question_id, id) ON DELETE RESTRICT DEFERRABLE INITIALLY IMMEDIATE
);

