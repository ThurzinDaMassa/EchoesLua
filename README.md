# Echoes — Integracao Lua → Marte

Jogo 2D de sobrevivencia e exploracao espacial desenvolvido em Java 17 com
[libGDX](https://libgdx.com/). Explore a superficie, administre oxigenio e
energia, recupere a colonia e avance pela Lua, Marte e Tita.

## Gameplay

- Explore um mapa lunar com obstaculos, recursos, particulas e audio.
- Escolha Triple T, Winston, Shrek ou Neon antes da missao. Cada astronauta
  possui ciclos de quatro quadros para idle, caminhada e corrida, alem de
  resposta visual de recuo ao disparar e movimento durante a fabricacao.
- Colete oxigenio, alimento e gelo.
- Preserve a saude em combate e colete kits medicos para recuperar HP; o
  oxigenio e consumido apenas pelo ambiente.
- Entre na base para recarregar O2 e usar a bancada de fabricacao.
- Use corrida com cuidado: ela aumenta a velocidade, mas consome energia.
- Encontre pecas e repare comunicacao, energia, extracao e estufa.
- Colete tres componentes, fabrique a arma EVA, mire livremente com o mouse e
  dispare projeteis de energia contra as ameacas hostis.
- Cumpra o protocolo lunar para liberar o portal e seguir para Marte sem perder progresso.
- Restaure os quatro satelites de Marte, converse com a Oficial Vega e libere a expedicao de Tita.
- Explore Tita em um mapa proprio, abra os baus criogenicos e enfrente o Predador de Metano.
- Use o refugio pressurizado de Tita para recuperar oxigenio e energia, e colete suprimentos espalhados.
- Recupere o Nucleo de Tita, retorne diretamente a Marte e instale-o no Reator Ares para concluir a missao.
- Escolha diretamente no menu entre Facil, Medio e Dificil. Cada modo altera consumo de
  oxigenio, objetivos e multiplicador de pontos.

As configuracoes de dificuldade, volume, mute e tela cheia, assim como os
recordes, sao salvas automaticamente pelo libGDX Preferences. Durante a
missao, o autosave tambem preserva posicao, recursos, inventario, reparos,
arma, inimigos eliminados, itens coletados e a fase atual.

## Controles

| Tecla | Acao |
| --- | --- |
| `WASD` ou setas | Movimento |
| `Shift` | Correr enquanto houver energia |
| `E` | Entrar/sair da base, interagir e usar a bancada |
| `E` | Iniciar o reparo de 3 segundos em uma estacao ou satelite proximo |
| `Mouse` | Mirar a arma EVA |
| `Clique esquerdo` ou `Espaco` | Disparar; segure para fogo continuo |
| `L` | Continuar o ultimo autosave a partir do menu |
| `Esc` | Pausar ou voltar |
| `F1` | Ativar/desativar audio durante a missao |
| `R` | Recarregar a arma durante a missao |
| `M` | Voltar ao menu nas telas de pausa/resultado |
| `1` a `4` no menu | Escolher o astronauta |
| `Delete` no menu | Apagar o autosave atual |

No menu principal, clique em `FACIL`, `MEDIO` ou `DIFICIL` antes de iniciar.
Pressione `O` para abrir volume, audio e exibicao; dentro das opcoes, use
cima/baixo para o volume, `M` para mute e `F` para tela cheia.

## Requisitos do portal

O portal e liberado quando o astronauta conclui os quatro reparos, processa uma
amostra de gelo lunar, fabrica a arma, elimina as quatro ameacas e mantem o
oxigenio acima do nivel critico. O HUD organiza o protocolo em cinco etapas e
mostra a acao exata do passo atual, junto do resumo de sistemas, arma, hostis e portal.

Em Marte, a Base Ares recarrega O2 como a base lunar. A fase possui obstaculos,
recursos, kits medicos, inimigos, arma, projeteis, particulas, som ambiente e
quatro satelites danificados que levam tres segundos para serem restaurados.

Depois dos quatro satelites, a missao continua: fale com a Oficial Vega dentro
da Base Ares. O portal de Tita exige o dialogo concluido e uma de duas provas:
uma eliminacao em combate ou a entrega de uma amostra de metano. Tita possui
terreno, audio ambiente, baus, minions, chefe com tres ataques e objetivo proprios.
Depois do combate, o nucleo derrubado deve ser instalado em Marte para abrir a
tela animada de conclusao. O autosave reconhece
`LUA`, `BASE`, `MARTE`, `MARS_BASE` e `TITA`.

## Estrutura

- `core`: regras, entidades, telas Lua/Marte, HUD, audio, configuracoes e progresso.
- `lwjgl3`: inicializador e empacotamento desktop.
- `assets`: texturas, sons e definicoes de particulas.
- `core/src/test`: testes das regras de status, dificuldade e pontuacao.

A interface usa a fonte open-source Space Mono, distribuida sob a SIL Open
Font License. A licenca esta em `assets/fonts/OFL.txt`.

## Desenvolvimento

Requisitos: JDK 17 ou superior. O wrapper baixa a versao correta do Gradle.
O roteiro completo para a avaliacao esta em `DEMONSTRACAO.md`.

No Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat lwjgl3:run
```

No Linux/macOS:

```bash
./gradlew test
./gradlew lwjgl3:run
```

## Distribuicao

Gere o JAR executavel multiplataforma:

```powershell
.\gradlew.bat lwjgl3:jar
```

O arquivo sera criado em `lwjgl3/build/libs/`. Para um pacote nativo com runtime
Java incluso, use uma das tarefas do Construo, como `lwjgl3:packageWinX64`. As
variantes `packageLinuxX64`, `packageMacM1` e `packageMacX64` tambem estao
configuradas no modulo desktop.
