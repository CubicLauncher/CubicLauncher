/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
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

package com.cubiclauncher.launcher.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.AppenderBase;
import com.cubiclauncher.launcher.ui.views.ErrorConsoleView;

public class LogAppender extends AppenderBase<ILoggingEvent> {
    private PatternLayoutEncoder encoder;

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (encoder != null) {
            String formattedMessage = new String(encoder.encode(eventObject));
            ErrorConsoleView.getInstance().log(formattedMessage.trim());
        } else {
            ErrorConsoleView.getInstance().log(eventObject.getFormattedMessage());
        }
    }

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }
}
