<script lang="ts">
</script>

<div class="screenshots-section">
    <div class="row-header">
        <span class="section-title">Screenshots</span>
        <button class="view-all-btn">Ver todas (0)</button>
    </div>
    <div class="screenshots-grid">
        <div class="screenshot-card empty">
            <div class="empty-screenshot-content">
                <span class="empty-icon"></span>
                <span class="empty-text">No hay capturas aún</span>
            </div>
        </div>
    </div>
</div>

<style>
    .screenshots-section {
        flex: 1;
        display: flex;
        flex-direction: column;
    }
    .row-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
    }
    .section-title {
        margin-bottom: 0;
    }
    .view-all-btn {
        background: transparent;
        border: 1px solid var(--border);
        color: var(--text-secondary);
        font-size: 0.65rem;
        padding: 4px 8px;
        border-radius: 4px;
        cursor: pointer;
        transition: color 0.2s;
    }
    .view-all-btn:hover {
        color: var(--text-primary);
        border-color: rgba(255, 255, 255, 0.2);
    }
    .screenshots-grid {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 180px;
    }
    .screenshot-card {
        background: rgba(255, 255, 255, 0.02);
        border: 1px solid var(--border);
        border-radius: 6px;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        overflow: hidden;
    }
    .empty-screenshot-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        opacity: 0.3;
    }
    .empty-icon {
        font-size: 1.5rem;
    }
    .empty-text {
        font-size: 0.75rem;
        font-weight: 500;
    }
</style>
