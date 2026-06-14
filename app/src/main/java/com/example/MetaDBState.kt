package com.example

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// ==========================================
// MODULE: DATA MODELS
// ==========================================

data class MetaManifest(
    val name: String = "MetaCore Runtime",
    val version: String = "5.0.0",
    val author: String = "Andarer",
    val mode: String = "Monolithic Kotlin Android App",
    val storage: String = "Local First",
    val database: String = "MetaDB",
    val buildDate: String = "2026-06-14",
    val compatibility: List<String> = listOf("Android Jetpack Compose", "Local Filesystem", "Offline Sync")
)

data class MetaMemory(
    val id: String,
    val type: String, // "decision", "rule", "experiment"
    val title: String,
    val reason: String,
    val status: String // "active", "deprecated"
)

data class MetaJournalEntry(
    val date: String,
    val version: String,
    val author: String = "Andarer",
    val changes: List<String>,
    val notes: List<String> = emptyList(),
    val issues: List<String> = emptyList(),
    val ideas: List<String> = emptyList()
)

data class MetaProject(
    val id: String,
    val title: String,
    val description: String,
    val status: String, // "draft", "active", "completed", "archived"
    val progress: Int, // 0 - 100
    val tasks: List<MetaTask> = emptyList()
)

data class MetaTask(
    val id: String,
    val text: String,
    val isCompleted: Boolean
)

data class MetaWikiEntry(
    val id: String,
    val title: String,
    val category: String, // "Разработка", "Архитектура", "Безопасность", "Индустрия"
    val date: String,
    val excerpt: String,
    val content: String
)

data class MetaPrompt(
    val id: String,
    val category: String, // "UI/UX", "Код", "Контент", "База данных", "Безопасность"
    val title: String,
    val prompt: String
)

data class MetaChangelogEntry(
    val version: String,
    val date: String,
    val isCurrent: Boolean,
    val added: List<String> = emptyList(),
    val changed: List<String> = emptyList(),
    val fixed: List<String> = emptyList(),
    val removed: List<String> = emptyList()
)

data class MetaFeature(
    val id: String,
    val name: String,
    val version: String,
    val status: String // "active", "draft", "deprecated"
)

data class MetaDebtItem(
    val id: String,
    val priority: String, // "high", "medium", "low"
    val module: String,
    val issue: String,
    val created: String
)

// CMS BLOCK SCHEMAS
data class BuilderBlock(
    val id: String,
    val type: String, // "hero", "text", "features", "gallery", "cta", "contact", "faq", "pricing"
    val title: String,
    val content: String,
    val properties: Map<String, String> = emptyMap() // e.g. "subtitle", "buttonText", "alignment"
)

// ==========================================
// MODULE: STATE STORE & PERSISTENCE Engine
// ==========================================

class MetaDBState {
    var manifest = MetaManifest()
    var memories = mutableListOf<MetaMemory>()
    var journals = mutableListOf<MetaJournalEntry>()
    var projects = mutableListOf<MetaProject>()
    var wikiEntries = mutableListOf<MetaWikiEntry>()
    var prompts = mutableListOf<MetaPrompt>()
    var changelog = mutableListOf<MetaChangelogEntry>()
    var features = mutableListOf<MetaFeature>()
    var debtItems = mutableListOf<MetaDebtItem>()
    var builderBlocks = mutableListOf<BuilderBlock>()
    
    // User metadata
    var userName: String = "Andarer"
    var userEmail: String = "andarer86@gmail.com"
    var userRole: String = "Главный Архитектор"
    var appThemeMode: String = "dark" // "light", "dark", "auto"

    init {
        loadDefaults()
    }

    private fun loadDefaults() {
        // Core Architectural Memories
        memories.clear()
        memories.add(MetaMemory("MEM-001", "decision", "Монолитный HTML как контейнер", "Максимальная портативность, совместимость с Android WebView и Qwen-экосистемой", "active"))
        memories.add(MetaMemory("MEM-002", "rule", "Local First по умолчанию", "Гарантированная работа со знаниями без задержек и интернет-соединений", "active"))
        memories.add(MetaMemory("MEM-003", "decision", "Эвакуация в JSON", "База данных может экспортироваться в файл одной кнопкой и переноситься", "active"))
        memories.add(MetaMemory("MEM-004", "rule", "Экранирование HTML на выводе", "Защита от XSS и DOM-инъекций при работе со сторонними блоками", "active"))

        // Journal Development entries
        journals.clear()
        journals.add(MetaJournalEntry("2026-06-14", "5.0.0", "Andarer", 
            listOf("Создан первый полностью нативный клиент MetaCore Studio на Android", "Реализован движок оффлайн JSON-хранилища MetaDB", "Интегрирован симулятор Automation CI/CD аудита")
        ))
        journals.add(MetaJournalEntry("2026-06-13", "2.5.1", "Andarer", 
            listOf("Добавлены новые статьи в Wiki-репозиторий", "Исправлены уязвимости рендеринга пользовательских блоков в конструкторе")
        ))

        // Projects & Roadmap goals
        projects.clear()
        projects.add(MetaProject("PROJ-001", "Релиз MetaCore Studio Android", "Перенос веб-архитектуры конструктора сайтов на рельсы нативного Jetpack Compose приложения", "active", 85, listOf(
            MetaTask("TASK-1", "Реализовать UI-песочницу блоков конструктора (Hero, Text, CTA)", true),
            MetaTask("TASK-2", "Добавить полноценную Wiki-базу и справочник паттернов", true),
            MetaTask("TASK-3", "Внедрить симулятор отладки и аудита системы (CI/CD Pipeline)", false),
            MetaTask("TASK-4", "Реализовать экспорт сгенерированного HTML-кода в оффлайн-песочницу", false)
        )))
        projects.add(MetaProject("PROJ-002", "Интеграция с ИИ ассистентами", "Прямая выгрузка сгенерированных промптов в ИИ контексты для авто-написания модулей", "draft", 30, listOf(
            MetaTask("TASK-5", "Написать библиотеку готовых шаблонов промптов", true),
            MetaTask("TASK-6", "Разработать конструктор цепочек промптов для генерации кода", false)
        )))

        // Personal Wiki Base
        wikiEntries.clear()
        wikiEntries.add(MetaWikiEntry(
            "WIKI-001", 
            "Принципы архитектуры Local-First", 
            "Архитектура", 
            "2026-06-14",
            "Детальный анализ концепции локального владения данными и независимости ПО от серверов.", 
            "Локально-ориентированное программное обеспечение (Local-First) заново утверждает главенство пользовательских данных. Ваши файлы хранятся на вашем устройстве, а не в облаке. Облако выступает лишь как шина синхронизации и резервного копирования. Основные преимущества:\n1. Мгновенный отклик UI (производительность локальной ОЗУ).\n2. Полная автономность - работа в самолете, лесу или метро.\n3. Конфиденциальность - данные не сканируются крупными корпорациями.\n4. Долговечность - приложение продолжит работать, даже если разработчик закроет сервера."
        ))
        wikiEntries.add(MetaWikiEntry(
            "WIKI-002", 
            "Способы предотвращения XSS атак", 
            "Безопасность", 
            "2026-06-12",
            "Руководство по санитизации пользовательского ввода и защите локального DOM дерева/WebView.", 
            "При рендеринге пользовательских HTML блоков или импорте чужих конфигураций конструктора возникает риск Cross-Site Scripting (XSS). Для защиты в приложении реализованы жесткие барьеры:\n1. Обязательное кодирование (escapeHtml) всех динамических значений.\n2. Парсинг структуры только через валидные JSON схемы с белым списком разрешенных объектов.\n3. Использование библиотек вроде DOMPurify (в WebView) или безопасных Text-компонентов в Jetpack Compose, которые игнорируют теги <script> и onClick-инъекции."
        ))
        wikiEntries.add(MetaWikiEntry(
            "WIKI-003", 
            "Паттерн Модульный Монолит для Single-Page Apps", 
            "Разработка", 
            "2026-06-14",
            "Качественный подход к разделению монолитного каркаса на независимые плагины.", 
            "Модульный монолит сочетает легкую дистрибуцию одного файла с высокой логической развязкой. На практике каждый сервис (Storage, Router, Builder, Audit) объявляется в виде самодостаточного модуля и регистрируется через глобальный метод registerModule(). Это позволяет изолировать разработку модулей, тестировать их по отдельности, а при увеличении нагрузок - безболезненно выносить в микросервисы."
        ))

        // High Quality AI Prompts
        prompts.clear()
        prompts.add(MetaPrompt("PRM-001", "UI/UX", "Генератор Hero-секции", "Создай современный Hero-блок для SaaS продукта с заголовком, подзаголовком, двумя CTA-кнопками и местом для 3D-иллюстрации. Используй стеклянный морфизм и градиенты."))
        prompts.add(MetaPrompt("PRM-002", "Код", "Оптимизация CSS", "Оптимизируй этот CSS-код, удали дубликаты, добавь CSS-переменные для темы и обеспечь поддержку dark mode через media query prefers-color-scheme."))
        prompts.add(MetaPrompt("PRM-003", "Контент", "SEO-описание страницы", "Напиши SEO-оптимизированное описание для страницы 'Конструктор сайтов' длиной 150-160 символов, включающее ключевые слова: no-code, визуальный редактор, быстрый запуск."))
        prompts.add(MetaPrompt("PRM-004", "Безопасность", "Аудит кода на XSS", "Проанализируй данный JavaScript/Compose UI-код на наличие XSS-уязвимостей, небезопасного использования innerHTML и проблем с валидацией пользовательского ввода."))

        // Features registry
        features.clear()
        features.add(MetaFeature("feat_builder", "Visual Constructor", "2.1.0", "active"))
        features.add(MetaFeature("feat_wiki", "Knowledge Wiki", "1.5.0", "active"))
        features.add(MetaFeature("feat_prompts", "AI Prompts Library", "1.1.0", "active"))
        features.add(MetaFeature("feat_audit", "Automation Engine Pipeline", "2.0.0", "active"))
        features.add(MetaFeature("feat_projects", "Projects Roadmap", "1.0.0", "active"))

        // Technical Debt Tracker
        debtItems.clear()
        debtItems.add(MetaDebtItem("DEBT-001", "high", "Router Engine", "Монолитная реализация роутера в Compose", "2026-06-14"))
        debtItems.add(MetaDebtItem("DEBT-002", "medium", "Storage Engine", "Отсутствие шифрования локального JSON бекапа", "2026-06-13"))
        debtItems.add(MetaDebtItem("DEBT-003", "low", "CSS/Theming", "Дублирование стилей в некоторых сгенерированных блоках экспорта", "2026-06-14"))

        // Official App Changelog
        changelog.clear()
        changelog.add(MetaChangelogEntry("5.0.0", "2026-06-14", true,
            added = listOf("Полная миграция нативного Android приложения на Jetpack Compose", "Котлин-модель базы знаний MetaWiki", "Интегрированный редактор CMS блоков в реальном времени"),
            changed = listOf("Оптимизирован оффлайн-движок сохранения данных"),
            fixed = listOf("Исправлены утечки памяти при перерисовке сложных Canvas графов")
        ))
        changelog.add(MetaChangelogEntry("2.5.1", "2026-06-10", false,
            added = listOf("Новая санитизация HTML против XSS атак", "Дорожная карта расширения экосистемы Portal"),
            fixed = listOf("Краш в рендере раздела Компании из-за пустых рекомендаций")
        ))

        // Initial CMS Blocks inside CMS Builder
        builderBlocks.clear()
        builderBlocks.add(BuilderBlock("BLK-101", "hero", "MetaCore Studio", "Создавайте стильные адаптивные целевые страницы, накапливайте персональные базы знаний и ведите проекты прямо со смартфона без кода.", mapOf("subtitle" to "Универсальное оффлайн-приложение нового поколения", "buttonText" to "Начать Творить", "alignment" to "center")))
        builderBlocks.add(BuilderBlock("BLK-102", "text", "Архитектурная Концепция", "Этот продукт воплощает идеологию 'Локально Первого ПО' (Local-First). Все данные и логика визуального рендеринга находятся исключительно на вашем Android устройстве. Это делает его невосприимчивым к отключению сети, гарантирует защиту конфиденциальности данных и обеспечивает нулевые задержки интерфейса."))
        builderBlocks.add(BuilderBlock("BLK-103", "cta", "Готовы Создать Свой Шаблон?", "Мы подготовили экспорт качественного чистого адаптивного HTML/CSS кода одной кнопкой.", mapOf("buttonText" to "Экспортировать HTML код", "subtitle" to "Готово к загрузке на GitHub Pages или Vercel")))
    }

    // ==========================================
    // MODULE: SERIALIZATION / TO JSON
    // ==========================================
    fun toJson(): String {
        try {
            val root = JSONObject()
            
            // user profile
            val user = JSONObject()
            user.put("name", userName)
            user.put("email", userEmail)
            user.put("role", userRole)
            user.put("themeMode", appThemeMode)
            root.put("user_profile", user)

            // manifest
            val manifestObj = JSONObject()
            manifestObj.put("name", manifest.name)
            manifestObj.put("version", manifest.version)
            manifestObj.put("author", manifest.author)
            manifestObj.put("mode", manifest.mode)
            manifestObj.put("storage", manifest.storage)
            manifestObj.put("database", manifest.database)
            manifestObj.put("buildDate", manifest.buildDate)
            
            val compatArray = JSONArray()
            manifest.compatibility.forEach { compatArray.put(it) }
            manifestObj.put("compatibility", compatArray)
            root.put("manifest", manifestObj)

            // memories
            val memArr = JSONArray()
            memories.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("type", it.type)
                obj.put("title", it.title)
                obj.put("reason", it.reason)
                obj.put("status", it.status)
                memArr.put(obj)
            }
            root.put("memories", memArr)

            // journals
            val jourArr = JSONArray()
            journals.forEach {
                val obj = JSONObject()
                obj.put("date", it.date)
                obj.put("version", it.version)
                obj.put("author", it.author)
                
                val chArr = JSONArray()
                it.changes.forEach { chArr.put(it) }
                obj.put("changes", chArr)
                
                val noArr = JSONArray()
                it.notes.forEach { noArr.put(it) }
                obj.put("notes", noArr)
                
                val isArr = JSONArray()
                it.issues.forEach { isArr.put(it) }
                obj.put("issues", isArr)

                val idArr = JSONArray()
                it.ideas.forEach { idArr.put(it) }
                obj.put("ideas", idArr)

                jourArr.put(obj)
            }
            root.put("journals", jourArr)

            // projects
            val projArr = JSONArray()
            projects.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("title", it.title)
                obj.put("description", it.description)
                obj.put("status", it.status)
                obj.put("progress", it.progress)
                
                val taskArr = JSONArray()
                it.tasks.forEach { task ->
                    val tObj = JSONObject()
                    tObj.put("id", task.id)
                    tObj.put("text", task.text)
                    tObj.put("isCompleted", task.isCompleted)
                    taskArr.put(tObj)
                }
                obj.put("tasks", taskArr)
                projArr.put(obj)
            }
            root.put("projects", projArr)

            // wiki
            val wikiArr = JSONArray()
            wikiEntries.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("title", it.title)
                obj.put("category", it.category)
                obj.put("date", it.date)
                obj.put("excerpt", it.excerpt)
                obj.put("content", it.content)
                wikiArr.put(obj)
            }
            root.put("wiki", wikiArr)

            // prompts
            val prmArr = JSONArray()
            prompts.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("category", it.category)
                obj.put("title", it.title)
                obj.put("prompt", it.prompt)
                prmArr.put(obj)
            }
            root.put("prompts", prmArr)

            // changelog
            val changeArr = JSONArray()
            changelog.forEach {
                val obj = JSONObject()
                obj.put("version", it.version)
                obj.put("date", it.date)
                obj.put("isCurrent", it.isCurrent)
                
                val ad = JSONArray()
                it.added.forEach { ad.put(it) }
                obj.put("added", ad)

                val ch = JSONArray()
                it.changed.forEach { ch.put(it) }
                obj.put("changed", ch)

                val fx = JSONArray()
                it.fixed.forEach { fx.put(it) }
                obj.put("fixed", fx)

                val rm = JSONArray()
                it.removed.forEach { rm.put(it) }
                obj.put("removed", rm)

                changeArr.put(obj)
            }
            root.put("changelog", changeArr)

            // features
            val featArr = JSONArray()
            features.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("version", it.version)
                obj.put("status", it.status)
                featArr.put(obj)
            }
            root.put("features", featArr)

            // debt
            val debtArr = JSONArray()
            debtItems.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("priority", it.priority)
                obj.put("module", it.module)
                obj.put("issue", it.issue)
                obj.put("created", it.created)
                debtArr.put(obj)
            }
            root.put("debt", debtArr)

            // builder blocks
            val blocksArr = JSONArray()
            builderBlocks.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("type", it.type)
                obj.put("title", it.title)
                obj.put("content", it.content)
                val propsObj = JSONObject()
                it.properties.forEach { (k, v) -> propsObj.put(k, v) }
                obj.put("properties", propsObj)
                blocksArr.put(obj)
            }
            root.put("builder_blocks", blocksArr)

            return root.toString(2)
        } catch (e: Exception) {
            Log.e("MetaDBState", "Error parsing state to XML/JSON", e)
            return "{}"
        }
    }

    // ==========================================
    // MODULE: DESERIALIZATION / FROM JSON
    // ==========================================
    fun fromJson(jsonStr: String): Boolean {
        try {
            val root = JSONObject(jsonStr)

            // User profile
            if (root.has("user_profile")) {
                val user = root.getJSONObject("user_profile")
                userName = user.optString("name", userName)
                userEmail = user.optString("email", userEmail)
                userRole = user.optString("role", userRole)
                appThemeMode = user.optString("themeMode", appThemeMode)
            }

            // Memories
            if (root.has("memories")) {
                val memArr = root.getJSONArray("memories")
                val loadedMem = mutableListOf<MetaMemory>()
                for (i in 0 until memArr.length()) {
                    val o = memArr.getJSONObject(i)
                    loadedMem.add(MetaMemory(
                        o.getString("id"),
                        o.getString("type"),
                        o.getString("title"),
                        o.getString("reason"),
                        o.getString("status")
                    ))
                }
                memories.clear()
                memories.addAll(loadedMem)
            }

            // Journals
            if (root.has("journals")) {
                val jourArr = root.getJSONArray("journals")
                val loadedJour = mutableListOf<MetaJournalEntry>()
                for (i in 0 until jourArr.length()) {
                    val o = jourArr.getJSONObject(i)
                    val changesList = mutableListOf<String>()
                    val cArr = o.getJSONArray("changes")
                    for (j in 0 until cArr.length()) { changesList.add(cArr.getString(j)) }

                    val notesList = mutableListOf<String>()
                    val nArr = o.optJSONArray("notes")
                    if (nArr != null) {
                        for (j in 0 until nArr.length()) { notesList.add(nArr.getString(j)) }
                    }

                    val issuesList = mutableListOf<String>()
                    val isArr = o.optJSONArray("issues")
                    if (isArr != null) {
                        for (j in 0 until isArr.length()) { issuesList.add(isArr.getString(j)) }
                    }

                    val ideasList = mutableListOf<String>()
                    val idArr = o.optJSONArray("ideas")
                    if (idArr != null) {
                        for (j in 0 until idArr.length()) { ideasList.add(idArr.getString(j)) }
                    }

                    loadedJour.add(MetaJournalEntry(
                        date = o.getString("date"),
                        version = o.getString("version"),
                        author = o.optString("author", "Andarer"),
                        changes = changesList,
                        notes = notesList,
                        issues = issuesList,
                        ideas = ideasList
                    ))
                }
                journals.clear()
                journals.addAll(loadedJour)
            }

            // Projects
            if (root.has("projects")) {
                val projArr = root.getJSONArray("projects")
                val loadedProj = mutableListOf<MetaProject>()
                for (i in 0 until projArr.length()) {
                    val o = projArr.getJSONObject(i)
                    val tasksList = mutableListOf<MetaTask>()
                    val tArr = o.getJSONArray("tasks")
                    for (j in 0 until tArr.length()) {
                        val tObj = tArr.getJSONObject(j)
                        tasksList.add(MetaTask(
                            tObj.getString("id"),
                            tObj.getString("text"),
                            tObj.getBoolean("isCompleted")
                        ))
                    }
                    loadedProj.add(MetaProject(
                        o.getString("id"),
                        o.getString("title"),
                        o.getString("description"),
                        o.getString("status"),
                        o.getInt("progress"),
                        tasksList
                    ))
                }
                projects.clear()
                projects.addAll(loadedProj)
            }

            // Wiki
            if (root.has("wiki")) {
                val wikiArr = root.getJSONArray("wiki")
                val loadedWiki = mutableListOf<MetaWikiEntry>()
                for (i in 0 until wikiArr.length()) {
                    val o = wikiArr.getJSONObject(i)
                    loadedWiki.add(MetaWikiEntry(
                        o.getString("id"),
                        o.getString("title"),
                        o.getString("category"),
                        o.getString("date"),
                        o.optString("excerpt", ""),
                        o.getString("content")
                    ))
                }
                wikiEntries.clear()
                wikiEntries.addAll(loadedWiki)
            }

            // Prompts
            if (root.has("prompts")) {
                val prmArr = root.getJSONArray("prompts")
                val loadedPrm = mutableListOf<MetaPrompt>()
                for (i in 0 until prmArr.length()) {
                    val o = prmArr.getJSONObject(i)
                    loadedPrm.add(MetaPrompt(
                        o.getString("id"),
                        o.getString("category"),
                        o.getString("title"),
                        o.getString("prompt")
                    ))
                }
                prompts.clear()
                prompts.addAll(loadedPrm)
            }

            // Changelog
            if (root.has("changelog")) {
                val chgArr = root.getJSONArray("changelog")
                val loadedChg = mutableListOf<MetaChangelogEntry>()
                for (i in 0 until chgArr.length()) {
                    val o = chgArr.getJSONObject(i)
                    
                    val add = mutableListOf<String>()
                    val adArr = o.optJSONArray("added")
                    if (adArr != null) { for (j in 0 until adArr.length()) add.add(adArr.getString(j)) }

                    val ch = mutableListOf<String>()
                    val chArr = o.optJSONArray("changed")
                    if (chArr != null) { for (j in 0 until chArr.length()) ch.add(chArr.getString(j)) }

                    val fx = mutableListOf<String>()
                    val fxArr = o.optJSONArray("fixed")
                    if (fxArr != null) { for (j in 0 until fxArr.length()) fx.add(fxArr.getString(j)) }

                    val rm = mutableListOf<String>()
                    val rmArr = o.optJSONArray("removed")
                    if (rmArr != null) { for (j in 0 until rmArr.length()) rm.add(rmArr.getString(j)) }

                    loadedChg.add(MetaChangelogEntry(
                        o.getString("version"),
                        o.getString("date"),
                        o.optBoolean("isCurrent", false),
                        added = add,
                        changed = ch,
                        fixed = fx,
                        removed = rm
                    ))
                }
                changelog.clear()
                changelog.addAll(loadedChg)
            }

            // Features
            if (root.has("features")) {
                val featArr = root.getJSONArray("features")
                val loadedFeat = mutableListOf<MetaFeature>()
                for (i in 0 until featArr.length()) {
                    val o = featArr.getJSONObject(i)
                    loadedFeat.add(MetaFeature(
                        o.getString("id"),
                        o.getString("name"),
                        o.getString("version"),
                        o.getString("status")
                    ))
                }
                features.clear()
                features.addAll(loadedFeat)
            }

            // Debt
            if (root.has("debt")) {
                val debtArr = root.getJSONArray("debt")
                val loadedDebt = mutableListOf<MetaDebtItem>()
                for (i in 0 until debtArr.length()) {
                    val o = debtArr.getJSONObject(i)
                    loadedDebt.add(MetaDebtItem(
                        o.getString("id"),
                        o.getString("priority"),
                        o.getString("module"),
                        o.getString("issue"),
                        o.getString("created")
                    ))
                }
                debtItems.clear()
                debtItems.addAll(loadedDebt)
            }

            // Builder blocks
            if (root.has("builder_blocks")) {
                val blocksArr = root.getJSONArray("builder_blocks")
                val loadedBlocks = mutableListOf<BuilderBlock>()
                for (i in 0 until blocksArr.length()) {
                    val o = blocksArr.getJSONObject(i)
                    val propsObj = o.optJSONObject("properties")
                    val propsMap = mutableMapOf<String, String>()
                    if (propsObj != null) {
                        val keys = propsObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            propsMap[k] = propsObj.getString(k)
                        }
                    }
                    loadedBlocks.add(BuilderBlock(
                        o.getString("id"),
                        o.getString("type"),
                        o.getString("title"),
                        o.getString("content"),
                        propsMap
                    ))
                }
                builderBlocks.clear()
                builderBlocks.addAll(loadedBlocks)
            }

            return true
        } catch (e: Exception) {
            Log.e("MetaDBState", "Error parsing imports", e)
            return false
        }
    }

    companion object {
        private const val DB_FILE_NAME = "metadb_config.json"
        
        fun loadFromFile(context: Context): MetaDBState {
            val state = MetaDBState()
            try {
                val file = File(context.filesDir, DB_FILE_NAME)
                if (file.exists()) {
                    val jsonStr = file.readText()
                    state.fromJson(jsonStr)
                }
            } catch (e: Exception) {
                Log.e("MetaDBState", "Failed reading storage", e)
            }
            return state
        }

        fun saveToFile(context: Context, state: MetaDBState) {
            try {
                val file = File(context.filesDir, DB_FILE_NAME)
                file.writeText(state.toJson())
            } catch (e: Exception) {
                Log.e("MetaDBState", "Failed committing state", e)
            }
        }
    }
}
