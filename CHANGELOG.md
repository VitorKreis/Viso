# Changelog

## [Unreleased]

### Added

- Schemas Room versionados e testes de migration.
- Contratos incrementais de persistência local/remota.
- CI de qualidade e workflow de release seguro.

### Changed

- Fechamento mensal e reset automático agora usam transação Room e marcador idempotente.
- Reagendamento de notificações após boot usa WorkManager.

### Fixed

- Validação de identidade, `dueMonth` e `paidMonth`.
- Migração v1→v2 sem remoção silenciosa de metas.
- Bloqueadores de lint para alarmes exatos, cutout e WorkManager.

### Security

- APK de release não usa mais o fluxo público de APK debug.
- Keystore e senhas são consumidos somente por GitHub Secrets.

## [2.2.0]

- Versão base analisada desta execução.
