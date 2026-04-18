<script lang="ts">
    import type { InstanceDto } from "$lib/types/types";
    import { t } from "$lib/i18n";

    let { instance } = $props<{ instance: InstanceDto }>();
</script>

<section class="details-panel">
    <div class="section-title">{t('instanceView.details.title')}</div>
    <div class="details-list">
        <div class="detail-item">
            <div class="detail-label">{t('instanceView.details.version')}</div>
            <div class="detail-value">{instance.version}</div>
        </div>
        <div class="detail-item">
            <div class="detail-label">{t('instanceView.details.loader')}</div>
            <div class="detail-value">{instance.loader || "Vanilla"}</div>
        </div>
        <div class="detail-item">
            <div class="detail-label">{t('instanceView.details.location')}</div>
            <div
                class="detail-value"
                style="font-size: 0.72rem; opacity: 0.8; word-break: break-all;"
            >
                ~/.cubic/instances/{instance.name}
            </div>
        </div>
    </div>
</section>
