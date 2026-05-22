import logging

from opentelemetry import metrics
from opentelemetry.exporter.otlp.proto.http.metric_exporter import OTLPMetricExporter
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader
from opentelemetry.sdk.resources import Resource, SERVICE_NAME

from shared.config.env import settings

_logger = logging.getLogger(__name__)


def init_otel() -> MeterProvider | None:
    if not settings.OTEL_ENABLED:
        return None

    metric_reader = PeriodicExportingMetricReader(
        OTLPMetricExporter(
            endpoint=f"{settings.OTEL_EXPORTER_OTLP_ENDPOINT}/v1/metrics",
        ),
        export_interval_millis=10_000,
    )

    # Use Resource.create() to get default telemetry attributes merged in
    resource = Resource.create({
        SERVICE_NAME: settings.OTEL_SERVICE_NAME,
    })

    meter_provider = MeterProvider(
        resource=resource,
        metric_readers=[metric_reader],
    )

    metrics.set_meter_provider(meter_provider)

    _logger.info("OpenTelemetry metrics initialized (service=%s endpoint=%s)",
                 settings.OTEL_SERVICE_NAME, settings.OTEL_EXPORTER_OTLP_ENDPOINT)

    return meter_provider


# Removed async/await: shutdown is a blocking call
def shutdown_otel(meter_provider: MeterProvider | None) -> None:
    if meter_provider is None:
        return

    meter_provider.shutdown()
    _logger.info("OpenTelemetry metrics shut down")