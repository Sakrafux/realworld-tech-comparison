import { diag, DiagConsoleLogger, DiagLogLevel } from "@opentelemetry/api";
import { NodeSDK } from "@opentelemetry/sdk-node";
import { OTLPMetricExporter } from "@opentelemetry/exporter-metrics-otlp-http";
import { PeriodicExportingMetricReader } from "@opentelemetry/sdk-metrics";
import { resourceFromAttributes } from "@opentelemetry/resources"; // Updated import
import { ATTR_SERVICE_NAME } from "@opentelemetry/semantic-conventions";
import { HttpInstrumentation } from "@opentelemetry/instrumentation-http";
import { ExpressInstrumentation } from "@opentelemetry/instrumentation-express";
import type { OtelConfig } from "./config.js";

export function initOtel(cfg: OtelConfig): NodeSDK | null {
    if (!cfg.enabled) {
        return null;
    }

    if (cfg.diagnosticLoggingEnabled) {
        diag.setLogger(new DiagConsoleLogger(), DiagLogLevel.DEBUG);
    }

    const sdk = new NodeSDK({
        resource: resourceFromAttributes({
            [ATTR_SERVICE_NAME]: cfg.serviceName,
        }),
        metricReaders: [
            new PeriodicExportingMetricReader({
                exporter: new OTLPMetricExporter({
                    url: `${cfg.endpoint}/v1/metrics`,
                }),
                exportIntervalMillis: 10000,
            }),
        ],
        instrumentations: [new HttpInstrumentation(), new ExpressInstrumentation()],
    });

    sdk.start();

    return sdk;
}
