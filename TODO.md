# TODO - Proximas logicas do Viso

Este arquivo organiza as duas proximas frentes de produto antes da implementacao. A ideia e manter o Viso como um app que nao apenas registra contas, mas ajuda o usuario a comecar o mes certo e perceber cedo quando esta tomando decisoes financeiras ruins.

Status: primeira versao implementada. Este documento permanece como referencia para evolucoes futuras.

## 1. Abertura do Mes

### Objetivo
Criar um ritual mensal simples para lembrar o usuario de revisar/adicionar contas quando um novo mes comeca.

### Problema
Hoje o app reseta/fecha o mes, mas nao existe um estado claro dizendo: "este mes ainda nao foi preparado". O usuario pode entrar no mes sem revisar contas novas, parcelas, entradas extras ou reserva.

### Proposta
Adicionar um estado mensal chamado `month_setup`, com algo como:

- `month`: `yyyy-MM`
- `isPrepared`: boolean
- `preparedAt`: timestamp opcional
- `lastReminderAt`: timestamp opcional
- `dismissedUntil`: timestamp opcional

### Comportamento esperado
- No primeiro dia de um novo mes, o app marca o mes como "nao preparado".
- Enquanto o mes nao estiver preparado, o app mostra um painel na Home e/ou Contas.
- O app envia notificacoes lembrando de preparar o mes.
- Ao adicionar uma conta nova no mes ou tocar em "Confirmar mes revisado", o app marca o mes como preparado.
- Depois de preparado, as notificacoes param.

### Checklist sugerido na UI
- Revisar contas recorrentes
- Adicionar contas avulsas do mes
- Confirmar parcelas novas
- Registrar entradas extras esperadas
- Definir contribuicao minima para reserva
- Comecar o mes

### Telas impactadas
- Home: card "Preparar mes"
- Contas: banner no topo enquanto `isPrepared = false`
- Configuracoes: opcao para reabrir preparacao do mes
- Notificacoes: lembrete de novo mes

### Criterios de pronto
- [x] O app detecta novo mes sem depender de abrir a tela de Contas.
- [x] O usuario consegue concluir a preparacao manualmente.
- [x] Adicionar conta nova do mes tambem pode concluir a preparacao.
- [x] Notificacoes param depois da preparacao.
- [x] Meses com contas recorrentes nao exigem recadastro manual de tudo.

### Evolucoes futuras
- Criar uma tela dedicada com checklist editavel.
- Permitir adiar o lembrete por alguns dias.
- Guardar historico de meses preparados para relatorios.

## 2. Radar Financeiro

### Objetivo
Avisar cedo quando o usuario esta passando do limite, deixando de guardar dinheiro ou criando contas demais, e explicar onde o problema apareceu.

### Problema
O usuario pode resolver uma pressao financeira criando novas contas, novas parcelas ou gastando acima da renda. O app hoje mostra numeros, mas ainda nao diagnostica o comportamento.

### Proposta
Criar um diagnostico mensal com niveis:

- `OK`: dentro da regra 70-20-10
- `ATENCAO`: perto do limite ou reserva abaixo do planejado
- `RISCO`: contas acima de 70%, reserva zerada ou muitas contas novas
- `CRITICO`: gastos/contas maiores que a renda do mes

### Sinais para calcular
- Total de contas vs limite de 70%
- Margem restante ou estouro do limite
- Reserva planejada vs valor realmente adicionado
- Quantidade de contas novas no mes
- Total de parcelas ativas
- Crescimento por categoria comparado com meses anteriores
- Meses seguidos sem adicionar dinheiro na reserva

### Mensagens sugeridas
- "Voce passou R$ X do limite de contas."
- "Sua reserva ficou zerada este mes."
- "O maior aumento veio de parcelas."
- "Voce criou X contas novas neste mes."
- "Se nada mudar, voce fecha o mes sem guardar dinheiro."

### Acoes sugeridas ao usuario
- Adiar uma conta avulsa
- Reduzir uma categoria especifica
- Colocar um valor minimo na reserva
- Marcar uma conta como "problema"
- Criar plano de recuperacao para voltar ao limite

### Telas impactadas
- Home: card "Radar Financeiro"
- Relatorios: secao "Onde eu errei?"
- Contas: alerta quando nova conta estoura o limite
- Metas: aviso quando reserva fica abaixo do planejado

### Criterios de pronto
- [x] O diagnostico deve ser simples e direto.
- [x] O app deve explicar o motivo do alerta, nao apenas mostrar vermelho.
- [x] O alerta deve sugerir uma acao concreta.
- [x] O usuario deve conseguir entender a causa em menos de 10 segundos.

### Evolucoes futuras
- Registrar movimentacoes de metas por mes para saber exatamente se a reserva recebeu aporte.
- Criar a tela "Onde eu errei?" com comparacao por categoria contra meses anteriores.
- Permitir marcar uma conta como "problema" e acompanhar recorrencia.

## Bug corrigido nesta etapa

- Vencimento de contas agora permite selecionar dias 1 a 31.
- O dominio ja usa `clampDayToMonth`, entao contas nos dias 29, 30 ou 31 sao ajustadas corretamente em meses mais curtos para calculos de agenda/status/notificacao.
