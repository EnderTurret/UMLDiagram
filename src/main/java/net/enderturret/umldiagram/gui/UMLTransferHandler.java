package net.enderturret.umldiagram.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

/**
 * Handles drag-and-drop support for the GUI.
 * @author EnderTurret
 */
class UMLTransferHandler extends TransferHandler {

	private final Consumer<File> callback;

	UMLTransferHandler(Consumer<File> callback) {
		this.callback = callback;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		return support.isDrop() && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!support.isDrop()) return false;

		try {
			final Transferable trans = support.getTransferable();
			final List<File> files = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);

			if (files.isEmpty()) return false;

			callback.accept(files.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}