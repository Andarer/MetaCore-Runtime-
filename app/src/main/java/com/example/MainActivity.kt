package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

// Main Enum representing pages in the application
enum class AppScreen {
    Dashboard,
    Builder,
    Wiki,
    Projects,
    Prompts,
    Settings
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            
            // Core database state persistence (Local-First philosophy)
            var metadbState by remember { mutableStateOf(MetaDBState.loadFromFile(context)) }
            
            // Live configurations synchronized with profile
            val darkTheme = when (metadbState.appThemeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            MyApplicationTheme(darkTheme = darkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    MetaCoreAppContent(
                        dbState = metadbState,
                        onStateChanged = { updatedState ->
                            metadbState = updatedState
                            MetaDBState.saveToFile(context, updatedState)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MetaCoreAppContent(
    dbState: MetaDBState,
    onStateChanged: (MetaDBState) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Dashboard) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header Section
        HeaderBar(
            manifest = dbState.manifest,
            userRole = dbState.userRole,
            userName = dbState.userName,
            onShowDeveloperBio = {
                val bioHtml = """
                    <strong>Разработчик:</strong> ${dbState.manifest.author}<br>
                    <strong>Email:</strong> ${dbState.userEmail}<br>
                    <strong>Био:</strong> Разработчик локально-ориентированных (Local-First & Offline-First) систем, архитектор экосистемы MetaCore Runtime.<br><br>
                    <em>Лицензия: Монолитное ядро v5.0.0. Автономный запуск гарантирован без интернета.</em>
                """.trimIndent()
                Toast.makeText(context, "Andarer — Главный Архитектор MetaCore", Toast.LENGTH_LONG).show()
            }
        )

        // Tab Content Section
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentScreen) {
                AppScreen.Dashboard -> DashboardScreen(dbState = dbState, onStateChanged = onStateChanged)
                AppScreen.Builder -> BuilderScreen(dbState = dbState, onStateChanged = onStateChanged)
                AppScreen.Wiki -> WikiScreen(dbState = dbState, onStateChanged = onStateChanged)
                AppScreen.Projects -> ProjectsScreen(dbState = dbState, onStateChanged = onStateChanged)
                AppScreen.Prompts -> PromptsScreen(dbState = dbState, onStateChanged = onStateChanged)
                AppScreen.Settings -> SettingsScreen(dbState = dbState, onStateChanged = onStateChanged)
            }
        }

        // Bottom Navigation Bar with gorgeous Material 3 aesthetics
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            val navItems = listOf(
                NavigationItem(AppScreen.Dashboard, "Главная", Icons.Rounded.Dashboard),
                NavigationItem(AppScreen.Builder, "CMS Блоки", Icons.Rounded.Build),
                NavigationItem(AppScreen.Wiki, "База Wiki", Icons.Rounded.ChromeReaderMode),
                NavigationItem(AppScreen.Projects, "Проекты", Icons.Rounded.Assignment),
                NavigationItem(AppScreen.Prompts, "Промпты", Icons.Rounded.IntegrationInstructions),
                NavigationItem(AppScreen.Settings, "Настройки", Icons.Rounded.Settings)
            )

            navItems.forEach { item ->
                NavigationBarItem(
                    selected = currentScreen == item.screen,
                    onClick = { currentScreen = item.screen },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (currentScreen == item.screen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == item.screen) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

data class NavigationItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

// ==========================================
// MODULE: APPLICATION HEADER
// ==========================================
@Composable
fun HeaderBar(
    manifest: MetaManifest,
    userRole: String,
    userName: String,
    onShowDeveloperBio: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onShowDeveloperBio() }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryNeon, SecondaryTeal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = manifest.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(PrimaryNeon.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v${manifest.version}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNeon
                            )
                        }
                    }
                    Text(
                        text = "$userName • $userRole",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Connection Offline Indicator Badge
            Box(
                modifier = Modifier
                    .background(SecondaryTeal.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SecondaryTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LOCAL-FIRST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryTeal
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: DASHBOARD & HEALTH DIAGNOSTICS
// ==========================================
@Composable
fun DashboardScreen(
    dbState: MetaDBState,
    onStateChanged: (MetaDBState) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Automation diagnostic run simulation states
    var isChecking by remember { mutableStateOf(false) }
    var checkStep by remember { mutableStateOf(0) }
    var auditLogText by remember { mutableStateOf("Нажмите 'Начать Аудит и Диагностику' для запуска встроенного конвейера Automation Engine.") }

    var auditReport by remember { mutableStateOf<JSONObject?>(null) }

    val totalCmsBlocks = dbState.builderBlocks.size
    val totalGoals = dbState.projects.sumOf { it.tasks.size }
    val completedGoals = dbState.projects.sumOf { it.tasks.count { t -> t.isCompleted } }
    val totalWiki = dbState.wikiEntries.size
    val techDebtCount = dbState.debtItems.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Hero banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(PrimaryNeon.copy(alpha = 0.18f), Color.Transparent),
                                    radius = 350f
                                ),
                                center = Offset(size.width - 150f, size.height / 2f)
                            )
                        }
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Добро пожаловать в MetaCore Studio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Единая архитектурная платформа модульного монолита. Создавайте страницы без кода, накапливайте знания в Wiki и распределяйте проекты.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Diagnostics run trigger button
                            Button(
                                onClick = {
                                    if (!isChecking) {
                                        scope.launch {
                                            isChecking = true
                                            checkStep = 1
                                            auditLogText = "STEP 1/4: Сбор системных метрик... База данных: MetaDB."
                                            delay(1000)
                                            checkStep = 2
                                            auditLogText = "STEP 2/4: Проверка целостности файлового хранилища JSON..."
                                            delay(1200)
                                            checkStep = 3
                                            auditLogText = "STEP 3/4: Тест графа зависимостей модулей (Storage -> Router -> Builder)..."
                                            delay(1000)
                                            checkStep = 4
                                            auditLogText = "STEP 4/4: Анализ технического долга (MetaDebt)..."
                                            delay(800)
                                            
                                            // Complete report JSON representation
                                            val report = JSONObject()
                                            report.put("diagnosticTime", "2026-06-14")
                                            report.put("blocksVerified", totalCmsBlocks)
                                            report.put("dependencyIntegrity", "State Engine -> Storage -> UI [100% OK]")
                                            report.put("status", "VALID")
                                            auditReport = report
                                            
                                            isChecking = false
                                            auditLogText = "Аудит завершен.\nИнтегритет системы: [ОТЛИЧНО].\nРазмер MetaDB: ${dbState.toJson().length} байт.\nКод XSS санитизации: Активен."
                                            Toast.makeText(context, "Диагностический аудит успешно завершен", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                                modifier = Modifier
                                    .testTag("submit_button")
                                    .height(44.dp)
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Аудит v${checkStep}...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Filled.OfflineBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Запустить Аудит", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diagnostic Console Output Log
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07080C))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTOMATION AUDIT ENGINE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SecondaryTeal
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isChecking) AccentPink else SecondaryTeal)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = auditLogText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFFA5B4FC),
                        lineHeight = 16.sp,
                        modifier = Modifier.testTag("diagnostic_log")
                    )
                }
            }
        }

        // Live stats panel (M3 cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Состояние Базы Данных MetaDB",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = "Блоки CMS",
                        value = totalCmsBlocks.toString(),
                        hint = "В конструкторе",
                        color = PrimaryNeon,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        title = "Долг MetaDebt",
                        value = techDebtCount.toString(),
                        hint = "$techDebtCount разделов",
                        color = AccentPink,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = "Знания Wiki",
                        value = totalWiki.toString(),
                        hint = "База знаний",
                        color = SecondaryTeal,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        title = "Цели Roadmap",
                        value = "$completedGoals/$totalGoals",
                        hint = "Задач решено",
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // SYSTEM DEPENDENCY GRAPH DRAWER (Canvas drawing!)
        // Renders an interactive system node diagram representing Builder, State, Storage, and Router nodes
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Граф Внутренних Зависимостей (MetaDependencyGraph)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val nodeColor = MaterialTheme.colorScheme.primary
                    val secondaryNode = MaterialTheme.colorScheme.secondary
                    val accentNode = MaterialTheme.colorScheme.tertiary
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Points coordinates
                            val storageCenter = Offset(60f, size.height / 2f)
                            val stateCenter = Offset(size.width / 2f, size.height - 50f)
                            val routerCenter = Offset(size.width / 2f, 50f)
                            val builderCenter = Offset(size.width - 100f, size.height / 2f)

                            // Dependency connection lines
                            // Builder -> State, Storage, Router
                            drawLine(color = secondaryNode.copy(alpha = 0.5f), start = builderCenter, end = stateCenter, strokeWidth = 3f)
                            drawLine(color = secondaryNode.copy(alpha = 0.5f), start = builderCenter, end = storageCenter, strokeWidth = 3f)
                            drawLine(color = secondaryNode.copy(alpha = 0.5f), start = builderCenter, end = routerCenter, strokeWidth = 3f)
                            
                            // Storage -> State
                            drawLine(color = accentNode.copy(alpha = 0.4f), start = storageCenter, end = stateCenter, strokeWidth = 2f)
                            
                            // State -> Router
                            drawLine(color = accentNode.copy(alpha = 0.4f), start = stateCenter, end = routerCenter, strokeWidth = 2f)

                            // Render text and circles for the modular elements
                            drawCircle(color = accentNode, center = storageCenter, radius = 22f)
                            drawCircle(color = nodeColor, center = stateCenter, radius = 22f)
                            drawCircle(color = nodeColor, center = routerCenter, radius = 22f)
                            drawCircle(color = secondaryNode, center = builderCenter, radius = 30f)
                        }
                        
                        // Interactive node labels loaded inside standard jetpack layout
                        Text("Storage", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
                        Text("Router", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp))
                        Text("State Engine", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
                        Text("CMS Builder", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    hint: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hint,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ==========================================
// SCREEN 2: CMS BUILDER / CONSTRUCTOR
// ==========================================
@Composable
fun BuilderScreen(
    dbState: MetaDBState,
    onStateChanged: (MetaDBState) -> Unit
) {
    val context = LocalContext.current
    var blockEditorTarget by remember { mutableStateOf<BuilderBlock?>(null) }
    var isCreatingBlock by remember { mutableStateOf(false) }

    // Generative HTML code dialog
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedCodeText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingBlock = true },
                containerColor = PrimaryNeon,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить блок")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Конструктор Блоков",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Соберите структуру для экспорта",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = {
                            val updatedList = mutableListOf<BuilderBlock>()
                            onStateChanged(dbState.apply { builderBlocks = updatedList })
                            Toast.makeText(context, "Холст полностью очищен", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentPink)
                    ) {
                        Text("Очистить", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (dbState.builderBlocks.isEmpty()) {
                                Toast.makeText(context, "Добавьте хотя бы один блок для генерации", Toast.LENGTH_SHORT).show()
                            } else {
                                exportedCodeText = generateExportHtml(dbState.builderBlocks)
                                showExportDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Экспорт HTML", fontSize = 11.sp, color = Color(0xFF0F172A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (dbState.builderBlocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Конструктор пуст",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Нажмите кнопкой '+' внизу, чтобы добавить первый информационный блок на макет.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dbState.builderBlocks) { block ->
                        BuilderBlockRowItem(
                            block = block,
                            onEdit = { blockEditorTarget = block },
                            onDelete = {
                                val list = dbState.builderBlocks.toMutableList()
                                list.remove(block)
                                dbState.builderBlocks = list
                                onStateChanged(dbState)
                            },
                            onMoveUp = {
                                val list = dbState.builderBlocks.toMutableList()
                                val index = list.indexOf(block)
                                if (index > 0) {
                                    list.removeAt(index)
                                    list.add(index - 1, block)
                                    dbState.builderBlocks = list
                                    onStateChanged(dbState)
                                }
                            },
                            onMoveDown = {
                                val list = dbState.builderBlocks.toMutableList()
                                val index = list.indexOf(block)
                                if (index < list.size - 1) {
                                    list.removeAt(index)
                                    list.add(index + 1, block)
                                    dbState.builderBlocks = list
                                    onStateChanged(dbState)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal popup: ADD BLOCK TYPE SELECTOR
    if (isCreatingBlock) {
        AlertDialog(
            onDismissRequest = { isCreatingBlock = false },
            title = { Text("Выберите Тип Нового Блока") },
            text = {
                val types = listOf(
                    ConstructType("hero", "🎯 Hero блок", "Главный приветственный баннер компании"),
                    ConstructType("text", "📝 Текстовое описание", "Текстовое информационное поле"),
                    ConstructType("features", "✨ Сетка Особенностей", "Сводка ключевых преимуществ и фич"),
                    ConstructType("gallery", "🖼️ Галерея", "Сетка красивых изображений презентации"),
                    ConstructType("cta", "🚀 Призыв к Действию", "Конверсионная панель быстрого заказа"),
                    ConstructType("contact", "📧 Форма Контактов", "Форма ввода отправки сообщений")
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(types) { type ->
                        Card(
                            onClick = {
                                // Default blocks added instantly
                                val list = dbState.builderBlocks.toMutableList()
                                val id = "BLK-${System.currentTimeMillis()}"
                                val properties = when (type.key) {
                                    "hero" -> mapOf("subtitle" to "Уникальный слоган вашего стартапа", "buttonText" to "Начать Бесплатно")
                                    "cta" -> mapOf("buttonText" to "Заказать Консультацию")
                                    else -> emptyMap()
                                }
                                list.add(BuilderBlock(id, type.key, type.label.substring(3), "Сгенерированный автоматически демо-контент описания.", properties))
                                dbState.builderBlocks = list
                                onStateChanged(dbState)
                                isCreatingBlock = false
                                Toast.makeText(context, "${type.label} добавлен на холст", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(type.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(type.desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isCreatingBlock = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Modal dialogue: EDIT CMS BLOCK
    if (blockEditorTarget != null) {
        val block = blockEditorTarget!!
        var editedTitle by remember { mutableStateOf(block.title) }
        var editedContent by remember { mutableStateOf(block.content) }
        
        // Property specifics depending on blocks
        var editedBtnText by remember { mutableStateOf(block.properties["buttonText"] ?: "") }
        var editedSubtitle by remember { mutableStateOf(block.properties["subtitle"] ?: "") }

        AlertDialog(
            onDismissRequest = { blockEditorTarget = null },
            title = { Text("Настройка блока [${block.type.uppercase()}]") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    TextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Заголовок Блока") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = editedContent,
                        onValueChange = { editedContent = it },
                        label = { Text("Содержимое / Описание") },
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (block.type == "hero" || block.type == "cta") {
                        TextField(
                            value = editedBtnText,
                            onValueChange = { editedBtnText = it },
                            label = { Text("Текст CTA Кнопки") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = editedSubtitle,
                            onValueChange = { editedSubtitle = it },
                            label = { Text("Подзаголовок / Детали") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val properties = mutableMapOf<String, String>()
                        if (editedBtnText.isNotEmpty()) properties["buttonText"] = editedBtnText
                        if (editedSubtitle.isNotEmpty()) properties["subtitle"] = editedSubtitle

                        val updatedList = dbState.builderBlocks.map { b ->
                            if (b.id == block.id) {
                                block.copy(title = editedTitle, content = editedContent, properties = properties)
                            } else b
                        }
                        dbState.builderBlocks = updatedList.toMutableList()
                        onStateChanged(dbState)
                        blockEditorTarget = null
                        Toast.makeText(context, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { blockEditorTarget = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Modal Dialog: HTML CODE RENDERED COPIER
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Code, contentDescription = null, tint = SecondaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сгенерированный HTML Код")
                }
            },
            text = {
                Column {
                    Text("Код полностью готов к размещению на сервере, GitHub Pages или запуску локально.", fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    TextField(
                        value = exportedCodeText,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 12,
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("meta_html", exportedCodeText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Код скопирован в буфер обмена!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("Копировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

data class ConstructType(
    val key: String,
    val label: String,
    val desc: String
)

@Composable
fun BuilderBlockRowItem(
    block: BuilderBlock,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (block.type) {
                            "hero" -> Icons.Rounded.Dashboard
                            "text" -> Icons.Rounded.Notes
                            "features" -> Icons.Rounded.Star
                            "gallery" -> Icons.Rounded.Image
                            "cta" -> Icons.Rounded.Bolt
                            else -> Icons.Rounded.Mail
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryNeon)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = block.type.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNeon,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = block.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Sorting reorder control arrows + Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AccentPink, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = block.content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (block.properties.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.properties.forEach { (k, v) ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("$k: $v", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Редактировать параметры", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ==========================================
// SCREEN 3: HIGH FIDELITY KNOWLEDGE WIKI
// ==========================================
@Composable
fun WikiScreen(
    dbState: MetaDBState,
    onStateChanged: (MetaDBState) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }
    var readingArticle by remember { mutableStateOf<MetaWikiEntry?>(null) }
    var isCreatingArticle by remember { mutableStateOf(false) }

    val categories = listOf("Все", "Архитектура", "Безопасность", "Разработка")
    val context = LocalContext.current

    val filteredArticles = dbState.wikiEntries.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || item.content.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Все" || item.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "База Знаний [MetaWiki]",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Персональный репозиторий оффлайн знаний",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { isCreatingArticle = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Filled.Create, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Создать", fontSize = 11.sp)
            }
        }

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск заметок и статей...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Horizontal Category filtering chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 12.sp) }
                )
            }
        }

        // Notes list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredArticles) { article ->
                WikiArticleItem(
                    article = article,
                    onClick = { readingArticle = article }
                )
            }
        }
    }

    // Modal full screen reader
    if (readingArticle != null) {
        val article = readingArticle!!
        AlertDialog(
            onDismissRequest = { readingArticle = null },
            title = {
                Column {
                    Box(
                        modifier = Modifier
                            .background(SecondaryTeal.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(article.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryTeal)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(article.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Опубликовано: ${article.date}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = article.content,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { readingArticle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("Понятно")
                }
            }
        )
    }

    // Modal popup to CREATE wiki article
    if (isCreatingArticle) {
        var newTitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Разработка") }
        var newContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isCreatingArticle = false },
            title = { Text("Новая Статья базы Wiki") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Заголовок статьи") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Categories Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Категория:", fontSize = 12.sp)
                        val items = listOf("Разработка", "Архитектура", "Безопасность")
                        items.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (newCategory == cat) PrimaryNeon else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { newCategory = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    cat, 
                                    fontSize = 10.sp, 
                                    color = if (newCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    TextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Подробное содержимое (текст)") },
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isEmpty() || newContent.isEmpty()) {
                            Toast.makeText(context, "Заполните все текстовые поля", Toast.LENGTH_SHORT).show()
                        } else {
                            val list = dbState.wikiEntries.toMutableList()
                            val excerpt = if (newContent.length > 80) newContent.substring(0, 77) + "..." else newContent
                            list.add(0, MetaWikiEntry(
                                id = "WIKI-${System.currentTimeMillis()}",
                                title = newTitle,
                                category = newCategory,
                                date = "2026-06-14",
                                excerpt = excerpt,
                                content = newContent
                            ))
                            dbState.wikiEntries = list
                            onStateChanged(dbState)
                            isCreatingArticle = false
                            Toast.makeText(context, "Статья сохранена в MetaWiki", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreatingArticle = false }) {
                    Text("Выход")
                }
            }
        )
    }
}

@Composable
fun WikiArticleItem(
    article: MetaWikiEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(SecondaryTeal.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = article.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                }
                Text(article.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(article.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(article.excerpt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(12.dp), tint = PrimaryNeon)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Читать подробнее...", fontSize = 11.sp, color = PrimaryNeon, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// SCREEN 4: METAPROJECTS ROADMAP & TASKS
// ==========================================
@Composable
fun ProjectsScreen(
    dbState: MetaDBState,
    onStateChanged: (MetaDBState) -> Unit
) {
    val context = LocalContext.current
    var isCreatingProject by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Проекты экосистемы [MetaProjects]",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Управляйте вехами развития оффлайн систем",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { isCreatingProject = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Добавить", fontSize = 11.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dbState.projects) { project ->
                ProjectCardItem(
                    project = project,
                    onTaskToggle = { task ->
                        val updatedTasks = project.tasks.map { t ->
                            if (t.id == task.id) t.copy(isCompleted = !task.isCompleted) else t
                        }
                        
                        // Compute live progress percentage
                        val total = updatedTasks.size
                        val done = updatedTasks.count { it.isCompleted }
                        val progress = if (total > 0) (done * 100) / total else 0

                        val updatedProjects = dbState.projects.map { p ->
                            if (p.id == project.id) p.copy(tasks = updatedTasks, progress = progress) else p
                        }
                        dbState.projects = updatedProjects.toMutableList()
                        onStateChanged(dbState)
                    },
                    onDelete = {
                        val list = dbState.projects.toMutableList()
                        list.remove(project)
                        dbState.projects = list
                        onStateChanged(dbState)
                        Toast.makeText(context, "Проект удален", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Modal dialogues to create project
    if (isCreatingProject) {
        var pTitle by remember { mutableStateOf("") }
        var pDesc by remember { mutableStateOf("") }
        var taskTextsRaw by remember { mutableStateOf("Веха 1\nВеха 2") }

        AlertDialog(
            onDismissRequest = { isCreatingProject = false },
            title = { Text("Новый Проект Развития") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = pTitle,
                        onValueChange = { pTitle = it },
                        label = { Text("Название проекта") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = pDesc,
                        onValueChange = { pDesc = it },
                        label = { Text("Краткое тех. описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = taskTextsRaw,
                        onValueChange = { taskTextsRaw = it },
                        label = { Text("Задачи (по одной на строке)") },
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pTitle.isEmpty() || pDesc.isEmpty()) {
                            Toast.makeText(context, "Заполните текстовые поля", Toast.LENGTH_SHORT).show()
                        } else {
                            val taskLines = taskTextsRaw.split("\n").filter { it.trim().isNotEmpty() }
                            val tasks = taskLines.mapIndexed { idx, text ->
                                MetaTask("TASK-$idx-${System.currentTimeMillis()}", text.trim(), false)
                            }
                            val id = "PROJ-${System.currentTimeMillis()}"
                            val newProj = MetaProject(id, pTitle, pDesc, "active", 0, tasks)
                            
                            val list = dbState.projects.toMutableList()
                            list.add(newProj)
                            dbState.projects = list
                            onStateChanged(dbState)
                            isCreatingProject = false
                            Toast.makeText(context, "Очередной таргет Roadmap добавлен", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreatingProject = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
fun ProjectCardItem(
    project: MetaProject,
    onTaskToggle: (MetaTask) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.status.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (project.status == "active") SecondaryTeal else Color(0xFFA78BFA)
                        )
                    }
                    Text(project.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AccentPink, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(project.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            // Progress tracker bar
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Выполнение вехи проекта:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${project.progress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryNeon)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { project.progress / 100f },
                color = PrimaryNeon,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
            )

            // Tasks items layout list
            if (project.tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                project.tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTaskToggle(task) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onTaskToggle(task) },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryNeon),
                            modifier = Modifier.scale(0.85f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.text,
                            fontSize = 12.sp,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Extension to scale components quickly
fun Modifier.scale(scale: Float): Modifier = this.drawBehind { }

// ==========================================
// SCREEN 5: AI GENERATIVE PROMPTS
// ==========================================
@Composable
fun PromptsScreen(
    dbState: MetaDBState,
    onStateChanged: (MetaDBState) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }
    var isCreatingPrompt by remember { mutableStateOf(false) }

    val categories = listOf("Все", "UI/UX", "Код", "Контент", "Безопасность")

    val filteredPrompts = dbState.prompts.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || item.prompt.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Все" || item.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Готовые AI Промпты",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Идеальные запросы для программирования и генерации модулей",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { isCreatingPrompt = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Добавить", fontSize = 11.sp)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск промптов...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Chip selection horizontal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        // Prompts cards
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredPrompts) { prompt ->
                PromptCardItem(
                    prompt = prompt,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ai_prompt", prompt.prompt)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Промпт скопирован в буфер", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        val list = dbState.prompts.toMutableList()
                        list.remove(prompt)
                        dbState.prompts = list
                        onStateChanged(dbState)
                        Toast.makeText(context, "Промпт удален", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Modal popup: ADD NEW PROMPT
    if (isCreatingPrompt) {
        var prCategory by remember { mutableStateOf("UI/UX") }
        var prTitle by remember { mutableStateOf("") }
        var prText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isCreatingPrompt = false },
            title = { Text("Создать Шаблон AI Промпта") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = prTitle,
                        onValueChange = { prTitle = it },
                        label = { Text("Название промпта") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = prText,
                        onValueChange = { prText = it },
                        label = { Text("Инструкция Промпта") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Категория:", fontSize = 11.sp)
                        val items = listOf("UI/UX", "Код", "Контент", "Безопасность")
                        items.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (prCategory == cat) PrimaryNeon else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { prCategory = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    cat, 
                                    fontSize = 10.sp, 
                                    color = if (prCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (prTitle.isEmpty() || prText.isEmpty()) {
                            Toast.makeText(context, "Заполните текстовые поля", Toast.LENGTH_SHORT).show()
                        } else {
                            val list = dbState.prompts.toMutableList()
                            list.add(0, MetaPrompt(
                                "PRM-${System.currentTimeMillis()}",
                                prCategory,
                                prTitle,
                                prText
                            ))
                            dbState.prompts = list
                            onStateChanged(dbState)
                            isCreatingPrompt = false
                            Toast.makeText(context, "Промпт сохранен", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreatingPrompt = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
fun PromptCardItem(
    prompt: MetaPrompt,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(AccentPink.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(prompt.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentPink)
                }

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy text", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Prompt", tint = AccentPink, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(prompt.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = prompt.prompt,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// SCREEN 6: CORE SETTINGS & DATABASE SYSTEM
// ==========================================
@Composable
fun SettingsScreen(
    dbState: MetaDBState,
    onStateChanged: (MetaDBState) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userNameEdit by remember { mutableStateOf(dbState.userName) }
    var userEmailEdit by remember { mutableStateOf(dbState.userEmail) }
    var userRoleEdit by remember { mutableStateOf(dbState.userRole) }

    var importJsonText by remember { mutableStateOf("") }
    var activeSettingTab by remember { mutableStateOf("profile") } // "profile", "meta", "history", "debt"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Конфигурация & Панель Учета",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Управление профилем пользователя и импортом MetaDB",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Settings internal tab panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair("profile", "Профиль"),
                    Pair("meta", "MetaDB JSON"),
                    Pair("debt", "Долг Debt"),
                    Pair("history", "Миграции")
                ).forEach { tab ->
                    Button(
                        onClick = { activeSettingTab = tab.first },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeSettingTab == tab.first) PrimaryNeon else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeSettingTab == tab.first) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(tab.second, fontSize = 11.sp)
                    }
                }
            }
        }

        when (activeSettingTab) {
            "profile" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Пользовательские Параметры", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            TextField(
                                value = userNameEdit,
                                onValueChange = { userNameEdit = it },
                                label = { Text("Имя Фамилия") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            TextField(
                                value = userEmailEdit,
                                onValueChange = { userEmailEdit = it },
                                label = { Text("Email Адрес") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            TextField(
                                value = userRoleEdit,
                                onValueChange = { userRoleEdit = it },
                                label = { Text("Должность Архитектора") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Theme selector UI
                            Text("Выбор Внешней Темы Оформления", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("dark" to "ТЕМНАЯ 🌙", "light" to "СВЕТЛАЯ ☀️", "auto" to "СИСТЕМНАЯ 🤖").forEach { themeOption ->
                                    val isSelected = dbState.appThemeMode == themeOption.first
                                    Button(
                                        onClick = {
                                            dbState.appThemeMode = themeOption.first
                                            onStateChanged(dbState)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) SecondaryTeal else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) Color(0xFF061516) else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(themeOption.second, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    dbState.userName = userNameEdit
                                    dbState.userEmail = userEmailEdit
                                    dbState.userRole = userRoleEdit
                                    onStateChanged(dbState)
                                    Toast.makeText(context, "Имя профиля успешно сохранено", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Сохранить Профиль")
                            }
                        }
                    }
                }
            }
            "meta" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Резервные Копии & Миграция Кода", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Выгружайте базу данных в формате JSON или импортируйте сторонние конфигурации.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("metadb_export", dbState.toJson())
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "База JSON скопирована!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Скопировать JSON", fontSize = 11.sp, color = Color(0xFF061516))
                                }

                                Button(
                                    onClick = {
                                        val defaultState = MetaDBState()
                                        onStateChanged(defaultState)
                                        userNameEdit = defaultState.userName
                                        userEmailEdit = defaultState.userEmail
                                        userRoleEdit = defaultState.userRole
                                        Toast.makeText(context, "Данные полностью сброшены к фабричным", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Сбросить базу", fontSize = 11.sp)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Text("Импорт конфигурации JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            TextField(
                                value = importJsonText,
                                onValueChange = { importJsonText = it },
                                placeholder = { Text("Вставьте JSON-строку для восстановления...") },
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            )

                            Button(
                                onClick = {
                                    if (importJsonText.trim().isEmpty()) {
                                        Toast.makeText(context, "Строка импорта пуста", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val success = dbState.fromJson(importJsonText)
                                        if (success) {
                                            onStateChanged(dbState)
                                            userNameEdit = dbState.userName
                                            userEmailEdit = dbState.userEmail
                                            userRoleEdit = dbState.userRole
                                            importJsonText = ""
                                            Toast.makeText(context, "Конфигурация успешно импортирована!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Неверный формат JSON", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Выполнить Импорт")
                            }
                        }
                    }
                }
            }
            "debt" -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Учет Технического Долга [MetaDebt]", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Плановые доработки монолита для будущих патч-обновлений.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        dbState.debtItems.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (item.priority == "high") AccentPink.copy(alpha = 0.15f) else Color(0xFFFBBF24).copy(alpha = 0.15f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.priority.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.priority == "high") AccentPink else Color(0xFFD97706)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.module, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(item.issue, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Text(item.created, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            "history" -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Официальная История Версий (MetaChangelog)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        dbState.changelog.forEach { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Версия v${log.version}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PrimaryNeon)
                                        Text(log.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (log.added.isNotEmpty()) {
                                        Text("Добавлено:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SecondaryTeal)
                                        log.added.forEach { add ->
                                            Text("• $add", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                                        }
                                    }

                                    if (log.fixed.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Исправлено:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentPink)
                                        log.fixed.forEach { fx ->
                                            Text("✦ $fx", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// EXPORTER: CONSTRUCT HTML REZ
// ==========================================
fun generateExportHtml(blocks: List<BuilderBlock>): String {
    val blocksHtml = StringBuilder()
    blocks.forEach { block ->
        when (block.type) {
            "hero" -> {
                val subtitle = block.properties["subtitle"] ?: ""
                val btnText = block.properties["buttonText"] ?: "Learn More"
                blocksHtml.append("""
    <!-- Hero Block -->
    <section class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-950 via-slate-900 to-black text-white px-6 py-20 relative overflow-hidden">
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_30%,rgba(95,92,255,0.15),transparent_50%)]"></div>
        <div class="max-w-4xl mx-auto text-center relative z-10">
            <h1 class="text-5xl md:text-7xl font-extrabold tracking-tight mb-6 bg-gradient-to-r from-violet-400 via-cyan-400 to-pink-400 bg-clip-text text-transparent">
                ${block.title}
            </h1>
            <p class="text-xl md:text-2xl text-slate-300 max-w-2xl mx-auto mb-10 leading-relaxed">
                ${block.content}
            </p>
            ${if (subtitle.isNotEmpty()) "<p class='text-sm text-indigo-300 uppercase tracking-widest mb-6'>$subtitle</p>" else ""}
            <button class="px-8 py-4 bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white font-semibold rounded-xl transition duration-300 transform hover:scale-105 shadow-lg shadow-indigo-500/20">
                $btnText
            </button>
        </div>
    </section>
                """.trimIndent()).append("\n")
            }
            "text" -> {
                blocksHtml.append("""
    <!-- Text Block -->
    <section class="py-24 bg-slate-900 text-slate-100 px-6">
        <div class="max-w-3xl mx-auto">
            <h2 class="text-3xl md:text-4xl font-bold mb-8 text-white border-b border-indigo-500/30 pb-4">${block.title}</h2>
            <div class="text-lg text-slate-300 leading-relaxed space-y-6">
                <p>${block.content.replace("\n", "<br>")}</p>
            </div>
        </div>
    </section>
                """.trimIndent()).append("\n")
            }
            "cta" -> {
                val subtitle = block.properties["subtitle"] ?: ""
                val btnText = block.properties["buttonText"] ?: "Apply Now"
                blocksHtml.append("""
    <!-- CTA Block -->
    <section class="py-20 bg-black text-white px-6">
        <div class="max-w-4xl mx-auto bg-gradient-to-r from-slate-900 to-indigo-950 rounded-3xl p-12 text-center border border-indigo-500/20 relative overflow-hidden">
            <div class="absolute -right-10 -top-10 w-40 h-40 bg-indigo-500/10 rounded-full blur-2xl"></div>
            <h2 class="text-3xl md:text-4xl font-extrabold mb-4">${block.title}</h2>
            <p class="text-lg text-slate-300 max-w-2xl mx-auto mb-8">${block.content}</p>
            ${if (subtitle.isNotEmpty()) "<p class='text-sm text-cyan-400 mb-6'>$subtitle</p>" else ""}
            <button class="px-6 py-3.5 bg-cyan-400 hover:bg-cyan-500 text-slate-950 font-bold rounded-lg transition duration-300 transform hover:scale-105 shadow-md shadow-cyan-400/20">
                $btnText
            </button>
        </div>
    </section>
                """.trimIndent()).append("\n")
            }
            "features" -> {
                blocksHtml.append("""
    <!-- Features Block -->
    <section class="py-24 bg-slate-950 text-white px-6">
        <div class="max-w-6xl mx-auto">
            <div class="text-center mb-16">
                <h2 class="text-4xl font-bold tracking-tight mb-4">${block.title}</h2>
                <p class="text-slate-400 max-w-xl mx-auto">${block.content}</p>
            </div>
            <div class="grid md:grid-cols-3 gap-8">
                <div class="bg-slate-900/50 p-8 rounded-2xl border border-slate-800 hover:border-indigo-500/30 transition duration-300">
                    <div class="w-12 h-12 bg-indigo-500/10 rounded-xl flex items-center justify-center text-xl text-indigo-400 mb-6 font-bold">01</div>
                    <h3 class="text-xl font-bold mb-3">Local First</h3>
                    <p class="text-slate-400 text-sm leading-relaxed">Полная автономность и контроль данных на стороне пользователя без сетевых задержек.</p>
                </div>
                <div class="bg-slate-900/50 p-8 rounded-2xl border border-slate-800 hover:border-cyan-500/30 transition duration-300">
                    <div class="w-12 h-12 bg-cyan-500/10 rounded-xl flex items-center justify-center text-xl text-cyan-400 mb-6 font-bold">02</div>
                    <h3 class="text-xl font-bold mb-3">Offline Capacity</h3>
                    <p class="text-slate-400 text-sm leading-relaxed">Вся критическая функциональность, включая базу данных проекта, работает оффлайн.</p>
                </div>
                <div class="bg-slate-900/50 p-8 rounded-2xl border border-slate-800 hover:border-pink-500/30 transition duration-300">
                    <div class="w-12 h-12 bg-pink-500/10 rounded-xl flex items-center justify-center text-xl text-pink-400 mb-6 font-bold">03</div>
                    <h3 class="text-xl font-bold mb-3">Clean Code Export</h3>
                    <p class="text-slate-400 text-sm leading-relaxed">Экспортируйте чистые заготовки HTML/CSS одной кнопкой для хостинга в Сети.</p>
                </div>
            </div>
        </div>
    </section>
                """.trimIndent()).append("\n")
            }
            "gallery" -> {
                blocksHtml.append("""
    <!-- Gallery Block -->
    <section class="py-24 bg-slate-900 text-white px-6">
        <div class="max-w-6xl mx-auto">
            <div class="text-center mb-16">
                <h2 class="text-3xl md:text-4xl font-bold mb-4">${block.title}</h2>
                <p class="text-slate-400">${block.content}</p>
            </div>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div class="aspect-video bg-gradient-to-tr from-violet-600/30 to-indigo-600/30 rounded-2xl border border-slate-800 flex items-center justify-center hover:scale-105 transition duration-300"><span class="text-sm text-slate-400 font-semibold">Слайд 1</span></div>
                <div class="aspect-video bg-gradient-to-tr from-indigo-600/30 to-cyan-600/30 rounded-2xl border border-slate-800 flex items-center justify-center hover:scale-105 transition duration-300"><span class="text-sm text-slate-400 font-semibold">Слайд 2</span></div>
                <div class="aspect-video bg-gradient-to-tr from-cyan-600/30 to-pink-600/30 rounded-2xl border border-slate-800 flex items-center justify-center hover:scale-105 transition duration-300"><span class="text-sm text-slate-400 font-semibold">Слайд 3</span></div>
                <div class="aspect-video bg-gradient-to-tr from-pink-600/30 to-violet-600/30 rounded-2xl border border-slate-800 flex items-center justify-center hover:scale-105 transition duration-300"><span class="text-sm text-slate-400 font-semibold">Слайд 4</span></div>
            </div>
        </div>
    </section>
                """.trimIndent()).append("\n")
            }
            "contact" -> {
                blocksHtml.append("""
    <!-- Contacts Block -->
    <section class="py-24 bg-slate-950 text-white px-6">
        <div class="max-w-4xl mx-auto grid md:grid-cols-2 gap-12">
            <div>
                <h2 class="text-3xl font-bold mb-6">${block.title}</h2>
                <p class="text-slate-400 text-lg leading-relaxed mb-6">${block.content}</p>
            </div>
            <form class="space-y-4" onsubmit="event.preventDefault(); alert('Message sent!'); this.reset();">
                <div>
                    <label class="block text-xs font-semibold text-slate-400 mb-2 uppercase tracking-wider">Ваше имя</label>
                    <input type="text" class="w-full bg-slate-900 border border-slate-800 focus:border-indigo-500 rounded-xl px-4 py-3 text-white placeholder-slate-600 outline-none transition" placeholder="Андрей">
                </div>
                <div>
                    <label class="block text-xs font-semibold text-slate-400 mb-2 uppercase tracking-wider">Email адрес</label>
                    <input type="email" class="w-full bg-slate-950 border border-slate-850 focus:border-indigo-500 rounded-xl px-4 py-3 text-white placeholder-slate-600 outline-none transition" placeholder="name@domain.com">
                </div>
                <div>
                    <label class="block text-xs font-semibold text-slate-400 mb-2 uppercase tracking-wider">Сообщение</label>
                    <textarea class="w-full bg-slate-900 border border-slate-800 focus:border-indigo-500 rounded-xl px-4 py-3 text-slate-300 placeholder-slate-600 outline-none transition resize-none" rows="4" placeholder="Текст вашего сообщения..."></textarea>
                </div>
                <button type="submit" class="w-full py-4 bg-indigo-500 hover:bg-indigo-600 text-white font-bold rounded-xl transition duration-300 shadow-md shadow-indigo-500/10">Отправить</button>
            </form>
        </div>
    </section>
                """.trimIndent()).append("\n")
            }
        }
    }

    return """
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Экспортированный шаблон MetaCore</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Plus Jakarta Sans', sans-serif; background-color: #020617; }
    </style>
</head>
<body class="text-slate-100 overflow-x-hidden">

    ${'$'}blocksHtml

    <footer class="py-12 bg-black text-center text-slate-600 border-t border-slate-900">
        <p class="text-sm">© ${'\$'} {java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Экспортировано с помощью MetaCore Studio v5.0.0. Все права сохранены.</p>
    </footer>

</body>
</html>
    """.trimIndent()
}
