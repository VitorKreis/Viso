# Implementation Report

## Estado inicial

- Base confirmada: `main`, tag `v2.2.0`, commit `ba9eb22`.
- Room estava na versão 6 com `exportSchema = false` e sem testes de migration.
- `CloseMonthUseCase` fazia várias escritas Room independentes, gerava ids aleatórios para histórico e podia sobrescrever o fechamento anterior.
- `BillRepository`/`GoalRepository` conheciam diretamente o adapter Firestore; `SyncUseCase` importava a implementação concreta.
- `BootReceiver` usava um `CoroutineScope` solto.
- O release workflow publicava `assembleDebug` como APK público.
- Baseline: `testDebugUnitTest` existente passou antes das alterações.

## Alterações realizadas

1. Formalização de identidade e meses em `domain/model/IdentityAndMonth.kt`, com validação de ids, valores, dias, `dueMonth` e `paidMonth`.
2. Exportação dos schemas Room `1.json` a `6.json` e centralização das migrations em `VisoMigrations`.
3. Preservação dos registros legados na migração v1→v2; a operação não remove mais metas silenciosamente.
4. Introdução dos contratos `BillRepositoryContract`, `GoalRepositoryContract`, `RemoteDataSource`, `ConfigRepositoryContract` e `MonthCloseStore`.
5. Fechamento mensal Room transacional/idempotente, também usado pelo reset automático.
6. Histórico de pagamento com id estável por mês e conta.
7. Reagendamento pós-boot com WorkManager, Hilt WorkerFactory e retry.
8. Tratamento de permissão para alarmes exatos e correções de lint bloqueadoras.
9. CI de qualidade para push/PR em `main`.
10. Workflow de release com SemVer, conferência tag/`versionName`, `versionCode` crescente, assinatura por secrets, APK release nomeado e SHA-256.
11. Template de PR, changelog e checklist de release.
12. Runner AndroidX configurado, schemas Room incluídos nos assets de teste e correções nos testes instrumentados de migration e Compose.

## Tasks concluídas

- DATA-01 — DONE.
- DATA-02 — DONE; migration e criação limpa validadas no Moto G53 5G.
- ARCH-01 — DONE; atomicidade, rollback e idempotência validados no Moto G53 5G.
- ARCH-02 — DONE em código; execução em aparelho NOT VALIDATED.
- REL-01 — DONE em código; execução no GitHub NOT VALIDATED nesta sessão.
- REL-03 — DONE em código; execução no GitHub NOT VALIDATED nesta sessão.
- REL-04 — DONE.

## Bugs encontrados

- Lint estava bloqueado por `windowLayoutInDisplayCutoutMode` em API menor que 27, alarmes exatos sem verificação de permissão e initializer duplicado do WorkManager. Corrigidos.
- O teste existente permitia uma conta não paga com `paidMonth` preenchido; o contrato agora rejeita esse estado e o teste foi corrigido.
- A migração v1→v2 removia metas de emergência adicionais; a migration foi alterada para preservar os registros.
- Testes Compose instrumentados não tinham as dependências Compose UI test; dependências foram adicionadas.
- O APK de testes usava o runner legado `android.test.InstrumentationTestRunner`; foi configurado `androidx.test.runner.AndroidJUnitRunner`.
- O `MigrationTestHelper` não encontrava os schemas no APK instrumentado; `app/schemas` foi incluído nos assets de `androidTest`.
- A asserção de `payment_history` consultava a coluna errada e o teste de Reports tentava substituir o conteúdo já criado pela Activity; ambos foram corrigidos.

## Decisões arquiteturais

- Room continua sendo a fonte operacional offline.
- `month_history` é o marcador durável de fechamento concluído.
- A transação cobre as escritas financeiras Room; DataStore, notificações e conquistas ocorrem após o commit e podem ser reparados em uma repetição sem duplicar o fechamento.
- Firebase continua apenas como adapter remoto existente. Supabase, outbox, tombstones, RLS e migração de identidade ficam fora desta execução.
- Nenhum segredo, keystore ou token foi adicionado ao repositório.

## Testes adicionados

- `MonthContractTest`.
- `CloseMonthUseCaseTest`.
- `VisoDatabaseMigrationTest`.
- `RoomMonthCloseStoreTest`.
- Dependências Compose UI test para o teste instrumentado de relatórios existente.

## Testes executados

- `./gradlew testDebugUnitTest` — PASS.
- `./gradlew lintDebug` — PASS; há warnings de dependências antigas e melhorias não bloqueadoras.
- `./gradlew assembleDebug` — PASS.
- `./gradlew compileDebugAndroidTestKotlin` — PASS.
- `./gradlew connectedDebugAndroidTest` — PASS; 5 testes executados no Moto G53 5G (Android 14).
- `./gradlew :app:printVersionName :app:printVersionCode` — PASS; saída `2.2.0` e `22`.
- `assembleRelease` com keystore temporário fora do repositório — NOT VALIDATED: chegou a `validateSigningRelease` e iniciou `minifyReleaseWithR8`, mas R8 não concluiu no tempo disponível e foi interrompido; nenhum APK release foi publicado.

## Resultado dos testes

Os testes unitários, lint, build debug e testes instrumentados estão verdes. A cadeia de migration, criação limpa do banco e fechamento Room real foram executados no Moto G53 5G. O build release continua NOT VALIDATED porque R8 não concluiu nesta máquina; os fluxos funcionais completos e a instalação/upgrade sobre uma versão anterior ainda não foram validados.

## CI criado

`.github/workflows/quality.yml` executa em push e pull request para `main`:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

`.github/PULL_REQUEST_TEMPLATE.md` registra problema, solução, arquivos, testes, impacto, riscos e checklist.

## Processo de release

`.github/workflows/release-apk.yml` executa somente para tags `vX.Y.Z`, `vX.Y.Z-alpha.N` ou `vX.Y.Z-beta.N`. O fluxo valida tag/`versionName` e `versionCode`, repete testes e lint, reconstrói o keystore em diretório temporário a partir dos secrets, gera `assembleRelease`, calcula SHA-256 e publica `Viso-X.Y.Z.apk` e o arquivo `.sha256`. Alpha e beta são prereleases.

Secrets necessários:

- `VISO_KEYSTORE_BASE64`
- `VISO_KEYSTORE_PASSWORD`
- `VISO_KEY_ALIAS`
- `VISO_KEY_PASSWORD`

## Como gerar uma release

1. Atualizar `versionName` e incrementar `versionCode` em `app/build.gradle.kts`.
2. Atualizar `CHANGELOG.md` e marcar `docs/RELEASE_CHECKLIST.md`.
3. Executar localmente os testes, lint, build debug e migrations em dispositivo/emulador.
4. Abrir PR para `main` e aguardar `quality.yml`.
5. Criar uma tag SemVer igual ao `versionName`, por exemplo `v2.3.0-alpha.1`.
6. Publicar a tag; o workflow de release fará a assinatura e a publicação.

## Riscos restantes

- A instalação/upgrade real e a matriz completa de fluxos funcionais ainda não foram validadas; a suíte instrumentada atual passou no Moto G53 5G.
- O sync Firebase ainda não tem outbox, tombstones, resolução de conflitos ou cobertura completa; o comportamento cloud-wins permanece um risco conhecido.
- Configuração ainda está apenas em DataStore e o saldo de metas ainda não é um ledger auditável.
- Não há keystore de produção nem secrets configurados neste ambiente; o build release assinado em CI está preparado, mas não foi publicado e a minificação R8 ainda precisa ser validada em CI ou em outra máquina.
- Configurações de backup remoto, RLS e Supabase não foram implementadas.

## Próximas tasks recomendadas

1. Executar `connectedDebugAndroidTest` em emulador e validar upgrade de um APK anterior.
2. Criar fake remote e definir política de conflito/tombstones antes de qualquer troca para Supabase.
3. Completar DATA-03 para todos os ViewModels e mover `ReportsRepository` para `data`.
4. Implementar ledger de metas e sincronização de configuração.
5. Configurar os secrets de assinatura em um repositório privado/controle adequado e realizar uma alpha controlada.
6. Tratar ARCH-03/04 e completar a matriz de testes funcionais.
