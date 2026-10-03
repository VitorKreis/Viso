# ADR 001 — Room local e Supabase como backend sincronizado

- **Status:** Aceita como direção futura
- **Data:** 2026-10-03
- **Contexto:** O Viso é offline-first. Room/DataStore já sustentam todas as telas, enquanto o Firestore atual cobre apenas parte dos dados e sincroniza de forma pontual.

## Decisão

Manter Room e DataStore como persistência operacional local. Introduzir Supabase como backend remoto para usuários autenticados, por meio de contratos de repositório e um coordenador de sincronização com outbox, retry, versões e tombstones.

Não migrar diretamente a UI ou trocar o banco local por chamadas de rede. A migração deve ser incremental e só deve substituir o Firebase quando autenticação, schema, RLS, importação e sincronização estiverem validados.

## Motivos

- O uso principal precisa funcionar sem rede.
- Room oferece reatividade e baixa latência para cálculos mensais.
- PostgreSQL/Supabase é adequado para dados relacionais, relatórios e futuro multiusuário.
- Separar local/remoto reduz o risco de a troca de backend atravessar a UI e o domínio.
- O modelo atual ainda precisa definir conflitos, meses e histórico antes de receber múltiplos dispositivos.

## Consequências

Será necessário manter duas representações por algum tempo, implementar outbox e testes de conflito. Em troca, o app preserva a experiência offline, permite sincronização gradual e pode migrar o provedor remoto sem reescrever as telas.

## Condições obrigatórias

- RLS em todas as tabelas com dados de usuário.
- Nenhuma service role key no APK ou no GitHub.
- Importação idempotente e rollback local.
- Testes de isolamento, falha de rede, repetição e exclusão.
- Contrato único para `dueMonth`, `paidMonth` e contas recorrentes.
