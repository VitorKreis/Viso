# ADR 002 — Release gradual com GitHub Releases e assinatura de produção

- **Status:** Aceita
- **Data:** 2026-10-03
- **Contexto:** O Viso possui um workflow que publica APK debug em tags `v*`. Isso atende desenvolvimento e poucos testers técnicos, mas não oferece a garantia de uma distribuição estável.

## Decisão

Usar GitHub Releases como canal inicial para alpha e beta, com SemVer, tags imutáveis, notas de versão, SHA-256 e APK release assinado. Manter o script ADB para desenvolvimento local. Preparar Play Console Internal Testing com AAB quando o grupo de testers deixar de ser pequeno ou a instalação manual se tornar um obstáculo.

## Fluxo

```text
feature branch → PR → CI (test/lint/build) → tag SemVer
→ assembleRelease assinado → GitHub Release
→ feedback dos testers → issue reproduzível → nova versão
```

## Motivos

- É simples para um projeto pequeno.
- Não exige uma infraestrutura de distribuição complexa agora.
- Preserva a possibilidade de Play Store sem invalidar o histórico de releases.
- Diferencia claramente debug local de artefato entregue a usuários.

## Consequências

Será necessário criar e proteger um keystore, adicionar validação de versão e manter uma checklist. O GitHub Release não oferece a experiência de atualização automática da Play Store; por isso Play Internal Testing é a evolução natural, não uma obrigação imediata.

## Condições obrigatórias

- `versionCode` crescente e compatível com `versionName`.
- Keystore fora do repositório e secrets protegidos no GitHub.
- Workflow de release executa testes antes do upload.
- APK/AAB publicado acompanhado de hash, canal, versão mínima do Android e riscos conhecidos.
