/*
 * Copyright (C) 2026 Santiagolxx, Notstaff and CubicLauncher contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cubiclauncher.launcher.core.auth;

import com.cubiclauncher.claunch.auth.Account;
import com.cubiclauncher.claunch.auth.AccountManager;
import com.cubiclauncher.claunch.auth.AuthCallback;
import com.cubiclauncher.claunch.auth.MicrosoftAuthenticator;
import com.cubiclauncher.launcher.core.PathManager;
import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventData;
import com.cubiclauncher.launcher.core.events.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Singleton central que provee acceso al AccountManager de claunch.
 * Maneja la migración desde el sistema legacy (username strings en
 * SettingsManager)
 * al nuevo sistema multi-auth con UUIDs persistentes.
 */
public class AccountManagerProvider {
    private static final Logger log = LoggerFactory.getLogger(AccountManagerProvider.class);
    private static volatile AccountManagerProvider instance;

    // Client ID (verificado para el flujo de autenticación de
    // Minecraft)
    private static final String MICROSOFT_CLIENT_ID = "6aea8aa7-e635-4ab4-b07f-ce639e19b743";

    private final AccountManager accountManager;
    private final MicrosoftAuthenticator microsoftAuthenticator;

    private AccountManagerProvider() {
        PathManager pm = PathManager.getInstance();
        try {
            this.accountManager = new AccountManager(pm.getGamePath());
            log.info("AccountManager inicializado en: {}", pm.getGamePath());
        } catch (IOException e) {
            log.error("Error crítico inicializando AccountManager", e);
            throw new RuntimeException("No se pudo inicializar el AccountManager", e);
        }

        this.microsoftAuthenticator = new MicrosoftAuthenticator(MICROSOFT_CLIENT_ID);

        // Migrar cuentas legacy si es necesario
        migrateLegacyAccounts();
    }

    public static synchronized AccountManagerProvider getInstance() {
        if (instance == null) {
            instance = new AccountManagerProvider();
        }
        return instance;
    }

    /**
     * Migra las cuentas antiguas de SettingsManager (lista de strings)
     * al nuevo AccountManager con UUIDs persistentes.
     * Solo se ejecuta si el AccountManager está vacío y hay cuentas legacy.
     */
    private void migrateLegacyAccounts() {
        if (!accountManager.isEmpty()) {
            log.debug("AccountManager ya tiene cuentas, omitiendo migración legacy");
            return;
        }

        SettingsManager sm = SettingsManager.getInstance();
        List<String> legacyAccounts = sm.getUserAccounts();

        if (legacyAccounts.isEmpty()) {
            log.debug("No hay cuentas legacy para migrar");
            return;
        }

        log.info("Migrando {} cuentas legacy al nuevo sistema multi-auth", legacyAccounts.size());
        String selectedUsername = sm.getUsername();

        try {
            for (String username : legacyAccounts) {
                Account account = accountManager.addOfflineAccount(username);
                log.info("Cuenta migrada: {} -> UUID: {}", username, account.getUuid());
            }

            // Seleccionar la cuenta que estaba activa
            List<Account> migrated = accountManager.findByUsername(selectedUsername);
            if (!migrated.isEmpty()) {
                accountManager.selectAccount(migrated.get(0));
                log.info("Cuenta activa seleccionada: {}", selectedUsername);
            }

            log.info("Migración completada. {} cuentas migradas.", accountManager.getAccountCount());
        } catch (IOException e) {
            log.error("Error durante la migración de cuentas legacy", e);
        }
    }

    // ==================== ACCESO AL ACCOUNT MANAGER ====================

    /**
     * Obtiene el AccountManager subyacente para acceso completo.
     */
    public AccountManager getAccountManager() {
        return accountManager;
    }

    /**
     * Obtiene la cuenta actualmente seleccionada.
     */
    public Account getSelectedAccount() {
        return accountManager.getSelectedAccount();
    }

    /**
     * Selecciona una cuenta y emite evento de cambio.
     */
    public void selectAccount(Account account) {
        try {
            accountManager.selectAccount(account);
            EventBus.get().emit(EventType.ACCOUNT_CHANGED,
                    EventData.builder()
                            .put("username", account.getUsername())
                            .put("uuid", account.getUuid())
                            .put("type", account.getType().name())
                            .build());
            log.info("Cuenta seleccionada: {} ({})", account.getUsername(), account.getUuid());
        } catch (IOException e) {
            log.error("Error al seleccionar cuenta: {}", account.getUsername(), e);
        }
    }

    /**
     * Inicia el proceso de autenticación con Microsoft de forma asíncrona.
     * Retorna un CompletableFuture que se completa con la cuenta agregada.
     */
    public CompletableFuture<Account> loginWithMicrosoft(AuthCallback callback) {
        log.info("Iniciando proceso de autenticación con Microsoft...");
        return microsoftAuthenticator.authenticateAsync(callback)
                .thenApply(result -> {
                    Account account = addMicrosoftAccount(result.getUsername(), result.getUuid(),
                            result.getAccessToken());
                    if (account != null) {
                        selectAccount(account);
                        log.info("Autenticación con Microsoft exitosa para: {}", result.getUsername());
                    }
                    return account;
                })
                .exceptionally(ex -> {
                    log.error("Error en la autenticación con Microsoft", ex);
                    return null;
                });
    }

    /**
     * Agrega una cuenta Microsoft al manager.
     */
    public Account addMicrosoftAccount(String username, String uuid, String accessToken) {
        try {
            Account account = accountManager.addMicrosoftAccount(username, uuid, accessToken);
            log.info("Cuenta Microsoft agregada: {} (UUID: {})", username, uuid);
            return account;
        } catch (IOException e) {
            log.error("Error al agregar cuenta Microsoft: {}", username, e);
            return null;
        }
    }

    /**
     * Agrega una cuenta offline con UUID aleatorio y emite evento.
     */
    public Account addOfflineAccount(String username) {
        try {
            Account account = accountManager.addOfflineAccount(username);
            log.info("Nueva cuenta offline: {} (UUID: {})", username, account.getUuid());
            return account;
        } catch (IOException e) {
            log.error("Error al agregar cuenta offline: {}", username, e);
            return null;
        }
    }

    /**
     * Elimina una cuenta y emite evento si era la seleccionada.
     */
    public void removeAccount(Account account) {
        try {
            String removedUsername = account.getUsername();
            accountManager.removeAccount(account);
            log.info("Cuenta eliminada: {}", removedUsername);

            // Si quedan cuentas, emitir evento de cambio
            if (!accountManager.isEmpty()) {
                Account newSelected = accountManager.getSelectedAccount();
                EventBus.get().emit(EventType.ACCOUNT_CHANGED,
                        EventData.builder()
                                .put("username", newSelected.getUsername())
                                .put("uuid", newSelected.getUuid())
                                .put("type", newSelected.getType().name())
                                .build());
            }
        } catch (IOException e) {
            log.error("Error al eliminar cuenta: {}", account.getUsername(), e);
        }
    }

    /**
     * Obtiene todas las cuentas.
     */
    public List<Account> getAccounts() {
        return accountManager.getAccounts();
    }

    /**
     * Verifica si hay cuentas registradas.
     */
    public boolean isEmpty() {
        return accountManager.isEmpty();
    }

    /**
     * Obtiene el número de cuentas.
     */
    public int getAccountCount() {
        return accountManager.getAccountCount();
    }

    /**
     * Busca una cuenta por UUID.
     */
    public Optional<Account> findByUuid(String uuid) {
        return accountManager.findByUuid(uuid);
    }
}
