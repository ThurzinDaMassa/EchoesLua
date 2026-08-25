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
6. Entre na base e pressione `C` para fabricar a arma.
7. Aponte para um hostil com o mouse e segure o clique esquerdo ate elimina-lo.
8. Passe perto do portal para mostrar a validacao dos requisitos.
9. Recarregue O2 na base se estiver abaixo de 25%.
10. Atravesse o portal e confirme que inventario, arma e status continuam em Marte.
11. Explore Marte e pressione `E` nos tres sistemas da Base Ares.
12. Mostre a tela final com pontuacao e integracao concluida.

## Explicacao tecnica curta

- `MissionState` centraliza inventario, reparos, arma, inimigos e objetivos de Marte.
- `MissionSystem` consulta esse estado para liberar o portal; o portal nao decide a missao sozinho.
- `CollectionSystem` atualiza recursos e inventario ao coletar itens.
- `CombatSystem` controla mira, cadencia, projeteis, contato, dano e eliminacoes.
- `GameProgress` serializa jogador, mundo e fase atual com libGDX Preferences.
- `LunarScreen` e `MarsScreen` sao estados de fase separados no mesmo projeto.
- O mesmo `MissionState` e `PlayerStatus` atravessa o portal, preservando continuidade.

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
- [ ] Os tres sistemas marcianos concluem a missao.
