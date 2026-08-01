# 💰 Viso

**Organize suas finanças com inteligência.** Viso é um app Android que aplica a regra **70-20-10** para distribuir seu salário automaticamente entre contas, gastos pessoais e poupança.

<br>

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Funcionalidades](#-funcionalidades)
- [Regra 70-20-10](#-regra-70-20-10)
- [Arquitetura](#-arquitetura)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Stack Tecnológica](#-stack-tecnológica)
- [Modelos de Dados](#-modelos-de-dados)
- [Banco de Dados](#-banco-de-dados)
- [Navegação](#-navegação)
- [Design System](#-design-system)
- [Requisitos](#-requisitos)
- [Build & Instalação](#-build--instalação)
- [Changelog](#-changelog)
- [Licença](#-licença)

<br>

## 🎯 Visão Geral

Viso é um gerenciador financeiro pessoal offline-first, construído com Jetpack Compose e Material 3. O app calcula automaticamente quanto você pode gastar em contas fixas (70%), gastos pessoais (20%) e poupança (10%) baseado no seu salário — incluindo suporte a salário dividido em duas parcelas.

**Público-alvo:** Qualquer pessoa que queira organizar suas finanças mensais de forma simples e visual.

<br>

## ✨ Funcionalidades

### 🏠 Dashboard (Home)
- Visão geral do salário e distribuição financeira
- **Abertura do Mês** — Card para revisar o novo mês e parar lembretes após confirmação
- **Radar Financeiro** — Diagnóstico simples quando o mês entra em atenção, risco ou crítico
- Cards de resumo da regra 70-20-10 com barra visual
- Status de contas fixas vs. limite de 70%
- Indicador de margem (verde/amarelo/vermelho)
- Próximos vencimentos (7 dias)
- Suporte a entradas extras no mês
- Cards de salário dividido com destaque do próximo pagamento
- **🔥 Card de Streak** — Mostra quantos meses você pagou tudo em dia

### 📄 Contas Fixas (Bills)
- Cadastro de contas com nome, valor, dia de vencimento e categoria
- Controle por mês da conta: mês atual ou próximo mês, útil para contas pagas antecipadamente
- Campo "Falta pagar" com o total ainda pendente no mês selecionado
- Vencimentos do dia 1 ao 31, com ajuste automático em meses mais curtos
- Aviso antes de salvar quando uma nova conta estoura o limite de 70%
- Modal de criação/edição fecha automaticamente após salvar
- Mensagens de erro mais descritivas, separando erro de persistência de erro de notificação
- **🔄 Contas Parceladas** — Cadastre compras parceladas (2-48x), o app calcula e gera as parcelas automaticamente
- 8 categorias: Moradia, Alimentação, Transporte, Saúde, Educação, Utilidade, Lazer, Outro
- Agrupamento por categoria com sticky headers
- Swipe para marcar como pago (→) ou excluir (←)
- Status automático: Pago, Hoje, Próximo, Atrasado, Futuro
- **🎛️ Filtros** — Visualize: Todas | Pendentes | Pagas
- Filtro rápido entre mês atual e próximo mês
- **🎉 Mensagem de sucesso** quando todas as contas estão pagas
- Picker personalizado de dia (drum-roll) e categoria (chips com ícones)
- Badge visual para contas parceladas (Parcela X/Y)

### 📊 Gráfico por Categoria
- Acesse pela tela de Contas (ícone 🥧)
- Gráfico de pizza animado mostrando distribuição de gastos
- Cores distintas por categoria
- Lista detalhada com valores e percentuais
- Total consolidado

### 🎯 Metas de Poupança (Goals)
- Até 3 metas simultâneas
- **Reserva de emergência automática** (3× suas contas mensais)
- Barra de progresso animada
- Contribuição mensal configurável
- Estimativa de meses para conclusão
- Adicionar valores avulsos a qualquer momento
- Retirar valores de metas ou da reserva de emergência com validação de saldo
- Edição e exclusão de metas

### 🏆 Conquistas & Streaks
- **Streak** — Contador de meses pagando tudo em dia
- **10 Conquistas** para desbloquear:
  - 🔥 Fogo Baixo (3 meses)
  - 🔥🔥 Fogo Médio (6 meses)
  - 🔥🔥🔥 Fogo Alto (12 meses)
  - 👑 Mestre da Disciplina (24 meses)
  - 🎯 Mestre do 70-20-10
  - 💰 Economizador
  - 📈 Investidor
  - 🏆 Reserva Completa
  - 📝 Primeiro Passo
  - 📋 Organizador
- Progresso visual nas conquistas pendentes
- Raridades: Comum, Rara, Épica, Lendária

### 📅 Agenda
- Calendário mensal interativo com eventos coloridos
- Dots indicando contas (azul=pago, vermelho=atrasado, amarelo=pendente, verde=entrada)
- Lista de eventos do dia selecionado
- Navegação entre meses
- Exibição dos dias de recebimento (parcela 1 e 2 no modo dividido)

### 📈 Relatórios
- Histórico mensal de gastos
- Comparativo mês a mês
- Filtros por tipo: Consolidado, Contas, Gastos, Poupança

### ⚙️ Configurações
- Modo de salário: parcela única ou duas parcelas
- Dia(s) de recebimento configuráveis
- Entradas extras do mês
- Notificações de vencimento (1 a 7 dias antes e horário configurável)
- Fechamento manual do mês com resumo, confirmação e histórico
- Reset automático opcional no dia 1
- Reset completo dos dados

### 🚀 Onboarding
- Fluxo guiado em 3 etapas para novos usuários
- Configuração do salário (único ou dividido)
- Cadastro das contas fixas iniciais
- Resumo com a distribuição 70-20-10

### 🔔 Notificações
- Alarmes exatos para lembrar de contas próximas ao vencimento
- Lembrete de abertura do mês enquanto o mês ainda não foi revisado
- Reagendamento automático após reinicialização do dispositivo
- Configurável de 1 a 7 dias de antecedência e horário entre 6h e 22h

### 📱 Instalação Rápida no Celular
- Script `instalar-no-celular.bat` para Windows
- Detecta `adb`, instala o APK debug e tenta abrir o app automaticamente
- Fluxo pensado para testar no telefone com poucos cliques

### 📐 Responsividade
- Layouts adaptativos com `weight` e `BoxWithConstraints`
- Calendário com células proporcionais à largura da tela
- Textos com `TextOverflow.Ellipsis` para evitar overflow
- Bottom sheet com padding de navigation bar e teclado
- Suporte a edge-to-edge

<br>

## 📊 Regra 70-20-10

O Viso distribui automaticamente sua renda mensal:

| Bloco | % | Uso |
|-------|---|-----|
| **Contas** | 70% | Aluguel, água, luz, internet, etc. |
| **Gastar** | 20% | Lazer, compras, alimentação fora |
| **Guardar** | 10% | Poupança, investimentos, reserva |

```
Renda Total = Salário + Entradas Extras

Contas  = Renda × 0.70
Gastar  = Renda × 0.20
Guardar = Renda × 0.10
```

### Salário Dividido

Quando configurado em duas parcelas, o app:
- Distribui as contas fixas automaticamente entre as duas datas de recebimento
- Cada conta é atribuída à parcela com data de recebimento mais próxima (anterior) ao vencimento
- Exibe cards separados mostrando quanto cada parcela cobre e o que sobra

### Abertura do Mês

Quando o mês vira, o Viso marca o ciclo como "não revisado" e mostra um card de preparação. O usuário pode:
- Revisar contas recorrentes e parcelas
- Adicionar contas avulsas do mês
- Confirmar manualmente que o mês foi revisado

Ao adicionar uma nova conta ou tocar em "Mês revisado", o app marca o mês como preparado e para os lembretes desse ciclo.

### Radar Financeiro

O Radar analisa o mês atual e exibe um alerta quando encontra sinais de desorganização:
- Contas acima do limite de 70%
- Contas maiores que a renda do mês
- Muitas contas novas ou parcelas acumuladas
- Reserva de emergência zerada com poupança prevista

Cada alerta mostra a causa principal e uma ação sugerida, para ajudar o usuário a corrigir o mês antes de piorar.

### Contas Parceladas

Cadastre compras parceladas (ex: TV 12x de R$ 200):
- Informe o valor total e número de parcelas (2-48x)
- O app calcula automaticamente o valor de cada parcela
- A primeira parcela recebe o resto da divisão para bater o total exato
- Gera automaticamente as contas mensais no reset do mês
- Visualização clara: "Parcela 3/12" em cada conta

### Mês da Conta e Pagamento Antecipado

O Viso separa o mês da conta (`dueMonth`) do mês em que ela foi paga (`paidMonth`). Isso permite cadastrar e pagar uma conta futura sem contaminar os totais do mês atual.

Exemplo:
- Conta do carro vence dia 08 de setembro
- Você recebe dia 30 de agosto e paga antecipado
- A conta pode ser cadastrada como "próximo mês"
- Ao marcar como paga, ela continua pertencendo a setembro nos totais, agenda, radar, fechamento e streak

Essa separação evita duplicidades e impede que o fechamento do mês atual apague ou resete contas futuras já cadastradas.

<br>

## 🏗 Arquitetura

O projeto segue **MVVM** com Clean Architecture simplificada:

```
┌─────────────────────────────────────────┐
│                   UI                     │
│  Screens → ViewModels → UiState          │
├─────────────────────────────────────────┤
│                Domain                    │
│  Models  ·  UseCases  ·  Calculations    │
├─────────────────────────────────────────┤
│                 Data                     │
│  Repositories → DAOs / DataStore         │
├─────────────────────────────────────────┤
│             Infrastructure               │
│  Room DB · DataStore · Firebase · Hilt   │
└─────────────────────────────────────────┘
```

**Padrões:**
- **MVVM** — ViewModels expõem `StateFlow<UiState>` consumidos pelas Screens
- **Repository Pattern** — Abstração sobre Room DAOs e DataStore
- **Use Cases** — Lógica de negócio isolada (cálculo de regra, reset mensal, notificações, streaks)
- **Dependency Injection** — Hilt com `@HiltViewModel` e `@Inject constructor`
- **Reactive Streams** — `Flow` do Room + `combine()` nos ViewModels
- **Offline-first** — Todos os dados persistidos localmente (Room + DataStore)
- **Backup opcional** — Firebase Auth + Firestore preparados para login e sincronização

<br>

## 📁 Estrutura do Projeto

```
app/src/main/java/com/viso/
├── MainActivity.kt                # Activity principal (edge-to-edge)
├── MainApplication.kt             # @HiltAndroidApp
│
├── data/
│   ├── auth/
│   │   └── AuthRepository.kt      # Firebase Auth / login opcional
│   ├── datastore/
│   │   └── ConfigDataStore.kt     # Preferências do usuário (DataStore)
│   ├── db/
│   │   ├── VisoDB.kt              # Room Database (v6)
│   │   ├── dao/
│   │   │   ├── AchievementDao.kt  # 🏆 Conquistas
│   │   │   ├── BillDao.kt
│   │   │   ├── ExtraIncomeDao.kt
│   │   │   ├── GoalDao.kt
│   │   │   ├── InstallmentBillDao.kt  # 🔄 Parcelamentos
│   │   │   ├── MonthHistoryDao.kt
│   │   │   └── PaymentHistoryDao.kt   # Histórico detalhado
│   │   └── entity/
│   │       ├── AchievementEntity.kt   # 🏆
│   │       ├── BillEntity.kt
│   │       ├── CategorySpendTuple.kt
│   │       ├── ExtraIncomeEntity.kt
│   │       ├── GoalEntity.kt
│   │       ├── InstallmentBillEntity.kt  # 🔄
│   │       ├── MonthHistoryEntity.kt
│   │       └── PaymentHistoryEntity.kt
│   ├── notifications/
│   │   └── NotificationWorker.kt  # Worker preparado para lembretes
│   ├── repository/
│       ├── AchievementRepository.kt   # 🏆
│       ├── BillRepository.kt
│       ├── ConfigRepository.kt
│       ├── ExtraIncomeRepository.kt
│       ├── GoalRepository.kt
│       ├── HistoryRepository.kt
│       ├── InstallmentBillRepository.kt  # 🔄
│       └── PaymentHistoryRepository.kt
│   └── sync/
│       └── FirestoreSyncManager.kt # Backup/sync Firestore
│
├── di/
│   └── AppModule.kt               # Hilt module (@Provides)
│
├── domain/
│   ├── model/
│   │   ├── Achievement.kt         # 🏆 Conquistas
│   │   ├── Bill.kt
│   │   ├── CategorySpending.kt    # 📊 Gráfico
│   │   ├── Config.kt
│   │   ├── ExtraIncome.kt
│   │   ├── FinancialSummary.kt
│   │   ├── FinancialRadar.kt      # Diagnóstico mensal
│   │   ├── Goal.kt
│   │   ├── InstallmentBill.kt     # 🔄 Parcelas
│   │   ├── MonthlySpending.kt
│   │   ├── PaymentHistory.kt      # Histórico de contas pagas
│   │   └── StreakInfo.kt          # 🔥 Streaks
│   └── usecase/
│       ├── CalculateFinancialRadarUseCase.kt
│       ├── CalculateRuleUseCase.kt
│       ├── CloseMonthUseCase.kt   # Fechamento manual
│       ├── GenerateInstallmentBillsUseCase.kt  # 🔄
│       ├── GetCategoryDistributionUseCase.kt   # 📊
│       ├── GetMonthlySpendingTrendUseCase.kt
│       ├── MonthlyResetUseCase.kt
│       ├── ScheduleNotificationsUseCase.kt
│       ├── SyncUseCase.kt         # Pull/push inicial Firestore
│       └── StreakUseCases.kt      # 🔥🏆 Streaks & Conquistas
│
├── notification/
│   ├── BillAlarmReceiver.kt
│   ├── BootReceiver.kt
│   ├── BootReceiverEntryPoint.kt
│   ├── MonthSetupReminderReceiver.kt
│   └── NotificationHelper.kt
│
└── ui/
    ├── agenda/
    │   ├── AgendaScreen.kt
    │   └── AgendaViewModel.kt
    ├── auth/
    │   ├── LoginScreen.kt
    │   └── LoginViewModel.kt
    ├── bills/
    │   ├── BillsScreen.kt         # 🎛️ Filtros + Parcelas
    │   └── BillsViewModel.kt
    ├── categorychart/             # 📊 Gráfico
    │   ├── CategoryChartScreen.kt
    │   └── CategoryChartViewModel.kt
    ├── components/
    │   ├── AchievementComponents.kt  # 🏆 StreakBadge, AchievementCard
    │   ├── BillCard.kt
    │   ├── EmptyState.kt
    │   ├── GoalCard.kt
    │   ├── MonthCalendar.kt
    │   ├── PieChart.kt            # 📊
    │   ├── RuleBar.kt
    │   ├── StatusBadge.kt
    │   ├── SyncStatusIndicator.kt
    │   ├── SummaryGrid.kt
    │   ├── VisoBottomSheet.kt
    │   ├── VisoCategoryPicker.kt
    │   └── VisoNumberPicker.kt
    ├── config/
    │   ├── ConfigScreen.kt
    │   └── ConfigViewModel.kt
    ├── evolution/
    │   ├── EvolutionScreen.kt
    │   └── EvolutionViewModel.kt
    ├── goals/
    │   ├── GoalsScreen.kt
    │   └── GoalsViewModel.kt
    ├── home/
    │   ├── HomeScreen.kt          # 🔥 Card Streak
    │   └── HomeViewModel.kt
    ├── navigation/
    │   └── VisoNavGraph.kt
    ├── notifications/
    │   ├── NotificationSettingsScreen.kt
    │   └── NotificationSettingsViewModel.kt
    ├── onboarding/
    │   ├── OnboardingScreen.kt
    │   ├── OnboardingUiState.kt
    │   └── OnboardingViewModel.kt
    ├── reports/
    │   ├── ReportsScreen.kt
    │   └── ReportsViewModel.kt
    ├── streaks/                   # 🏆 Tela de Conquistas
    │   ├── StreaksScreen.kt
    │   └── StreaksViewModel.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Shape.kt
    │   ├── Theme.kt
    │   └── Typography.kt
    └── utils/
        └── FormatCurrency.kt

app/src/main/java/com/viso/widget/
├── VisoWidget.kt                  # Widget Glance
└── VisoWidgetReceiver.kt
```

<br>

## 🛠 Stack Tecnológica

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **Kotlin** | 1.9.24 | Linguagem principal |
| **Jetpack Compose** | BOM 2024.06.00 | UI declarativa |
| **Material 3** | — | Design system |
| **Room** | 2.6.1 | Banco de dados local |
| **DataStore** | 1.1.1 | Preferências persistentes |
| **Hilt** | 2.51.1 | Injeção de dependência |
| **Navigation Compose** | 2.7.7 | Navegação entre telas |
| **Coroutines** | 1.8.1 | Programação assíncrona |
| **Firebase Auth/Firestore** | BOM 32.7.0 | Login e backup opcional |
| **Google Sign-In** | 20.7.0 | Autenticação com Google |
| **WorkManager** | 2.9.0 | Infraestrutura para jobs em background |
| **Glance** | 1.1.0 | Widget Android |
| **KSP** | 1.9.24-1.0.20 | Processamento de anotações |
| **AGP** | 8.5.2 | Build system |

**Configuração do build:**
- `compileSdk` = 34
- `minSdk` = 26 (Android 8.0)
- `targetSdk` = 34
- `JVM Target` = 17

<br>

## 📦 Modelos de Dados

### Config
```kotlin
data class Config(
    val salaryCents: Long,
    val payday: Int,
    val onboardingDone: Boolean,
    val notifDaysBefore: Int,
    val lastResetMonth: String,
    val salaryMode: SalaryMode,     // SINGLE ou SPLIT
    val salary1Cents: Long,
    val payday1: Int,
    val salary2Cents: Long,
    val payday2: Int,
    val currentStreak: Int,         // 🔥 Streak atual
    val maxStreak: Int,             // 🔥 Recorde
    val notifHour: Int,             // Horário dos lembretes
    val isAutoReset: Boolean        // Fechamento automático no dia 1
)
```

### PaymentHistory (Histórico Detalhado)
```kotlin
data class PaymentHistory(
    val id: String,
    val month: String,
    val billId: String,
    val billName: String,
    val amountCents: Long,
    val category: String,
    val dueDay: Int,
    val paidAt: Long,
    val isRecurring: Boolean
)
```

### Bill (Conta Fixa)
```kotlin
data class Bill(
    val id: String,
    val name: String,
    val amountCents: Long,
    val dueDay: Int,
    val category: String,
    val isPaid: Boolean,
    val paidMonth: String,                   // mês em que foi paga
    val dueMonth: String = "",               // mês ao qual a conta pertence
    val createdAt: Long,
    val isRecurring: Boolean = false,
    val isInstallment: Boolean = false,       // 🔄 É parcela?
    val installmentNumber: Int? = null,       // 🔄 Número da parcela
    val totalInstallments: Int? = null,       // 🔄 Total de parcelas
    val parentInstallmentId: String? = null   // 🔄 ID do parcelamento
)
```

### InstallmentBill (Parcelamento)
```kotlin
data class InstallmentBill(
    val id: String,
    val name: String,
    val totalAmountCents: Long,
    val installmentAmountCents: Long,
    val totalInstallments: Int,
    val startMonth: String,
    val category: String,
    val dueDay: Int,
    val isActive: Boolean,
    val createdAt: Long
)
```

### Achievement (Conquista)
```kotlin
data class Achievement(
    val id: String,
    val type: AchievementType,      // STREAK, MILESTONE, SAVING
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val progress: Int,
    val target: Int,
    val rarity: Rarity              // COMMON, RARE, EPIC, LEGENDARY
)
```

### StreakInfo
```kotlin
data class StreakInfo(
    val currentStreak: Int,
    val maxStreak: Int,
    val lastMonthCompleted: Boolean,
    val thisMonthProgress: Float,
    val daysRemaining: Int
)
```

<br>

## 🗄 Banco de Dados

**Room Database** — `VisoDB` (versão 6)

| Tabela | Entidade | Descrição |
|--------|----------|-----------|
| `bills` | `BillEntity` | Contas fixas mensais |
| `installment_bills` | `InstallmentBillEntity` | 🔄 Parcelamentos ativos |
| `goals` | `GoalEntity` | Metas de poupança |
| `achievements` | `AchievementEntity` | 🏆 Conquistas do usuário |
| `extra_incomes` | `ExtraIncomeEntity` | Entradas extras do mês |
| `month_history` | `MonthHistoryEntity` | Histórico mensal |
| `payment_history` | `PaymentHistoryEntity` | Histórico detalhado de contas pagas |

**Migrations:**
- v1 → v2: Adiciona `isRecurring` na tabela bills
- v2 → v3: Adiciona tabela `installment_bills` e colunas de parcela em `bills`
- v3 → v4: Adiciona tabela `achievements`
- v4 → v5: Adiciona tabela `payment_history` e índice por mês
- v5 → v6: Adiciona `dueMonth` em `bills` para separar mês da conta e mês do pagamento

<br>

## 🧭 Navegação

```
┌──────────────────────────────────────────┐
│              VisoNavGraph                 │
│                                          │
│  Onboarding ──→ MainScaffold             │
│                  │                       │
│                  ├── 🏠 Início (Home)    │
│                  ├── 📄 Contas (Bills)   │
│                  ├── 🎯 Metas (Goals)    │
│                  └── 📅 Agenda           │
│                                          │
│  HomeScreen ──→ ⚙️ Configurações         │
│              → 📈 Relatórios             │
│              → 🔥 Conquistas             │
│                                          │
│  BillsScreen ──→ 📊 Gráfico Categorias   │
│                                          │
│  Auth/Login existe para backup opcional, │
│  mas o uso offline continua disponível.  │
└──────────────────────────────────────────┘
```

<br>

## 🎨 Design System

### Tema Escuro

O app utiliza um tema escuro exclusivo com paleta azul:

| Token | Hex | Uso |
|-------|-----|-----|
| `BgApp` | `#09111F` | Fundo principal |
| `BgCard` | `#0F1E35` | Cards |
| `BgCard2` | `#0C1829` | Cards secundários / Nav bar |
| `BgInput` | `#0A1525` | Campos de input |
| `BgSheet` | `#111F36` | Bottom sheets |
| `AccentBlue` | `#2A7FE0` | Cor primária |
| `AccentTeal` | `#1A9E72` | Cor secundária (metas) |
| `AccentGreen` | `#22C47E` | Sucesso / Receita |
| `AccentAmber` | `#D4920A` | Atenção / Pendente |
| `AccentRed` | `#DC3D3D` | Erro / Atrasado |
| `TextPrimary` | `#E8F0F8` | Texto principal |
| `TextSecondary` | `#7A98BB` | Texto secundário |
| `TextMuted` | `#4A6380` | Texto desabilitado |

### Cores do Gráfico de Pizza

| Categoria | Cor |
|-----------|-----|
| Moradia | `#FF6B6B` (Vermelho coral) |
| Alimentação | `#4ECDC4` (Turquesa) |
| Transporte | `#45B7D1` (Azul claro) |
| Saúde | `#96CEB4` (Verde sage) |
| Educação | `#FFEAA7` (Amarelo) |
| Utilidade | `#DDA0DD` (Lilás) |
| Lazer | `#FFB347` (Laranja) |
| Outro | `#B0C4DE` (Azul acinzentado) |

<br>

## 📱 Requisitos

- **Android** 8.0+ (API 26)
- **JDK** 17
- **Android Studio** Hedgehog ou superior
- **Gradle** 8.7

### Permissões

| Permissão | Motivo |
|-----------|--------|
| `SCHEDULE_EXACT_ALARM` | Agendar lembretes de vencimento |
| `POST_NOTIFICATIONS` | Exibir notificações |
| `RECEIVE_BOOT_COMPLETED` | Reagendar alarmes após reinício |

<br>

## 🔧 Build & Instalação

### Clone o projeto
```bash
git clone https://github.com/VitorKreis/Viso.git
cd Viso
```

### Build debug
```bash
./gradlew assembleDebug
```

### Instalar no dispositivo conectado
```bash
./gradlew installDebug
```

### Instalação rápida no Windows
Conecte o telefone por USB, autorize a depuração USB e execute:

```bat
instalar-no-celular.bat
```

O script procura o `adb`, instala o APK debug no primeiro aparelho autorizado e tenta abrir o Viso automaticamente.

### Build release
```bash
./gradlew assembleRelease
```

O APK gerado estará em `app/build/outputs/apk/`.

<br>

## 📝 Changelog

### v2.2.0 (Agosto/2026)

#### ✨ Novas Funcionalidades
- Contas agora possuem mês próprio (`dueMonth`), separado do mês de pagamento (`paidMonth`)
- Aba Contas ganhou alternância rápida entre mês atual e próximo mês
- Cadastro de conta permite escolher mês atual ou próximo mês diretamente no modal
- Total "Falta pagar" mostra quanto ainda resta pagar no mês selecionado
- Cards de conta exibem o mês da conta junto ao dia de vencimento

#### 🛠 Correções
- Corrigido falso erro ao criar/editar conta quando o salvamento dava certo, mas o reagendamento de notificações falhava depois
- Modal de criação/edição agora fecha corretamente após salvar
- Erros de conta ficaram mais descritivos, incluindo tipo técnico e detalhe da falha
- Contas futuras pagas antecipadamente não entram no fechamento do mês atual
- Fechamento, reset mensal, agenda, radar, streaks e notificações passaram a respeitar o mês real da conta
- Deduplicação de contas agora considera o mês, evitando juntar contas iguais de meses diferentes
- Testes do `BillRepository` atualizados para a nova assinatura do DAO

### v2.1.0 (Julho/2026)

#### ✨ Novas Funcionalidades
- Abertura do mês com card de revisão, confirmação manual e lembretes automáticos
- Radar financeiro com diagnóstico de limite estourado, excesso de contas, parcelas e reserva parada
- Aviso preventivo ao cadastrar uma conta que ultrapassa o limite de 70%
- Vencimentos liberados até o dia 31, com ajuste em meses curtos
- Metas agora aceitam entrada e retirada de valores
- Reserva de emergência também permite movimentação manual com validação de saldo
- Fechamento manual do mês com resumo, confirmação e aviso de pendências
- Instalação rápida no celular via `instalar-no-celular.bat`
- Widget Android registrado no manifesto

#### 🧾 Fechamento do Mês
- Salva histórico mensal e histórico detalhado de contas pagas
- Reseta contas recorrentes para o próximo ciclo
- Arquiva contas avulsas pagas
- Limpa entradas extras do mês fechado
- Atualiza streaks/conquistas conforme mês completo ou com pendências

#### 🛠 Correções
- Testes unitários atualizados para os DAOs/repositórios atuais
- Horário configurado de notificação agora é usado no agendamento real
- README alinhado com Room v5, Firebase, Glance, histórico de pagamentos e sync opcional

### v2.0.0 (Maio/2026)

#### ✨ Novas Funcionalidades

**🔄 Contas Parceladas**
- Cadastro de compras parceladas (2-48x)
- Cálculo automático do valor das parcelas
- Geração automática mensal das contas
- Visualização "Parcela X/Y" nas contas
- Badge indicativo de parcelamento

**📊 Gráfico de Pizza por Categoria**
- Acesso via tela de Contas
- Visualização animada da distribuição de gastos
- Cores distintas por categoria
- Lista detalhada com percentuais

**🏆 Sistema de Streaks & Conquistas**
- Contador de meses pagando em dia
- 10 conquistas para desbloquear
- Progresso visual em conquistas pendentes
- Card de streak no dashboard
- Tela dedicada de conquistas

**🎛️ Filtros de Contas**
- Filtros: Todas | Pendentes | Pagas
- Contadores em tempo real
- Mensagem de sucesso quando todas pagas
- Contas pagas podem ser ocultadas

#### 🗄️ Database
- Migration v3 → v4
- Nova tabela: `achievements`
- Novos campos em `Config`: `currentStreak`, `maxStreak`

#### 🏗️ Arquitetura
- Novos UseCases para Streaks e Conquistas
- Repository pattern para Achievements
- Componentes reutilizáveis: StreakBadge, AchievementCard, PieChart

### v1.1.0 (Abril/2026)

#### ✨ Novas Funcionalidades
- Recorrência mensal de contas (`isRecurring`)
- Edição e exclusão de metas

#### 🗄️ Database
- Migration v1 → v2
- Coluna `isRecurring` adicionada à tabela `bills`

#### 🧪 Testes
- Testes unitários para BillRepository

### v1.0.0 (2025)

#### ✨ Funcionalidades Iniciais
- Dashboard com regra 70-20-10
- Cadastro de contas fixas
- Metas de poupança
- Agenda com calendário
- Configurações
- Onboarding
- Notificações

<br>

## 📄 Licença

Este projeto é de uso pessoal.

---

**Viso v2.2.0** — Feito com Kotlin + Jetpack Compose ❤️
