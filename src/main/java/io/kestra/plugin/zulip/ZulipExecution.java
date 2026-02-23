package io.kestra.plugin.zulip;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.plugins.notifications.ExecutionInterface;
import io.kestra.core.plugins.notifications.ExecutionService;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Notify Zulip about a flow execution",
    description = "Renders a templated Zulip message with execution metadata (link, ID, namespace, flow name, start date, duration, terminal status, and failing task when applicable). Use inside [Flow triggers](https://kestra.io/docs/administrator-guide/monitoring#alerting); for `errors` handlers prefer [`ZulipIncomingWebhook`](https://kestra.io/plugins/plugin-zulip/io.kestra.plugin.zulip.zulipincomingwebhook). Configure channel/user/icon via template fields."
)
@Plugin(
    examples = {
        @Example(
            title = "Send a Zulip notification on a failed flow execution.",
            full = true,
            code = """
                id: failure_alert
                namespace: company.team

                tasks:
                  - id: send_alert
                    type: io.kestra.plugin.zulip.ZulipExecution
                    url: "{{ secret('ZULIP_WEBHOOK') }}" # format: https://yourZulipDomain.zulipchat.com/api/v1/external/INTEGRATION_NAME?api_key=API_KEY
                    channel: "#general"
                    executionId: "{{trigger.executionId}}"

                triggers:
                  - id: failed_prod_workflows
                    type: io.kestra.plugin.core.trigger.Flow
                    conditions:
                      - type: io.kestra.plugin.core.condition.ExecutionStatus
                        in:
                          - FAILED
                          - WARNING
                      - type: io.kestra.plugin.core.condition.ExecutionNamespace
                        namespace: prod
                        prefix: true
                """
        )
    },
    aliases = "io.kestra.plugin.notifications.zulip.ZulipExecution"
)
public class ZulipExecution extends ZulipTemplate implements ExecutionInterface {
    @Builder.Default
    private final Property<String> executionId = Property.ofExpression("{{ execution.id }}");
    private Property<Map<String, Object>> customFields;
    private Property<String> customMessage;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        this.templateUri = Property.ofValue("zulip-template.peb");
        this.templateRenderMap = Property.ofValue(ExecutionService.executionMap(runContext, this));

        return super.run(runContext);
    }
}
