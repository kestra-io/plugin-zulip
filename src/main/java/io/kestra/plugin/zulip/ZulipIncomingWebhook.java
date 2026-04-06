package io.kestra.plugin.zulip;

import java.net.URI;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Post messages through Zulip incoming webhook",
    description = "Renders the payload then POSTs it to a [Zulip incoming webhook URL](https://api.zulip.com/messaging/webhooks). Provide the full integration URL (includes API key) via a secret and adjust headers/timeouts through `options`. The task does not retry or raise on non-200 responses; monitor logs if delivery fails."
)
@Plugin(
    examples = {
        @Example(
            title = "Send a Zulip notification on a failed flow execution.",
            full = true,
            code = """
                id: unreliable_flow
                namespace: company.team

                tasks:
                  - id: fail
                    type: io.kestra.plugin.scripts.shell.Commands
                    runner: PROCESS
                    commands:
                      - exit 1

                errors:
                  - id: alert_on_failure
                    type: io.kestra.plugin.zulip.ZulipIncomingWebhook
                    url: "{{ secret('ZULIP_WEBHOOK') }}" # https://yourZulipDomain.zulipchat.com/api/v1/external/INTEGRATION_NAME?api_key=API_KEY
                    payload: |
                      {
                        "text": "Failure alert for flow {{ flow.namespace }}.{{ flow.id }} with ID {{ execution.id }}"
                      }
                """
        ),
        @Example(
            title = "Send a Zulip message via incoming webhook with a text argument.",
            full = true,
            code = """
                id: zulip_incoming_webhook
                namespace: company.team

                tasks:
                  - id: send_zulip_message
                    type: io.kestra.plugin.zulip.ZulipIncomingWebhook
                    url: "{{ secret('ZULIP_WEBHOOK') }}" # https://yourZulipDomain.zulipchat.com/api/v1/external/INTEGRATION_NAME?api_key=API_KEY
                    payload: |
                      {
                        "text": "Hello from the workflow {{ flow.id }}"
                      }
                """
        ),
        @Example(
            title = "Send a Zulip message via incoming webhook with a blocks argument, read more on [blocks](https://api.zulip.com/reference/block-kit/blocks).",
            full = true,
            code = """
                id: zulip_incoming_webhook
                namespace: company.team

                tasks:
                  - id: send_zulip_message
                    type: io.kestra.plugin.zulip.ZulipIncomingWebhook
                    url: "{{ secret('ZULIP_WEBHOOK') }}" # format: https://yourZulipDomain.zulipchat.com/api/v1/external/INTEGRATION_NAME?api_key=API_KEY
                    payload: |
                      {
                        "blocks": [
                            {
                                "type": "section",
                                "text": {
                                    "type": "mrkdwn",
                                    "text": "Hello from the workflow *{{ flow.id }}*"
                                }
                            }
                        ]
                      }
                """
        ),
    },
    aliases = "io.kestra.plugin.notifications.zulip.ZulipIncomingWebhook"
)
public class ZulipIncomingWebhook extends AbstractZulipConnection {
    @Schema(
        title = "Zulip incoming webhook URL",
        description = "Full incoming webhook URL (integration path and API key); render from a secret. See [Incoming Webhook Integrations](https://zulip.com/api/incoming-webhooks-overview) for formats."
    )
    @PluginProperty(dynamic = true, group = "main")
    @NotEmpty
    private String url;

    @Schema(
        title = "Zulip message payload",
        description = "Rendered JSON body sent as-is to the incoming webhook; follow Zulip payload schema for your integration."
    )
    @PluginProperty(group = "main")
    protected Property<String> payload;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        String url = runContext.render(this.url);

        try (HttpClient client = new HttpClient(runContext, super.httpClientConfigurationWithOptions())) {
            String payload = runContext.render(this.payload).as(String.class).orElse(null);

            runContext.logger().debug("Send Zulip webhook: {}", payload);
            HttpRequest.HttpRequestBuilder requestBuilder = createRequestBuilder(runContext)
                .addHeader("Content-Type", "application/json")
                .uri(URI.create(url))
                .method("POST")
                .body(
                    HttpRequest.StringRequestBody.builder()
                        .content(payload)
                        .build()
                );

            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = client.request(request, String.class);

            runContext.logger().debug("Response: {}", response.getBody());

            if (response.getStatus().getCode() == 200) {
                runContext.logger().info("Request succeeded");
            }
        }

        return null;
    }
}
