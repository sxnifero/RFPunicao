# RFPunicao

Plugin de punicoes para servidores Paper da Rede Fenix, com menu GUI para aplicar banimentos e mutes, persistencia em banco de dados e integracao com proxy para expulsar jogadores banidos da network.

## Recursos

- Menu `/punir <nick>` para selecionar banimento ou mute.
- Punicoes permanentes ou temporarias com motivo informado pelo chat.
- Bloqueio de login para jogadores banidos.
- Bloqueio de chat e comandos privados para jogadores mutados.
- Comando `/despunir <nick>` para desativar punicoes ativas.
- Registro das punicoes em MySQL por meio do plugin `RFBancoDeDados`.
- Mensagens de staff via permissao `rf.staff`.

## Requisitos

- Java 21
- Paper 1.21.1
- Maven 3.9+
- BancoDeDados instalado no servidor
- Proxy compativel com o canal `BungeeCord` caso queira expulsar jogadores banidos de outros servidores da network

## Dependencias

O projeto usa a Paper API como dependencia `provided`, ou seja, ela e fornecida pelo servidor em tempo de execucao.

Tambem depende do BancoDeDados, declarado no `plugin.yml` com `depend: [RFBancoDeDados]`. O servidor precisa carregar esse plugin antes do `RFPunicao`, pois ele fornece o `DatabaseManager` usado para criar e consultar a tabela `punicoes`.

## Configuracao

Ao iniciar, o plugin gera o arquivo `config.yml`:

```yml
blocked-muted-commands:
  - "/g"
  - "/l"
  - "/tell"
  - "/msg"
  - "/r"
  - "/responder"

messages:
  no-permission: "&cVoce nao tem permissao para usar este comando."
  punish-usage: "&cUse: /punir <nick>"
  unpunish-usage: "&cUse: /despunir <nick>"
  punish-cancelled: "&cPunicao cancelada."
  ask-reason:
    - "&e&lREDE FENIX &8- &fPor qual motivo voce esta punindo esse jogador?"
    - "&7(Digite &ccancelar &7no chat para cancelar a operacao)"
  ask-time: "&e&lREDE FENIX &8- &fQual o tempo da punicao? &7(Ex: 1s, 1m, 1h, 1d)"
  invalid-time: "&cFormato de tempo invalido! Use: 1s, 1m, 1h, 1d."
  muted: "&cVoce esta mutado por: &e{time}&c. Motivo: &f{reason}"
  staff-punish: "&8[&c&lPUNICAO&8] &f{target} &7foi {action} por &f{staff}&7. &8| &7Motivo: &f{reason} &8| &7Tempo: {time}"
  staff-unpunish: "&8[&a&lDESPUNICAO&8] &f{target} &7foi despunido(a) por &f{staff}&7."
  unpunish-success: "&aVoce removeu as punicoes de {target} com sucesso."
  ban-screen:
    - "&c&lREDE FENIX"
    - ""
    - "&cVoce foi banido!"
    - "&7Motivo: &f{reason}"
    - "&7Expira em: {time}"
    - ""
    - "&7Faca sua revisao em: &b{appeal_url}"
  appeal-url: "https://discord.gg/"
```

- `blocked-muted-commands`: comandos bloqueados para jogadores mutados.
- `messages`: textos exibidos para jogadores e equipe, com suporte a cores usando `&`.
- Placeholders disponiveis nas mensagens: `{target}`, `{staff}`, `{reason}`, `{time}`, `{action}` e `{appeal_url}`.

## Comandos

| Comando | Permissao | Descricao |
| --- | --- | --- |
| `/punir <nick>` | `rf.staff` | Abre o menu para aplicar ban ou mute. |
| `/despunir <nick>` | `rf.admin` | Remove punicoes ativas do jogador. |

## Banco de dados

O plugin cria automaticamente a tabela `punicoes`:

```sql
CREATE TABLE IF NOT EXISTS punicoes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  alvo VARCHAR(16),
  tipo VARCHAR(10),
  motivo TEXT,
  tempo_fim BIGINT,
  staff VARCHAR(16),
  ativo BOOLEAN DEFAULT TRUE
);
```

`tempo_fim` recebe `-1` para punicoes permanentes ou um timestamp em milissegundos para punicoes temporarias.

## Build

```bash
mvn clean package
```

O arquivo final sera gerado em `target/RFPunicao-1.0.jar`.

## Instalacao

1. Compile o projeto com Maven.
2. Coloque `RFPunicao-1.0.jar` na pasta `plugins/` do servidor Paper.
3. Garanta que `RFBancoDeDados` tambem esteja em `plugins/`.
4. Reinicie o servidor.
5. Ajuste o `config.yml`, se necessario.

## Observacoes para portfolio

Este projeto demonstra criacao de comandos, menus com Inventory API, listeners de login/chat, uso de banco de dados assincrono e integracao entre servidores por plugin messaging. Para uma evolucao futura, vale considerar armazenamento por UUID em vez de nick, mensagens totalmente configuraveis e cache temporario de punicoes para reduzir consultas durante chat/comandos.
