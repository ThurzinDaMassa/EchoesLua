# Lunar Echoes

Jogo 2D de sobrevivencia e exploracao lunar desenvolvido em Java 17 com
[libGDX](https://libgdx.com/). Explore a superficie, administre oxigenio e
energia, processe gelo na base e ative o portal de extracao.

## Gameplay

- Explore um mapa lunar com obstaculos, recursos, particulas e audio.
- Colete oxigenio, alimento e gelo.
- Volte para a base para recarregar O2 e transformar gelo em agua e H2.
- Use corrida com cuidado: ela aumenta a velocidade, mas consome energia.
- Conclua a missao rapidamente e com recursos sobrando para aumentar a pontuacao.
- Escolha entre Explorador, Padrao e Sobrevivente. Cada modo altera consumo de
  oxigenio, objetivos e multiplicador de pontos.

As configuracoes de dificuldade, volume, mute e tela cheia, assim como os
recordes, sao salvas automaticamente pelo libGDX Preferences.

## Controles

| Tecla | Acao |
| --- | --- |
| `WASD` ou setas | Movimento |
| `Shift` | Correr enquanto houver energia |
| `E` | Processar gelo dentro da base |
| `Esc` | Pausar ou voltar |
| `F1` | Ativar/desativar audio durante a missao |
| `R` | Reiniciar depois de uma falha ou iniciar nova missao |
| `M` | Voltar ao menu nas telas de pausa/resultado |

No menu, pressione `O` para abrir as opcoes. Dentro delas, use esquerda/direita
para a dificuldade, cima/baixo para o volume, `M` para mute e `F` para tela cheia.

## Estrutura

- `core`: regras, entidades, telas, HUD, audio, configuracoes e progresso.
- `lwjgl3`: inicializador e empacotamento desktop.
- `assets`: texturas, sons e definicoes de particulas.
- `core/src/test`: testes das regras de status, dificuldade e pontuacao.

A interface usa a fonte open-source Space Mono, distribuida sob a SIL Open
Font License. A licenca esta em `assets/fonts/OFL.txt`.

## Desenvolvimento

Requisitos: JDK 17 ou superior. O wrapper baixa a versao correta do Gradle.

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
