package com.refinedmods.refinedstorage2.platform.common.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public abstract class AbstractSafeSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void save(final File file) {
        if (!isDirty()) {
            return;
        }
        final var targetPath = file.toPath().toAbsolutePath();
        final var tempFile = targetPath.getParent().resolve(file.getName() + ".temp");
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", this.save(new CompoundTag()));
        NbtUtils.addCurrentDataVersion(compoundTag);
        try {
            // Write to temp file first.
            NbtIo.writeCompressed(compoundTag, tempFile.toFile()); // TODO backport to 1.20.1 - is the .toFile() correct?
            // Try atomic move
            try {
                Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (final AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final IOException e) {
            LOGGER.error("Could not save data {}", this, e);
        }
        setDirty(false);
    }
}
