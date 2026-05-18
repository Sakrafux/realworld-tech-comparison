import { diag, DiagConsoleLogger, DiagLogLevel, metrics } from "@opentelemetry/api";
import { OTLPMetricExporter } from "@opentelemetry/exporter-metrics-otlp-http";
import { MeterProvider, PeriodicExportingMetricReader } from "@opentelemetry/sdk-metrics";
import { resourceFromAttributes } from "@opentelemetry/resources";
import { ATTR_SERVICE_NAME } from "@opentelemetry/semantic-conventions";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import { HttpInstrumentation } from "@opentelemetry/instrumentation-http";
import type { OtelConfig } from "./config.js";

export function initOtel(cfg: OtelConfig): MeterProvider | null {
    if (!cfg.enabled) {
        return null;
    }

    if (cfg.diagnosticLoggingEnabled) {
        diag.setLogger(new DiagConsoleLogger(), DiagLogLevel.DEBUG);
    }

    // 1. Define the Metric Exporter and Reader
    const metricReader = new PeriodicExportingMetricReader({
        exporter: new OTLPMetricExporter({
            url: `${cfg.endpoint}/v1/metrics`,
        }),
        exportIntervalMillis: 10000,
    });

    // 2. Initialize the standalone MeterProvider
    const meterProvider = new MeterProvider({
        resource: resourceFromAttributes({
            [ATTR_SERVICE_NAME]: cfg.serviceName,
        }),
        readers: [metricReader],
    });

    // 3. Set it globally so the API and instrumentations can locate it
    metrics.setGlobalMeterProvider(meterProvider);

    // 4. Explicitly bind instrumentations to the meter provider
    registerInstrumentations({
        meterProvider: meterProvider,
        instrumentations: [new HttpInstrumentation()],
    });

    return meterProvider;
}
