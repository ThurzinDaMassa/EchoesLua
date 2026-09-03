# Roteiro de demonstracao — Desafio 02

## Preparacao

1. Execute `lwjgl3/build/libs/EchoesLua-1.0.0.jar`.
2. Confirme audio ativo e modo `PADRAO`.
3. Pressione `ENTER` para iniciar uma missao nova.
4. Use `L` apenas quando quiser demonstrar a continuidade do autosave.

## Fluxo recomendado para o video ou avaliacao

1. Mostre que o jogo inicia na Lua e destaque O2, energia, objetivo e inventario no HUD.
2. Colete uma peca de reparo e mostre o contador sendo atualizado.
3. Aproxime-se da estacao correspondente e pressione `E`.
4. Repita ate completar os quatro reparos.
5. Colete as partes A, B e C da arma.
6. Entre na base, abra a bancada, processe uma amostra de gelo e fabrique a arma.
7. Aponte para um hostil com o mouse e segure o clique esquerdo ate elimina-lo.
8. Passe perto do portal para mostrar a validacao dos requisitos.
9. Recarregue O2 na base se estiver abaixo de 25%.
10. Atravesse o portal e confirme que inventario, arma e status continuam em Marte.
11. Repare os quatro satelites marcianos e entre na Base Ares.
12. Aproxime-se da Oficial Vega, pressione `E` e avance as tres falas pelo botao `CONTINUAR`.
13. Demonstre uma das rotas de prova: elimine um inimigo em Marte ou entregue a amostra de metano a Vega.
14. Aproxime-se do novo portal e mostre a mudanca de `BLOQUEADO` para `PORTAL TITA ONLINE`.
15. Atravesse o portal, mostre a barra de 3.000 HP do Predador de Metano e enfrente seus minions.
16. Entre no refugio de Tita para demonstrar a recarga segura de oxigenio e energia.
17. Abra um bau criogenico e mostre kits medicos, oxigenio, municao e materiais pelo mapa.
18. Demonstre os orbes, a rajada tripla e a onda radial do chefe; derrote-o e colete o Nucleo de Tita.
19. Use o portal de retorno direto para Marte e instale o nucleo no Reator Ares por tres segundos.
20. Mostre a sequencia animada de missao concluida e os botoes finais.

## Explicacao tecnica curta

- `MissionState` centraliza inventario, reparos, arma, inimigos e objetivos de Marte.
- `MissionSystem` consulta esse estado para liberar o portal; o portal nao decide a missao sozinho.
- `CollectionSystem` atualiza recursos e inventario ao coletar itens.
- `CombatSystem` controla mira, cadencia, projeteis, contato, dano e eliminacoes.
- `GameProgress` serializa jogador, mundo e fase atual com libGDX Preferences.
- `LunarScreen`, `MarsScreen` e `TitanScreen` sao estados de fase separados no mesmo projeto.
- `DialogSystem` avanca uma fala por acionamento e registra a autorizacao apenas no encerramento.
- O mesmo `MissionState` e `PlayerStatus` atravessa os portais, preservando continuidade.

## Evidencias T1–T7

- T1: Oficial Vega apresenta tres falas e fecha sem repetir quadros por tecla segurada.
- T2: o objetivo do HUD muda de satelites para conversa, prova, portal e ameaca de Tita.
- T3: arma possui mira, pente, reserva, cadencia, recarga e dano por projetil.
- T4: Predador de Metano tem 3.000 HP, escala de chefe, tres ataques animados e tres fases de combate.
- T5: Tita usa tela, terreno, atmosfera, itens e audio proprios.
- T5 extra: refugio pressurizado, chefe visivel, cinco minions e dezoito recursos sustentam a exploracao.
- T6: portal exige `dialogo && (combate || amostra)` e informa claramente o requisito pendente.
- T7: Continuar restaura fase, posicao, inventario, inimigos e progresso em Lua, Marte ou Tita.

## Requisitos do portal

- Quatro reparos concluidos.
- Arma EVA fabricada com as tres partes.
- Pelo menos um inimigo eliminado.
- Oxigenio acima de 25%.

## Checklist final

- [ ] Projeto abre sem crash.
- [ ] Lua e a fase inicial.
- [ ] Sete pecas podem ser coletadas.
- [ ] As quatro estacoes aparecem e todas sao reparaveis.
- [ ] Existem tres ou mais inimigos com movimento e dano.
- [ ] Craft consome as partes A, B e C.
- [ ] Arma acompanha a mira e os projeteis eliminam um inimigo.
- [ ] Portal bloqueado explica o requisito pendente.
- [ ] Marte abre e permanece jogavel.
- [ ] Autosave continua na fase e posicao corretas.
- [ ] Os quatro satelites marcianos ficam online.
- [ ] A Oficial Vega conclui o dialogo e muda o objetivo no HUD.
- [ ] Combate ou amostra libera o portal, mas nenhum deles libera sem o dialogo.
- [ ] Tita abre com visual proprio e Predador de Metano.
- [ ] Os quatro baus de Tita persistem e liberam suprimentos.
- [ ] O chefe derruba o Nucleo de Tita e o portal retorna diretamente a Marte.
- [ ] Instalar o nucleo no Reator Ares abre a tela animada de vitoria.
