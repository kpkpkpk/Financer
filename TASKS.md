# Finance Tracker — Декомпозиция задач

Формат: `[ ]` — не начато, `[~]` — в работе, `[x]` — готово

---

## Фаза 1 — Фундамент

### Epic 1: Локальная база данных

- [ ] 1.1. Подключить SQLDelight (или Room KMP) — зависимость в `libs.versions.toml`, плагин в `build.gradle.kts`
- [ ] 1.2. Описать SQL-схему таблицы `categories` (id, name, emoji, type, is_default)
- [ ] 1.3. Описать SQL-схему таблицы `transactions` (id, type, amount, category_id, date, note)
- [ ] 1.4. Сгенерировать DAO/queries для `categories` (getAll, getByType, getById, insert, delete)
- [ ] 1.5. Сгенерировать DAO/queries для `transactions` (getAll, getByPeriod, getByCategory, insert, update, delete)
- [ ] 1.6. Создать `TransactionRepository` (интерфейс + локальная реализация) в `core/data`
- [ ] 1.7. Создать `CategoryRepository` (интерфейс + локальная реализация) в `core/data`
- [ ] 1.8. Заполнить предустановленные категории при первом запуске (prepopulate)
- [ ] 1.9. Зарегистрировать репозитории в `CoreDataModule` (Koin)

### Epic 2: Модуль core/common

- [ ] 2.1. Создать gradle-модуль `core/common`, подключить в `settings.gradle.kts`
- [ ] 2.2. Утилиты форматирования: сумма в строку (`12 344,50 ₽`), дата в строку
- [ ] 2.3. Реализовать алгоритм скоринга категорий (Top-5) — функция `CategoryScorer.score(amount, transactions) → List<Category>`

---

## Фаза 2 — Основные экраны

### Epic 3: Bottom Navigation + MainComponent

- [ ] 3.1. Создать `MainComponent` с Decompose (Pages/Stack для вкладок + слот для модальных экранов)
- [ ] 3.2. Описать `Config` для вкладок: Home, Insights, Settings
- [ ] 3.3. Описать `Config` для модальных экранов: Transaction (create/edit), Filter
- [ ] 3.4. Сверстать `MainScreen` с BottomNavigationBar (4 вкладки)
- [ ] 3.5. Реализовать FAB-кнопку «+» по центру навбара (приподнятая)
- [ ] 3.6. Платформенная адаптация навбара (Material 3 для Android, плоский стиль для iOS)
- [ ] 3.7. Обновить `RootComponent` — после онбординга переходить на `MainComponent`
- [ ] 3.8. Зарегистрировать `MainModule` в Koin

### Epic 4: Главный экран (Home)

**Domain:**
- [ ] 4.1. `GetBalanceUseCase` — подсчёт общего баланса (сумма доходов − сумма расходов)
- [ ] 4.2. `GetTransactionsUseCase` — получение транзакций за период с группировкой по дате
- [ ] 4.3. `GetPeriodSummaryUseCase` — доход и расход за выбранный период

**Presentation:**
- [ ] 4.4. `HomeStore` — Intent (LoadData, PeriodChanged, TransactionClicked, DeleteRequested, DeleteConfirmed), State (balance, income, expense, transactions, period, isEmpty), Label (OpenTransaction, OpenFilter)
- [ ] 4.5. `HomeStoreFactory` — Executor (загрузка данных из репозитория, удаление) + Reducer
- [ ] 4.6. `HomeComponent` — создание Store, обработка Label, навигация

**UI:**
- [ ] 4.7. Верстка: блок баланса (сумма крупным шрифтом)
- [ ] 4.8. Верстка: карточка-сводка (доход / расход за период)
- [ ] 4.9. Верстка: иконка фильтра + текстовая метка текущего периода
- [ ] 4.10. Верстка: список транзакций с группировкой по дате (LazyColumn, sticky headers)
- [ ] 4.11. Верстка: элемент транзакции (эмодзи, название, время, сумма с цветом)
- [ ] 4.12. Верстка: empty state (иллюстрация + текст + кнопка)
- [ ] 4.13. Свайп для удаления (SwipeToDismiss) + диалог подтверждения
- [ ] 4.14. Зарегистрировать `HomeModule` в Koin

### Epic 5: Добавление транзакции

**Domain:**
- [ ] 5.1. `CreateTransactionUseCase` — валидация + сохранение в БД
- [ ] 5.2. `GetTopCategoriesUseCase` — вызов CategoryScorer, возврат top-5 по сумме и типу
- [ ] 5.3. `GetAllCategoriesUseCase` — все категории по типу (расход/доход)

**Presentation:**
- [ ] 5.4. `TransactionStore` — Intent (AmountChanged, TypeToggled, CategorySelected, DateChanged, NoteChanged, Confirm, Close), State (amount, type, selectedCategory, date, note, topCategories, allCategories, showAllCategories), Label (TransactionSaved, Close)
- [ ] 5.5. `TransactionStoreFactory` — Executor (загрузка категорий, пересчёт top-5 при изменении суммы, сохранение) + Reducer
- [ ] 5.6. `TransactionComponent` — создание Store, обработка Label, параметр mode (create/edit)

**UI:**
- [ ] 5.7. Верстка: переключатель Расход / Доход (сегментированная кнопка)
- [ ] 5.8. Верстка: поле суммы (крупный текст `0,00 ₽`)
- [ ] 5.9. Верстка: кастомный numpad (1-9, запятая, 0, ✓) — обработка ввода с копейками
- [ ] 5.10. Верстка: top-5 категорий (горизонтальный скролл чипов: эмодзи + название)
- [ ] 5.11. Верстка: кнопка «Все категории» → грид категорий (3-4 колонки)
- [ ] 5.12. Верстка: чип даты («Сегодня, 3 июня») + date picker при тапе
- [ ] 5.13. Верстка: поле комментария (TextField, опциональное)
- [ ] 5.14. Верстка: кнопка закрытия (X) + логика закрытия модального экрана
- [ ] 5.15. Зарегистрировать `TransactionModule` в Koin

---

## Фаза 3 — Аналитика и фильтрация

### Epic 6: Экран Insights (Статистика + Аналитика)

**Domain:**
- [ ] 6.1. `GetStatisticsUseCase` — агрегация сумм по дням/месяцам для bar chart
- [ ] 6.2. `GetCategoryDistributionUseCase` — распределение расходов/доходов по категориям (сумма, доля)
- [ ] 6.3. `GetCategoryTrendUseCase` — сравнение текущего и предыдущего периодов по категориям (рост/снижение)
- [ ] 6.4. `GetAverageSummaryUseCase` — расчёт «больше/меньше среднего на X ₽»

**Presentation:**
- [ ] 6.5. `InsightsStore` — Intent (TabChanged, PeriodChanged, TypeToggled, BarTapped), State (activeTab, period, statisticsData, analyticsData, selectedBar), Label (NavigateToTransaction)
- [ ] 6.6. `InsightsStoreFactory` — Executor + Reducer
- [ ] 6.7. `InsightsComponent` — создание Store, обработка Label

**UI — общее:**
- [ ] 6.8. Верстка: переключатель Статистика / Аналитика (сегментированная кнопка вверху)

**UI — вкладка Статистика:**
- [ ] 6.9. Верстка: чипы «Общий расход» (розовый) и «Общий доход» (зелёный)
- [ ] 6.10. Верстка: переключатель периода (Неделя / Месяц / Год)
- [ ] 6.11. Верстка: метка диапазона дат + итоговая сумма
- [ ] 6.12. Компонент Bar Chart (Canvas/DrawScope) — столбцы, подписи осей
- [ ] 6.13. Tooltip при тапе на столбец (всплывающая подсказка с суммой)
- [ ] 6.14. Список транзакций под графиком (переиспользовать из Home)

**UI — вкладка Аналитика:**
- [ ] 6.15. Верстка: переключатель Расход / Доход
- [ ] 6.16. Верстка: общая сумма + сравнительный текст («Больше среднего на X ₽»)
- [ ] 6.17. Верстка: bar chart тренда
- [ ] 6.18. Верстка: список категорий (эмодзи, название, сумма, индикатор тренда ↑↓)
- [ ] 6.19. Зарегистрировать `InsightsModule` в Koin

### Epic 7: Экран выбора периода (Filter)

**Presentation:**
- [ ] 7.1. `FilterStore` — Intent (ChipSelected, DateFromTapped, DateToTapped, TodayClicked, DoneClicked), State (selectedChip, dateFrom, dateTo), Label (FilterApplied, Close)
- [ ] 7.2. `FilterStoreFactory` — Executor (вычисление дат для чипов) + Reducer
- [ ] 7.3. `FilterComponent` — создание Store, обработка Label, передача результата обратно

**UI:**
- [ ] 7.4. Верстка: быстрые чипы (Эта неделя / Этот месяц / Этот квартал / С начала года)
- [ ] 7.5. Верстка: поля «с [дата]» — «по [дата]»
- [ ] 7.6. Верстка: скроллируемый календарь (LazyColumn с месяцами, грид дней)
- [ ] 7.7. Верстка: выделение диапазона на календаре (selected range)
- [ ] 7.8. Верстка: кнопки «Сегодня» и «Готово»
- [ ] 7.9. Зарегистрировать `FilterModule` в Koin

---

## Фаза 4 — Полировка

### Epic 8: Редактирование транзакции

- [ ] 8.1. Расширить `TransactionComponent` — режим edit (принимает transactionId)
- [ ] 8.2. `GetTransactionByIdUseCase` — загрузка транзакции по ID
- [ ] 8.3. `UpdateTransactionUseCase` — обновление транзакции в БД
- [ ] 8.4. Предзаполнение полей экрана данными транзакции
- [ ] 8.5. Кнопка «Удалить» внизу экрана + диалог подтверждения
- [ ] 8.6. Навигация: тап на транзакцию (Home, Insights) → открытие TransactionComponent в режиме edit

### Epic 9: Общие UI-компоненты (core/ui)

- [ ] 9.1. Компонент `TransactionItem` (переиспользуемый: эмодзи, название, время, сумма) — используется в Home, Insights
- [ ] 9.2. Компонент `TransactionList` (LazyColumn с группировкой по дате + sticky headers)
- [ ] 9.3. Компонент `PeriodSelector` (чипы Неделя/Месяц/Год) — используется в Insights
- [ ] 9.4. Компонент `ConfirmDialog` (универсальный диалог подтверждения)
- [ ] 9.5. Компонент `EmptyState` (иллюстрация + текст + кнопка)

### Epic 10: Настройки (Settings)

- [ ] 10.1. Создать gradle-модуль `feature/settings`
- [ ] 10.2. `SettingsComponent` + `SettingsScreen` — заглушка
- [ ] 10.3. Зарегистрировать `SettingsModule` в Koin
