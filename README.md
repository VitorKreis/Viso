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
- [Licença](#-licença)

<br>

## 🎯 Visão Geral

Viso é um gerenciador financeiro pessoal offline-first, construído com Jetpack Compose e Material 3. O app calcula automaticamente quanto você pode gastar em contas fixas (70%), gastos pessoais (20%) e poupança (10%) baseado no seu salário — incluindo suporte a salário dividido em duas parcelas.

**Público-alvo:** Qualquer pessoa que queira organizar suas finanças mensais de forma simples e visual.

<br>

## ✨ Funcionalidades

### 🏠 Dashboard (Home)
- Visão geral do salário e distribuição financeira
- Cards de resumo da regra 70-20-10 com barra visual
- Status de contas fixas vs. limite de 70%
- Indicador de margem (verde/amarelo/vermelho)
- Próximos vencimentos (7 dias)
- Suporte a entradas extras no mês
- Cards de salário dividido com destaque do próximo pagamento

### 📄 Contas Fixas (Bills)
- Cadastro de contas com nome, valor, dia de vencimento e categoria
- 8 categorias: Moradia, Alimentação, Transporte, Saúde, Educação, Utilidade, Lazer, Outro
- Agrupamento por categoria com sticky headers
- Swipe para marcar como pago (→) ou excluir (←)
- Status automático: Pago, Hoje, Próximo, Atrasado, Futuro
- Picker personalizado de dia (drum-roll) e categoria (chips com ícones)

### 🎯 Metas de Poupança (Goals)
- Até 3 metas simultâneas
- **Reserva de emergência automática** (3× suas contas mensais)
- Barra de progresso animada
- Contribuição mensal configurável
- Estimativa de meses para conclusão
- Adicionar valores avulsos a qualquer momento

### 📅 Agenda
- Calendário mensal interativo com eventos coloridos
- Dots indicando contas (azul=pago, vermelho=atrasado, amarelo=pendente, verde=entrada)
- Lista de eventos do dia selecionado
- Navegação entre meses
- Exibição dos dias de recebimento (parcela 1 e 2 no modo dividido)

### ⚙️ Configurações
- Modo de salário: parcela única ou duas parcelas
- Dia(s) de recebimento configuráveis
- Entradas extras do mês
- Notificações de vencimento (1 a 7 dias antes)
- Reset completo dos dados

### 🚀 Onboarding
- Fluxo guiado em 3 etapas para novos usuários
- Configuração do salário (único ou dividido)
- Cadastro das contas fixas iniciais
- Resumo com a distribuição 70-20-10

### 🔔 Notificações
- Alarmes exatos para lembrar de contas próximas ao vencimento
- Reagendamento automático após reinicialização do dispositivo
- Configurável de 1 a 7 dias de antecedência

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
│  Room DB  ·  DataStore  ·  Hilt DI       │
└─────────────────────────────────────────┘
```

**Padrões:**
- **MVVM** — ViewModels expõem `StateFlow<UiState>` consumidos pelas Screens
- **Repository Pattern** — Abstração sobre Room DAOs e DataStore
- **Use Cases** — Lógica de negócio isolada (cálculo de regra, reset mensal, notificações)
- **Dependency Injection** — Hilt com `@HiltViewModel` e `@Inject constructor`
- **Reactive Streams** — `Flow` do Room + `combine()` nos ViewModels
- **Offline-first** — Todos os dados persistidos localmente (Room + DataStore)

<br>

## 📁 Estrutura do Projeto

```
app/src/main/java/com/viso/
├── MainActivity.kt                # Activity principal (edge-to-edge)
├── MainApplication.kt             # @HiltAndroidApp
│
├── data/
│   ├── datastore/
│   │   └── ConfigDataStore.kt     # Preferências do usuário (DataStore)
│   ├── db/
│   │   ├── VisoDB.kt              # Room Database (v1)
│   │   ├── dao/
│   │   │   ├── BillDao.kt
│   │   │   ├── ExtraIncomeDao.kt
│   │   │   ├── GoalDao.kt
│   │   │   └── MonthHistoryDao.kt
│   │   └── entity/
│   │       ├── BillEntity.kt
│   │       ├── ExtraIncomeEntity.kt
│   │       ├── GoalEntity.kt
│   │       └── MonthHistoryEntity.kt
│   └── repository/
│       ├── BillRepository.kt
│       ├── ConfigRepository.kt
│       ├── ExtraIncomeRepository.kt
│       ├── GoalRepository.kt
│       └── HistoryRepository.kt
│
├── di/
│   └── AppModule.kt               # Hilt module (@Provides)
│
├── domain/
│   ├── model/
│   │   ├── Bill.kt
│   │   ├── Config.kt              # SalaryMode (SINGLE/SPLIT)
│   │   ├── ExtraIncome.kt
│   │   ├── FinancialSummary.kt    # SalaryPart, distributeBillsByPart()
│   │   └── Goal.kt
│   └── usecase/
│       ├── CalculateRuleUseCase.kt # Regra 70-20-10
│       ├── Calculations.kt        # billsMargin, emergencyFundTarget, etc.
│       ├── GetUpcomingBillsUseCase.kt
│       ├── MonthlyResetUseCase.kt
│       └── ScheduleNotificationsUseCase.kt
│
├── notification/
│   ├── BillAlarmReceiver.kt       # Receiver de alarmes
│   ├── BootReceiver.kt            # Reagenda após reboot
│   ├── BootReceiverEntryPoint.kt  # Hilt entry point
│   └── NotificationHelper.kt     # Criação de notificações
│
└── ui/
    ├── agenda/
    │   ├── AgendaScreen.kt
    │   └── AgendaViewModel.kt
    ├── bills/
    │   ├── BillsScreen.kt
    │   └── BillsViewModel.kt
    ├── components/
    │   ├── BillCard.kt            # Swipe-to-dismiss card
    │   ├── EmptyState.kt          # Estado vazio genérico
    │   ├── GoalCard.kt            # Card de meta com progresso
    │   ├── MonthCalendar.kt       # Calendário mensal responsivo
    │   ├── RuleBar.kt             # Barra visual 70-20-10
    │   ├── StatusBadge.kt         # Badge de status da conta
    │   ├── SummaryGrid.kt         # Grid de resumo financeiro
    │   ├── VisoBottomSheet.kt     # Bottom sheet com safe area
    │   ├── VisoCategoryPicker.kt  # Seletor de categoria (FilterChips)
    │   └── VisoNumberPicker.kt    # Picker drum-roll numérico
    ├── config/
    │   ├── ConfigScreen.kt
    │   └── ConfigViewModel.kt
    ├── goals/
    │   ├── GoalsScreen.kt
    │   └── GoalsViewModel.kt
    ├── home/
    │   ├── HomeScreen.kt
    │   └── HomeViewModel.kt
    ├── navigation/
    │   └── VisoNavGraph.kt        # NavHost + bottom navigation
    ├── onboarding/
    │   ├── OnboardingScreen.kt
    │   ├── OnboardingUiState.kt
    │   └── OnboardingViewModel.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Shape.kt
    │   ├── Theme.kt
    │   └── Typography.kt
    └── utils/
        └── FormatCurrency.kt      # formatCurrency() → "R$ 1.234,56"
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
enum class SalaryMode { SINGLE, SPLIT }

data class Config(
    val salaryCents: Long,          // Salário em centavos (modo único)
    val payday: Int,                // Dia de recebimento
    val onboardingDone: Boolean,    // Onboarding finalizado
    val notifDaysBefore: Int,       // Dias de antecedência para notificação
    val lastResetMonth: String,     // Último mês resetado
    val salaryMode: SalaryMode,     // Modo: SINGLE ou SPLIT
    val salary1Cents: Long,         // Parcela 1 (modo dividido)
    val payday1: Int,               // Dia recebimento parcela 1
    val salary2Cents: Long,         // Parcela 2 (modo dividido)
    val payday2: Int                // Dia recebimento parcela 2
)
```

### Bill (Conta Fixa)
```kotlin
data class Bill(
    val id: String,
    val name: String,               // Nome da conta
    val amountCents: Long,          // Valor em centavos
    val dueDay: Int,                // Dia de vencimento (1-28)
    val category: String,           // Categoria
    val isPaid: Boolean,            // Status de pagamento
    val paidMonth: String,          // Mês do pagamento
    val createdAt: Long             // Timestamp de criação
)
```

### Goal (Meta de Poupança)
```kotlin
data class Goal(
    val id: String,
    val name: String,
    val targetAmountCents: Long,        // Meta total
    val currentAmountCents: Long,       // Valor acumulado
    val monthlyContributionCents: Long, // Contribuição mensal
    val isEmergencyFund: Boolean,       // É reserva de emergência
    val color: String,                  // Cor do card (blue/teal/green)
    val createdAt: Long
)
```

### SalaryPart (Parcela do Salário)
```kotlin
data class SalaryPart(
    val amountCents: Long,              // Valor da parcela
    val payday: Int,                    // Dia de recebimento
    val billsAssigned: List<Bill>,      // Contas atribuídas
    val totalAssignedCents: Long,       // Total das contas
    val remainingCents: Long            // Sobra após contas
)
```

<br>

## 🗄 Banco de Dados

**Room Database** — `VisoDB` (versão 1)

| Tabela | Entidade | Descrição |
|--------|----------|-----------|
| `bills` | `BillEntity` | Contas fixas mensais |
| `goals` | `GoalEntity` | Metas de poupança |
| `extra_incomes` | `ExtraIncomeEntity` | Entradas extras do mês |
| `month_history` | `MonthHistoryEntity` | Histórico mensal |

**Operações principais:**
- `BillDao` — CRUD + `resetAllPaidStatus()` + `markAsPaid()`
- `GoalDao` — CRUD + `getEmergencyFund()` + `getGoalCount()`
- `ExtraIncomeDao` — CRUD por mês
- `MonthHistoryDao` — Inserir/consultar histórico

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
└──────────────────────────────────────────┘
```

- **Root NavController**: Onboarding → Main flow
- **Tab NavController**: Navegação entre as 4 abas principais
- **Bottom Navigation**: `NavigationBar` com 4 itens

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

### Tipografia

| Estilo | Tamanho | Peso |
|--------|---------|------|
| `displayLarge` | 36sp | Bold |
| `headlineMedium` | 22sp | SemiBold |
| `titleMedium` | 15sp | Medium |
| `bodyMedium` | 13sp | Normal |
| `labelSmall` | 11sp | Medium |

### Espaçamento

| Token | Valor |
|-------|-------|
| `xs` | 4dp |
| `sm` | 8dp |
| `md` | 12dp |
| `lg` | 16dp |
| `xl` | 20dp |
| `xxl` | 24dp |
| `xxxl` | 32dp |

### Cantos Arredondados

| Tamanho | Raio |
|---------|------|
| Small | 8dp |
| Medium | 12dp |
| Large | 16dp |
| Extra Large | 20dp |

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
git clone https://github.com/seu-usuario/viso.git
cd viso
```

### Build debug
```bash
./gradlew assembleDebug
```

### Instalar no dispositivo conectado
```bash
./gradlew installDebug
```

### Build release
```bash
./gradlew assembleRelease
```

O APK gerado estará em `app/build/outputs/apk/`.

<br>

## 📄 Licença

Este projeto é de uso pessoal.

---

**Viso v1.0.0** — Feito com Kotlin + Jetpack Compose

## 🧾 Notas do Desenvolvedor (recente)

Mudanças importantes (abril/2026):

- Recorrência mensal de contas: adicionada a flag `isRecurring` ao modelo/entidade `Bill` e mapeada pelo repositório. Arquivos principais: [app/src/main/java/com/viso/ui/bills/BillsScreen.kt](app/src/main/java/com/viso/ui/bills/BillsScreen.kt), [app/src/main/java/com/viso/ui/bills/BillsViewModel.kt](app/src/main/java/com/viso/ui/bills/BillsViewModel.kt), [app/src/main/java/com/viso/data/repository/BillRepository.kt](app/src/main/java/com/viso/data/repository/BillRepository.kt), [app/src/main/java/com/viso/domain/model/Bill.kt](app/src/main/java/com/viso/domain/model/Bill.kt), [app/src/main/java/com/viso/data/db/entity/BillEntity.kt](app/src/main/java/com/viso/data/db/entity/BillEntity.kt).

- Migração Room (v1 → v2): o schema do DB foi incrementado para `version = 2` e foi adicionada uma Migration(1,2) em [app/src/main/java/com/viso/di/AppModule.kt](app/src/main/java/com/viso/di/AppModule.kt) que cria a coluna `isRecurring` com valor padrão `0`.

- Testes unitários: teste isolado incluído em [app/src/test/java/com/viso/data/repository/BillRepositoryTest.kt](app/src/test/java/com/viso/data/repository/BillRepositoryTest.kt) que valida persistência de `isRecurring` e deduplicação.

- Metas (Goals): edição e exclusão de metas já estão expostas na UI em [app/src/main/java/com/viso/ui/goals/GoalsScreen.kt](app/src/main/java/com/viso/ui/goals/GoalsScreen.kt) e [app/src/main/java/com/viso/ui/goals/GoalsViewModel.kt](app/src/main/java/com/viso/ui/goals/GoalsViewModel.kt).

### Como testar localmente

1. Rodar testes unitários:

```bash
./gradlew test --no-daemon
```

2. Gerar e instalar o APK de debug (dispositivo conectado; aceite o prompt de depuração USB):

```bash
./gradlew installDebug --no-daemon --console=plain
# Se o adb não estiver no PATH, use:
"C:\Users\Usuario\AppData\Local\Android\Sdk\platform-tools\adb" devices -l
```

3. QA rápido após instalar:
- Criar uma nova conta e marcar a opção "Recorrente mensal"; confirmar que o registro é salvo.
- Marcar a conta como paga; confirmar que o campo `paidMonth` foi preenchido com o mês atual.
- Criar/editar/excluir uma meta para validar `GoalsScreen`.

### Commit & Push

```bash
git add .
git commit -m "feat: add recurring monthly bills, Room migration v1->v2, tests"
git push origin <sua-branch>
```

### Próximos passos recomendados

- Decidir comportamento de recorrência (mostrar como fixa todo mês vs. gerar ocorrências mensais históricas).
- Adicionar interface para editar `paidMonth` manualmente quando necessário.
- Atualizar changelog e versão do app se for um release.
