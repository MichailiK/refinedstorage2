package com.refinedmods.refinedstorage2.platform.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public final class SecurityToastHelper {
    // TODO not sure how to replace this with the backport to 1.20.1
    // private static final SystemToast.SystemToastId NO_PERMISSION_TOAST_ID = new SystemToast.SystemToastId();

    private SecurityToastHelper() {
    }

    public static void addNoPermissionToast(final Component message) {
        SystemToast.add(
            Minecraft.getInstance().getToasts(),
            // TODO not sure how to replace this with the backport to 1.20.1
            SystemToast.SystemToastIds.TUTORIAL_HINT,
            createTranslation("misc", "no_permission"),
            message
        );
    }
}
