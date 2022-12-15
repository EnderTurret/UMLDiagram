package net.enderturret.umldiagram.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLEditorKit;

import net.enderturret.umldiagram.IClass;
import net.enderturret.umldiagram.UMLDiagram;
import net.enderturret.umldiagram.UMLDiagrams;
import net.enderturret.umldiagram.source.Access;
import net.enderturret.umldiagram.source.IDiagramFormatter;
import net.enderturret.umldiagram.util.Settings;
import net.enderturret.umldiagram.util.Util;

/**
 * The program's GUI.
 * @author EnderTurret
 */
public class Gui extends JFrame {

	private JCheckBoxMenuItem onlyPublic;
	private JCheckBoxMenuItem sort;

	private JTextPane area;

	private IClass source;
	private String diagram;
	private String diagramHtml;

	public Gui(String version, IClass initial) {
		initUI(version, initial);
		setVisible(true);
	}

	private void initUI(String version, IClass initial) {
		setLayout(new BorderLayout());
		setTitle("UMLDiagram " + version);
		setMinimumSize(new Dimension(400, 400));
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		final JPanel root = new JPanel(new BorderLayout());
		getContentPane().add(root);

		makeMenuBar();

		final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

		area = new JTextPane();
		area.setEditable(false);
		area.setEditorKit(new HTMLEditorKit());
		area.setFont(font);
		if (initial != null) {
			source = initial;
			refresh();
		} else
			area.setText("Drag a class file here or open one from the menu.");

		root.add(new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		final UMLTransferHandler handler = new UMLTransferHandler(this::open);
		setTransferHandler(handler);
		area.setTransferHandler(handler);

		pack();
	}

	private void makeMenuBar() {
		final JMenuBar bar = new JMenuBar();

		final JMenu file = new JMenu("File");
		bar.add(file);

		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() { return "Java class files"; }
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".class");
			}
		});

		final JMenu open = new JMenu("Open...");

		final JMenuItem openFile = new JMenuItem("File...");
		openFile.addActionListener(e -> {
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				open(chooser.getSelectedFile());
		});
		open.add(openFile);

		final JMenuItem openClasspath = new JMenuItem("From Classpath...");
		openClasspath.addActionListener(e -> {
			final String name = JOptionPane.showInputDialog(this, "Enter the fully qualified name of the class to open.", "Input Class", JOptionPane.PLAIN_MESSAGE);
			if (name == null) return;

			Class<?> clazz = null;

			try {
				try {
					clazz = Class.forName(name, false, Gui.class.getClassLoader());
				} catch (ClassNotFoundException e1) {
					JOptionPane.showMessageDialog(this, "The class \"" + name + "\" couldn't be found.", "Class Not Found", JOptionPane.ERROR_MESSAGE);
				}
			} catch (NoClassDefFoundError e1) {
				JOptionPane.showMessageDialog(this, "The class \"" + name + "\" couldn't be loaded as one or more classes it depends on is missing.", "Failed to Load Class", JOptionPane.ERROR_MESSAGE);
			} catch (Throwable e1) {
				JOptionPane.showMessageDialog(this, "The class \"" + name + "\" couldn't be loaded.\n\n" + Util.getStackTrace(e1), "Failed to Load Class", JOptionPane.ERROR_MESSAGE);
			}

			if (clazz == null) return;

			source = new IClass.Reflected(clazz);
			refresh();
		});
		open.add(openClasspath);

		file.add(open);

		final JMenu copy = new JMenu("Copy to Clipboard...");

		final JMenuItem copyText = new JMenuItem("Plain Text");
		copyText.addActionListener(e -> {
			if (diagram != null)
				setClipboard(diagram);
		});
		copy.add(copyText);

		final JMenuItem copyMarkdown = new JMenuItem("Markdown");
		copyMarkdown.addActionListener(e -> {
			if (diagram != null)
				setClipboard("```\n" + diagram + "\n```");
		});
		copy.add(copyMarkdown);

		final JMenuItem copyHtml = new JMenuItem("HTML");
		copyHtml.addActionListener(e -> {
			if (diagramHtml != null)
				setClipboard(diagramHtml);
		});
		copy.add(copyHtml);

		file.add(copy);

		final JMenu settings = new JMenu("Settings");
		bar.add(settings);

		onlyPublic = new JCheckBoxMenuItem("Only Public");
		onlyPublic.setToolTipText("Only show public members in UML diagrams.");
		onlyPublic.addItemListener(e -> refresh());
		settings.add(onlyPublic);
		sort = new JCheckBoxMenuItem("Sort");
		sort.setToolTipText("Sort members in UML diagrams.");
		sort.addItemListener(e -> refresh());
		settings.add(sort);

		file.setMnemonic('F');
		open.setMnemonic('O');
		openFile.setMnemonic('i');
		openClasspath.setMnemonic('l');
		copy.setMnemonic('C');
		copyText.setMnemonic('T');
		copyMarkdown.setMnemonic('M');
		copyHtml.setMnemonic('H');
		settings.setMnemonic('S');
		onlyPublic.setMnemonic('P');
		sort.setMnemonic('r');

		setJMenuBar(bar);
	}

	private Settings settings(boolean formatting) {
		final Settings settings = new Settings()
				.showGenerics(true)
				.sort(sort.isSelected());

		if (!onlyPublic.isSelected())
			settings.setAllVisible();

		if (formatting)
			settings.formatter(new HtmlDiagramFormatter());

		return settings;
	}

	private void refresh() {
		if (source != null) {
			diagramHtml = "<pre>" + source.toUML(settings(true)).toString() + "</pre>";
			diagram = source.toUML(settings(false)).toString();
			area.setText(diagramHtml);
			//.replaceAll("\\b(boolean|byte|int|short|long|float|double)\\b", "<span color=\"800000\">$1</span>")
		}
	}

	private void open(File file) {
		try {
			final byte[] code = Files.readAllBytes(file.toPath());
			source = new IClass.ASM(code);
			refresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setClipboard(String text) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
	}
}