# How to use the Zulip plugin

Send messages and execution summaries to Zulip streams via incoming webhooks.

## Authentication

Set `url` to a Zulip incoming webhook URL. Create one in your Zulip organization under Settings → Integrations. Store it in a [secret](https://kestra.io/docs/concepts/secret).

## Tasks

`ZulipIncomingWebhook` sends a message as a step within a flow — set `payload` to a JSON body in the Zulip [incoming webhook format](https://zulip.com/api/incoming-webhooks-overview).

`ZulipExecution` sends a structured execution summary including status, duration, and an execution link, and is designed for use with a [Flow trigger](https://kestra.io/docs/workflow-components/triggers) in a dedicated monitoring namespace that watches other namespaces for failures.
