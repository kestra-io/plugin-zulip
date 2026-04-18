# Kestra Zulip Plugin

## What

- Provides plugin components under `io.kestra.plugin.zulip`.
- Includes classes such as `ZulipIncomingWebhook`, `ZulipExecution`, `ZulipTemplate`.

## Why

- This plugin integrates Kestra with Zulip.
- It provides tasks that send messages to Zulip streams or users.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `zulip`

Infrastructure dependencies (Docker Compose services):

- `app`

### Key Plugin Classes

- `io.kestra.plugin.zulip.ZulipExecution`
- `io.kestra.plugin.zulip.ZulipIncomingWebhook`

### Project Structure

```
plugin-zulip/
├── src/main/java/io/kestra/plugin/zulip/
├── src/test/java/io/kestra/plugin/zulip/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
