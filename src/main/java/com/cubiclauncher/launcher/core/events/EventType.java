/*
 *
 *  * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 *
 */

package com.cubiclauncher.launcher.core.events;

public enum EventType {
    // Eventos de descarga
    DOWNLOAD_STARTED,
    DOWNLOAD_PROGRESS,
    DOWNLOAD_COMPLETED,
    DOWNLOAD_FAILED,

    // Eventos de versiones
    VERSION_DOWNLOADED,
    VERSION_INSTALLED,
    VERSION_DELETED,
    VERSION_LAUNCHED,

    // Eventos del juego
    GAME_STARTED,
    GAME_STOPPED,
    GAME_CRASHED,
    GAME_OUTPUT,

    // Eventos de configuración
    SETTING_CHANGED,
    PROFILE_CHANGED,

    // Eventos de autenticación
    AUTH_SUCCESS,
    AUTH_FAILED,
    AUTH_LOGOUT,

    // Eventos del sistema
    ERROR,
    WARNING,
    INFO,

    // Eventos de UI
    WINDOW_OPENED,
    WINDOW_CLOSED,
    THEME_CHANGED
}
